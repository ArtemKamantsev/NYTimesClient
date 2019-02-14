package com.kamantsev.nytimes.controllers;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
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

//Provides any data access in the app
public class DataManager {

    //Labels for dialog of confirmation article removing from Favorite
    private static final String alertTitle = "Removal confirmation",
            alertMessage = "This article will be removed from \"Favorite\" tab" +
                    " and from device storage. Are you sure you want to remove it?",
            alertBtn1 = "Yes",
            alertBtn2 = "No";

    private static Handler UIHandler;//For performing actions on UI thread

    private static List<Long>[] pages;//Articles by categories
    private static Map<Long, Article> uniqueArticles;//Unique articles

    private static Context context;//Application context

    private static Set<DataModifiedListener>[] dataModifiedListeners;


    static {
        int tabsCount = 4;
        pages = new LinkedList[tabsCount];//adding or sequential access prevails
        dataModifiedListeners = new HashSet[tabsCount];
        for (int i = 0; i < tabsCount; i++) {
            pages[i] = new LinkedList<>();
            dataModifiedListeners[i] = new HashSet<>();//The faster implementation. No ordering required
        }
        uniqueArticles = new HashMap<>();//The faster implementation. No ordering required
        UIHandler = new Handler(Looper.getMainLooper());
    }

    //Service
    public static void initialize(Context context) {
        DataManager.context = context;
        loadCategory(Category.FAVORITE);
    }

    public static Context getContext() {
        return context;
    }

