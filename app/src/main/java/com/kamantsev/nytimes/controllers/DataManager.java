package com.kamantsev.nytimes.controllers;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.kamantsev.nytimes.models.Article;
import com.kamantsev.nytimes.models.request_model.AbstractResult;
import com.kamantsev.nytimes.models.request_model.NYTResponse;
import com.kamantsev.nytimes.models.request_model.ResultEmailed;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kamantsev.nytimes.models.Category;

public class DataManager {

    //Розділ, обраний за замовчуванням
    private static final String defaultSection = "all-sections";

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

        try {
            ProviderInstaller.installIfNeeded(context);
        } catch (GooglePlayServicesRepairableException e) {
            Log.e("DataManager", "static initializer", e);
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e("DataManager", "static initializer", e);
        }

        DeviceStorageDataProvider.loadFavorite();//завантажуємо категорію "Favorite"
    }

    public static Context getContext() {
        return context;
    }

    //Network
    public static void loadCategory(Category category) {
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
                if(!article.isBelong(category)){
                    article.addCategory(category);
                }
            }
            pages[category.ordinal()].add(article);
        }
        notifyOnDataChangeListener(category);
    }

    public static Article getArticle(Category category, int index) {
        return pages[category.ordinal()].get(index);
    }

    public static int getArticleCount(Category category) {
        return pages[category.ordinal()].size();
    }



    //Data listeners
    public static void registedOnDataChangeListener(Category category, OnDataChangeListener listener){
        dataChangeListeners[category.ordinal()].add(listener);
    }

    public static void unregisterOnDataChangeListener(Category category, OnDataChangeListener listener){
        dataChangeListeners[category.ordinal()].remove(listener);
    }

    private static void notifyOnDataChangeListener(Category category){
        for(OnDataChangeListener listener : dataChangeListeners[category.ordinal()]){
            listener.onContentChanged();
        }
    }


    //Files operations
    public static synchronized boolean addToFavorite(Article article) {//Додаємо статтю до категорії "Favorite"
        //true - якщо було додано до обраного, false - при помилці скачування/збереження даних або якщо стаття вже обрана
        boolean isSucceed = false;

        if (!article.isBelong(Category.FAVORITE)) {

            isSucceed = downloadFavorite(article);

            //Якщо стаття успішно збережена, відзначаємо її як обрану
            if (isSucceed) {
                article.addCategory(Category.FAVORITE);
                pages[Category.FAVORITE.ordinal()].add(article);
            }
        }
        return isSucceed;
    }

    public static synchronized boolean removeFromFavorite(Article article) {//Видаляємо статтю з категорії "Favorite"
        boolean isSucceed = false;

        if (article.isBelong(Category.FAVORITE)) {
            DeviceStorageDataProvider.deleteFromDataBase(article);

                /*if (!DeviceStorageDataProvider.deleteFile(article)) {
                    Toast.makeText(getContext(), "File isn't deleted", Toast.LENGTH_LONG);//сповіщуємо користувача, що файл не вдалось видалити
                }*/

                //Навіть якщо файл не видалено із системи, то для додатку його вже не існує.
                article.unfavorite();
                pages[Category.FAVORITE.ordinal()].remove(article);
                isSucceed = true;
        }

        return isSucceed;
    }

    private static synchronized boolean downloadFavorite(Article article) {
        boolean isSucceed = false;
        //Намагаємось зберігти файл на пам'ять девайсу
       /* if (NetworkDataProvider.downloadFile(article.getArticleExtra().getUrl(), article)) {
            if (DeviceStorageDataProvider.saveInDataBase(article))//Намагаємось записати інформацію про статтю до бази даних
                isSucceed = true;
        }*/

        return isSucceed;
    }


    //Inners
    public interface OnDataChangeListener {
        void onContentChanged();
    }
}