package com.kamantsev.nytimes.controllers;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.kamantsev.nytimes.models.Article;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kamantsev.nytimes.models.Category;

public class DataManager {

    private static final String DOWNLOAD_FAILED = "Downloading failed. See logs for more details.",
            DELETE_FAILED = "Removing file failed. See logs for more details.";

    //Розділ, обраний за замовчуванням
    private static final String defaultSection = "all-sections";

    private static Handler UIHandler;//For performing actions on UI thread

    //У додатку об'єкти статей існують у єдиному екземплярі
    private static List<Long>[] pages;//список індексів статей за категоріями
    private static Map<Long, Article> uniqueArticles;//Список унікальних статей

    private static Context context;//Контекст додатку, необхідний для деяких операцій

    private static Set<DataLoadingListener>[] dataLoadingListeners;

    static {
        int tabsCount = Category.values().length;
        pages = new LinkedList[tabsCount];//переважно додавання або послідовний доступ
        dataLoadingListeners = new HashSet[tabsCount];//найшвидша реалізація. Впорядкування не потрібне
        for (int i = 0; i < tabsCount; i++) {
            pages[i] = new LinkedList<>();
            dataLoadingListeners[i] = new HashSet<>();
        }
        uniqueArticles = new HashMap<>();//найшвидша реалізація. Впорядкування не потрібне
    }


    //Service
    public static void initialize(Context context) {
        DataManager.context = context;
        UIHandler = new Handler();

        loadCategory(Category.FAVORITE);
    }

    public static Context getContext() {
        return context;
    }

    //Network
    public static void loadCategory(final Category category) {

        DataLoadingListener listener = new DataLoadingListener() {
            @Override
            public void onLoadingSucceed() {
                notifyOnDataLoadingListener(category, true);
            }

            @Override
            public void onLoadingFailed() {
                notifyOnDataLoadingListener(category, false);
            }
        };

        if(category == Category.FAVORITE){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //Failure on loading favorite isn't expected
                    DeviceStorageDataProvider.loadFavorite();
                    notifyOnDataLoadingListener(category, true);
                }
            }).start();
        }else{

            try {
                if(context == null){
                    Log.e("DataManager", "null context");
                }
                //Fix android protocols' bug in old versions
                ProviderInstaller.installIfNeeded(context);
            } catch (GooglePlayServicesRepairableException e) {
                Log.e("DataManager", "static initializer", e);
            } catch (GooglePlayServicesNotAvailableException e) {
                Log.e("DataManager", "static initializer", e);
            }

            NetworkDataProvider.requestData(category, listener);
        }
    }

    public static void loadImage(ImageView imageView, String url) {
        NetworkDataProvider.loadImage(imageView, url);
    }


    //Data modifiers
    static void setCategory(Category category, List<Article> articles) {
        clearCategory(category);
        for (Article article : articles) {
            Long articleID = article.getArticleExtra().getId();//Long required for keys comparison
            if (uniqueArticles.containsKey(articleID)) {
                uniqueArticles.get(articleID).addCategory(category);
            } else {
                uniqueArticles.put(articleID, article);
            }
            pages[category.ordinal()].add(articleID);
        }
        notifyOnDataLoadingListener(category, true);
    }

    static void setFavorite(List<Article> articles) {
        clearCategory(Category.FAVORITE);
        for (Article article : articles) {
            Long articleID = article.getArticleExtra().getId();//Long required for keys comparison
            if (uniqueArticles.containsKey(articleID)) {
                Article oldArticle = uniqueArticles.get(articleID);
                oldArticle.addCategory(Category.FAVORITE);
                //will add category FAVORITE & local path to files to old article
                oldArticle.updateData(article);
            }else {
                uniqueArticles.put(articleID, article);
            }
            pages[Category.FAVORITE.ordinal()].add(articleID);
        }
        notifyOnDataLoadingListener(Category.FAVORITE, true);
    }

    public static Article getArticle(Category category, int index) {
        Long key = pages[category.ordinal()].get(index);
        return uniqueArticles.get(key);
    }

    public static Article getArticle(long id) {
        return uniqueArticles.get(id);
    }

    public static int getArticleCount(Category category) {
        return pages[category.ordinal()].size();
    }


    //Data listeners
    public static void registerOnDataChangeListener(Category category, DataLoadingListener listener) {
        dataLoadingListeners[category.ordinal()].add(listener);
    }

    public static void unregisterOnDataChangeListener(Category category, DataLoadingListener listener) {
        dataLoadingListeners[category.ordinal()].remove(listener);
    }

    private static void notifyOnDataLoadingListener(final Category category, final boolean isSucceed) {
        //most listeners(in fact all) need to update their UI on data change
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                for (DataLoadingListener listener : dataLoadingListeners[category.ordinal()]) {
                    if(isSucceed) {
                        listener.onLoadingSucceed();
                    }else{
                        listener.onLoadingFailed();
                    }
                }
            }
        });
    }


    //Files operations
    public static void addToFavorite(Long articleID) {
        final Article article = getArticle(articleID);

        new Thread(new Runnable() {
            @Override
            public void run() {
                article.addCategory(Category.LOADING);
                if (DeviceStorageDataProvider.saveArticle(article)) {
                    article.addCategory(Category.FAVORITE);
                    pages[Category.FAVORITE.ordinal()].add(article.getArticleExtra().getId());
                    notifyOnDataLoadingListener(Category.FAVORITE, true);
                } else {
                    notifyOnDataLoadingListener(Category.FAVORITE, false);
                }
                article.removeCategory(Category.LOADING);
            }
        }).start();
    }

    public static void removeFromFavorite(Long articleID) {
        final Article article = getArticle(articleID);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (DeviceStorageDataProvider.deleteArticle(article)) {
                    removeArticle(Category.FAVORITE, article);
                    notifyOnDataLoadingListener(Category.FAVORITE, true);
                } else {
                    notifyOnDataLoadingListener(Category.FAVORITE, false);
                }
            }
        }).start();
    }



    private static void clearCategory(Category category){
        Iterator<Long> iterator = pages[category.ordinal()].iterator();
        while(iterator.hasNext()){
            Long id = iterator.next();
            Article article = uniqueArticles.get(id);
            removeArticle(category, article, iterator);
        }
    }

    private static void removeArticle(Category category, Article article, Iterator<Long> iterator){
        iterator.remove();
        article.removeCategory(category);
        if(article.getCategoriesCount() == 0){
            uniqueArticles.remove(article.getArticleExtra().getId());
        }
    }

    private static void removeArticle(Category category, Article article){
        pages[category.ordinal()].remove(article.getArticleExtra().getId());
        article.removeCategory(category);
        if(article.getCategoriesCount() == 0){
            uniqueArticles.remove(article.getArticleExtra().getId());
        }
    }



    //Inners
    public interface DataLoadingListener {
        void onLoadingSucceed();
        void onLoadingFailed();
    }

    public interface FileOperationCallback {
        void onSucceed();

        void onFailure(String message);
    }
}