    //Network
    public static void loadCategory(final Category category) {

        if (category == Category.FAVORITE) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DeviceStorageDataProvider.loadFavorite();
                    //Failure on loading favorite isn't expected
                    notifyOnDataModifiedListener(category, DataModifiedListener.Status.CATEGORY_LOADED);
                }
            }).start();
        } else {

            try {
                /*if(context == null){
                    Log.e("DataManager", "null context");
                }*/
                //Fix android protocols' bug in old versions
                ProviderInstaller.installIfNeeded(context);
            } catch (GooglePlayServicesRepairableException e) {
                Log.e("DataManager", "static initializer", e);
            } catch (GooglePlayServicesNotAvailableException e) {
                Log.e("DataManager", "static initializer", e);
            }

            NetworkDataProvider.requestData(category, new DataModifiedListener() {
                @Override
                public void onDataModified(Status status) {
                    notifyOnDataModifiedListener(category, status);
                }
            });
        }
    }

    public static void loadImage(ImageView imageView, String url) {
        NetworkDataProvider.loadImage(imageView, url);
    }


    //Data modifiers
    static void setCategory(Category category, List<Article> articles) {
        clearCategory(category);//remove old category's articles
        for (Article article : articles) {
            Long articleID = article.getArticleExtra().getId();
            if (uniqueArticles.containsKey(articleID)) {
                //If such article is exist yes
                uniqueArticles.get(articleID).addCategory(category);
            } else {
                //If new article was received
                uniqueArticles.put(articleID, article);
            }
            pages[category.ordinal()].add(articleID);
        }
    }

    static void setFavorite(List<Article> articles) {
        clearCategory(Category.FAVORITE);//remove old Favorite articles information
        for (Article article : articles) {
            Long articleID = article.getArticleExtra().getId();//Long required for keys comparison
            if (uniqueArticles.containsKey(articleID)) {
                Article oldArticle = uniqueArticles.get(articleID);
                oldArticle.addCategory(Category.FAVORITE);
                //will add category FAVORITE & local path to files to old article
                oldArticle.updateData(article);
            } else {
                uniqueArticles.put(articleID, article);
            }
            pages[Category.FAVORITE.ordinal()].add(articleID);
        }
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
    public static void registerOnDataModifiedListener(Category category, DataModifiedListener listener) {
        dataModifiedListeners[category.ordinal()].add(listener);
    }

    public static void unregisterOnDataModifiedListener(Category category, DataModifiedListener listener) {
        dataModifiedListeners[category.ordinal()].remove(listener);
    }

    private static void notifyOnDataModifiedListener(final Category category, final DataModifiedListener.Status status) {
        //most listeners(in fact all) need to update their UI on data change
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                for (DataModifiedListener listener : dataModifiedListeners[category.ordinal()]) {
                    listener.onDataModified(status);
                }
            }
        });
    }


    //Files operations
    public static void addToFavorite(Long articleID) {//Adding article to Favorite category
        final Article article = getArticle(articleID);
        article.addCategory(Category.LOADING);//Set category, which indicates downloading
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (DeviceStorageDataProvider.saveArticle(article)) {
                    //Saving succeed
                    article.addCategory(Category.FAVORITE);
                    pages[Category.FAVORITE.ordinal()].add(article.getArticleExtra().getId());
                    notifyOnDataModifiedListener(Category.FAVORITE, DataModifiedListener.Status.ARTICLE_SAVED);
                } else {
                    //Saving failed
                    notifyOnDataModifiedListener(Category.FAVORITE, DataModifiedListener.Status.ARTICLE_SAVING_FAILED);
                }
                article.removeCategory(Category.LOADING);//Downloading has finished
            }
        }).start();
    }

    public static void tryToRemoveFromFavorite(Context context, final Long articleID) {
        //Request remove confirmation
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(alertTitle);
        alertDialog.setMessage(alertMessage);
        alertDialog.setPositiveButton(alertBtn1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                removeFromFavorite(articleID);
            }
        });
        alertDialog.setNegativeButton(alertBtn2, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                notifyOnDataModifiedListener(Category.FAVORITE, DataModifiedListener.Status.ARTICLE_REMOVING_CANCELED);
            }
        });
        alertDialog.setCancelable(true);
        alertDialog.show();
    }


    private static void removeFromFavorite(final Long articleID) {//Removing article from Favorite
        final Article article = getArticle(articleID);

        new Thread(new Runnable() {
            @Override
            public void run() {
                //Remove article from local storage and it's information from DB
                if (DeviceStorageDataProvider.deleteArticle(article)) {
                    //Removing succeed
                    removeArticle(Category.FAVORITE, article);
                    notifyOnDataModifiedListener(Category.FAVORITE, DataModifiedListener.Status.ARTICLE_REMOVED);
                } else {
                    //Removing failed
                    notifyOnDataModifiedListener(Category.FAVORITE, DataModifiedListener.Status.ARTICLE_REMOVING_FAILED);
                }
            }
        }).start();
    }

    private static void clearCategory(Category category) {//Remove all articles from category
        Iterator<Long> iterator = pages[category.ordinal()].iterator();
        while (iterator.hasNext()) {
            Long id = iterator.next();
            Article article = uniqueArticles.get(id);
            removeArticle(category, article, iterator);
        }
    }

    //Remove article via iterator
    private static void removeArticle(Category category, Article article, Iterator<Long> iterator) {
        //Remove article from specified category
        iterator.remove();
        article.removeCategory(category);
        //Remove article from app, if no category has refer to it
        if (article.getCategoriesCount() == 0) {
            uniqueArticles.remove(article.getArticleExtra().getId());
        }
    }

    //Remove article directly
    private static void removeArticle(Category category, Article article) {
        //Remove article from specified category(and from app, if no category has refer to it)
        pages[category.ordinal()].remove(article.getArticleExtra().getId());
        article.removeCategory(category);
        //Remove article from app, if no category has refer to it
        if (article.getCategoriesCount() == 0) {
            uniqueArticles.remove(article.getArticleExtra().getId());
        }
    }


    //Inners
    public interface DataModifiedListener {
        void onDataModified(Status status);

        enum Status {
            CATEGORY_LOADED,
            CATEGORY_LOADING_FAILED,
            ARTICLE_SAVED,
            ARTICLE_SAVING_FAILED,
            ARTICLE_REMOVED,
            ARTICLE_REMOVING_FAILED,
            ARTICLE_REMOVING_CANCELED
        }
    }
}