package com.kamantsev.nytimes.controllers;

import android.os.Environment;
import android.util.Log;

import com.kamantsev.nytimes.controllers.db.Database;
import com.kamantsev.nytimes.controllers.db.MediaMetadataDao;
import com.kamantsev.nytimes.controllers.db.MediumDao;
import com.kamantsev.nytimes.controllers.db.ResultDao;
import com.kamantsev.nytimes.models.Article;
import com.kamantsev.nytimes.models.Category;
import com.kamantsev.nytimes.models.request_model.AbstractResult;
import com.kamantsev.nytimes.models.request_model.MediaMetadata;
import com.kamantsev.nytimes.models.request_model.Media;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import static com.kamantsev.nytimes.controllers.DataManager.DataLoadingListener;

//Клас, що працює з базою даних та локальним сховищем даних
class DeviceStorageDataProvider {

    /*
        Схема файлів додатку:
        NYTimes - папка додатку
            |_Date(or default folder) - папки, що групують статті за датою
                |_name_of_article - папка з файлами для певної статті
                    |_Article.html,... - файли певної статті
    */

    private static final String baseFilePath;//шлях до папки файлів додатку

    private static final ResultDao resultDao;
    private static final MediumDao mediumDao;
    private static final MediaMetadataDao metadataDao;

    static {
        baseFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "NewYorkTimes";
        Database database = Database.getInstance(DataManager.getContext());
        resultDao = database.getResultDao();
        mediumDao = database.getMediumDao();
        metadataDao = database.getMetadataDao();
    }

    static synchronized void loadFavorite() {//Завантаження даних категорії "Favorite"

        List<Article> articles = new LinkedList<>();

        List<AbstractResult> results = resultDao.loadAllResults();

        for (AbstractResult result : results) {
            List<Media> mediaList = mediumDao.getMediumsForResult(result.getId());

            for (Media media : mediaList) {
                media.setMediaMetadata(
                        metadataDao.getMetadataForMedium(media.getId()));
            }

            result.setMedia(mediaList);

            Article article = new Article(result, Category.FAVORITE);
            if(checkFileExist(getPath(article))){//this check will remove redundant directories
                articles.add(article);
            }else{
                //if user has removed file
                deleteFromDataBase(article);
            }
        }

        DataManager.setFavorite(articles);
    }

    static boolean saveArticle(Article article){
        //blocks article's information of change
        synchronized(article) {
            String url = article.getArticleExtra().getUrl();
            String path = getPath(article);
            if (downloadFile(url, path)) {
                article.getArticleExtra().setPath("file://" + path);
                saveToDataBase(article);
                return true;
            }
            return false;
        }
    }

    static boolean deleteArticle(Article article){
        //blocks article's information of change
        synchronized (article) {
            String path = getPath(article);
            if (deleteFile(path)) {
                deleteFromDataBase(article);
                article.getArticleExtra().setPath(null);
                return true;
            }
            return true;
        }
    }

    //Database
    private static void saveToDataBase(Article article) {//Додаємо інформацію про статтю до бази даних

        resultDao.insertResult(article.getArticleExtra());

        for (Media media : article.getArticleExtra().getMedia()) {
            media.bindTo(article.getArticleExtra().getId());
            Long id = mediumDao.insert(media);

            for (MediaMetadata metadata : media.getMediaMetadata()) {
                metadata.bindTo(id);
                metadataDao.insert(metadata);
            }
        }
    }

    private static void deleteFromDataBase(Article article) {
        //Видаляємо запис про статтю з бази даних. Каскадно видаляться зв'язані дані
        resultDao.deleteResult(article.getArticleExtra());
    }

    //File system
    private static boolean downloadFile(String sUrl, String path){
        boolean isDownloaded=false;
        if(isExternalStorageWritable()){
            try {
                FileOutputStream os = null;
                InputStream is = null;
                try {
                    makeFolders(path);
                    os = new FileOutputStream(path);
                    URL url = new URL(sUrl);
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.connect();
                    is = connection.getInputStream();
                    int temp;
                    while ((temp = is.read()) != -1)
                        os.write(temp);

                    isDownloaded = true;
                } finally {
                    if (os != null) {
                        os.flush();
                        os.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                }
            } catch (IOException e) {
                Log.e("downloadFile", e.toString());
            }
        }
        return isDownloaded;
    }

    private static boolean deleteFile(String path){
        boolean isSucceed = false;
        Log.e("path:", path);
        if (isExternalStorageWritable()) {
            File file;
            Log.e("path:", path);
            for(int i=0;i<3;i++){
                file=new File(path);
                Log.e("path", path);
                if(file.exists() && (file.list() == null || file.list().length==0)) {
                    isSucceed = file.delete();
                }
                path=path.substring(0,path.lastIndexOf('/'));
            }
        }
        return isSucceed;
    }


    private static void makeFolders(String path){
        File folders = new File(path.substring(0,path.lastIndexOf('/')));
        if(!folders.exists()){
            folders.mkdirs();
        }
    }

    private static String getPath(Article article) {
        String res = baseFilePath + File.separator + article.getArticleExtra().getPublishedDate()
                    + File.separator + article.getArticleExtra().getTitle()
                    + File.separator + article.getArticleExtra().getTitle() + ".html";
        return res;
    }

    private static boolean checkFileExist(String path) {
        Log.e("DataProvider","checkFileExist");
        File file = new File(path);
        if (file.exists()) {
            return true;
        }else {
            return false;
        }
    }

    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
