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
    private static List<Article>[] pages;//список статей за категоріями
    private static Map<Long, Article> uniqueArticles;//Список унікальних статей

    private static Context context;//Контекст додатку, необхідний для деяких операцій

    private static Set<OnDataChangeListener>[] dataChangeListeners;

    static {
        int tabsCount = Category.values().length;
        pages = new LinkedList[tabsCount];//переважно додавання або послідовний доступ
        dataChangeListeners = new HashSet[tabsCount];//найшвидша реалізація. Впорядкування не потрібне
        for (int i = 0; i < tabsCount; i++) {
            pages[i] = new LinkedList<>();
            dataChangeListeners[i] = new HashSet<>();
        }
        uniqueArticles = new HashMap<>();//найшвидша реалізація. Впорядкування не потрібне
    }


    //Service
    public static void initialize(Context context) {
        DataManager.context = context;
        UIHandler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                //завантажуємо категорію "Favorite"
                DeviceStorageDataProvider.loadFavorite();
            }
        }).start();

    }

    public static Context getContext() {
        return context;
    }

    //Network
    public static void loadCategory(Category category) {
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

        NetworkDataProvider.requestData(category);
    }

    public static void loadCategories() {
        loadCategory(Category.EMAILED);
        loadCategory(Category.SHARED);
        loadCategory(Category.VIEWED);
    }

    public static void loadImage(ImageView imageView, String url) {
        NetworkDataProvider.loadImage(imageView, url);
    }


    //Data modifiers
    static void setCategory(Category category, List<Article> articles) {
        for (Article article : articles) {
            Long resId = article.getArticleExtra().getId();//Long required for keys comparison
            if (!uniqueArticles.containsKey(resId)) {
                uniqueArticles.put(resId, article);
            } else {
                article = uniqueArticles.get(resId);
                if (!article.isBelong(category)) {
                    article.addCategory(category);
                }
            }
            pages[category.ordinal()].add(article);
        }
        notifyOnDataChangeListener(category);
    }

    static void setFavorite(List<Article> articles) {
        for (Article article : articles) {
            Long resId = article.getArticleExtra().getId();//Long required for keys comparison
            if (uniqueArticles.containsKey(resId)) {
                //copy old article's categories
                Article oldArticle = uniqueArticles.get(resId);
                for (Category category : Category.values()) {
                    if (oldArticle.isBelong(category)) {
                        article.addCategory(category);
                        pages[category.ordinal()].add(article);
                    }
                }
            }
            uniqueArticles.put(resId, article);
            pages[Category.FAVORITE.ordinal()].add(article);
        }
        notifyOnDataChangeListener(Category.FAVORITE);
    }

    public static Article getArticle(Category category, int index) {
        return pages[category.ordinal()].get(index);
    }

    public static Article getArticle(long id) {
        return uniqueArticles.get(id);
    }

    public static int getArticleCount(Category category) {
        return pages[category.ordinal()].size();
    }


    //Data listeners
    public static void registerOnDataChangeListener(Category category, OnDataChangeListener listener) {
        dataChangeListeners[category.ordinal()].add(listener);
    }

    public static void unregisterOnDataChangeListener(Category category, OnDataChangeListener listener) {
        dataChangeListeners[category.ordinal()].remove(listener);
    }

    private static void notifyOnDataChangeListener(Category category) {
        for (OnDataChangeListener listener : dataChangeListeners[category.ordinal()]) {
            listener.onContentChanged();
        }
    }


    //Files operations
    public static void addToFavorite(final Article article, final FileOperationCallback callback) {
        if (!article.isBelong(Category.FAVORITE)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (DeviceStorageDataProvider.saveArticle(article)) {
                        article.addCategory(Category.FAVORITE);
                        pages[Category.FAVORITE.ordinal()].add(article);
                        runOnUISucceed(callback);
                    } else {
                        runOnUIFailure(callback, DOWNLOAD_FAILED);
                    }
                }
            }).start();
        }
    }

    public static void removeFromFavorite(final Article article, final FileOperationCallback callback) {
        if (article.isBelong(Category.FAVORITE)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (DeviceStorageDataProvider.deleteArticle(article)) {
                        article.unfavorite();
                        pages[Category.FAVORITE.ordinal()].remove(article);
                        runOnUISucceed(callback);
                    } else {
                        runOnUIFailure(callback, DELETE_FAILED);
                    }
                }
            }).start();
        }
    }

    private static void runOnUIFailure(final FileOperationCallback callback, final String err) {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onFailure(err);
            }
        });
    }

    private static void runOnUISucceed(final FileOperationCallback callback) {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onSucceed();
            }
        });
    }

    //Inners
    public interface OnDataChangeListener {
        void onContentChanged();
    }

    public interface FileOperationCallback {
        void onSucceed();

        void onFailure(String mesage);
    }
}