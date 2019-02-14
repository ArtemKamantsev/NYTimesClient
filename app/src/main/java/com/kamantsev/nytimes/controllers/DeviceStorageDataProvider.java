package com.kamantsev.nytimes.controllers;

import android.os.Environment;
import android.util.Log;

import com.kamantsev.nytimes.controllers.db.Database;
import com.kamantsev.nytimes.controllers.db.MediaDao;
import com.kamantsev.nytimes.controllers.db.MediaMetadataDao;
import com.kamantsev.nytimes.controllers.db.ResultDao;
import com.kamantsev.nytimes.models.Article;
import com.kamantsev.nytimes.models.Category;
import com.kamantsev.nytimes.models.request_model.AbstractResult;
import com.kamantsev.nytimes.models.request_model.Media;
import com.kamantsev.nytimes.models.request_model.MediaMetadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

//Local storage interaction logic
class DeviceStorageDataProvider {

    /*
        App files' structure:
        NYTimes - base folder
            |_Date - group articles published at the same date
                |_name_of_article - group certain article's files
                    |_Article.html,... - article's files
    */

    private static final String baseFilePath;//path to base folder

    //Data Access Objects for DB interaction
    private static final ResultDao resultDao;
    private static final MediaDao mediumDao;
    private static final MediaMetadataDao metadataDao;

    static {
        baseFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "NewYorkTimes";
        Database database = Database.getInstance(DataManager.getContext());
        resultDao = database.getResultDao();
        mediumDao = database.getMediumDao();
        metadataDao = database.getMetadataDao();
    }

    static synchronized void loadFavorite() {//Loading Favorite Articles

        //Restored articles' list
        List<Article> articles = new LinkedList<>();

        List<AbstractResult> results = resultDao.loadAllResults();
        for (AbstractResult result : results) {
            //Restore Media objects for article
            List<Media> mediaList = mediumDao.getMediumsForResult(result.getId());

            //Restore MediaMetadata objects for each Media object
            for (Media media : mediaList) {
                media.setMediaMetadata(
                        metadataDao.getMetadataForMedium(media.getId()));
            }

            result.setMedia(mediaList);

            //Create article based on restored data
            Article article = new Article(result, Category.FAVORITE);

            if (checkFileExist(getPath(article))) {//check, if file still exist on storage
                articles.add(article);//article has been restored successfully
            } else {
                //File has been removed
                //We should remove entity about it from DB
                deleteFromDataBase(article);
            }
        }

        DataManager.setFavorite(articles);//set articles
    }

    //Save article to local storage and add corresponding entity to DB
    static boolean saveArticle(Article article) {
        //Blocks article from changes
        synchronized (article) {
            String url = article.getArticleExtra().getUrl();//source path
            String path = getPath(article);//saving destination path
            if (downloadFile(url, path)) {//Trying to save file to local storage
                //If downloading succeed, article's descriptive information will be added to DB
                article.getArticleExtra().setPath("file://" + path);//set path to corresponding .html file
                saveToDataBase(article);//saving to DB
                return true;//operation succeed
            }
            return false;//operation failed
        }
    }

    //Remove article's files from local storage and corresponding entity from DB
    static boolean deleteArticle(Article article) {
        //Blocks article from changes
        synchronized (article) {
            String path = getPath(article);//.html file path
            if (deleteFile(path)) {//Trying to remove file from local storage
                //Id removing succeed, article's descriptive information will be removed from DB
                deleteFromDataBase(article);
                article.getArticleExtra().setPath(null);//remove path to .html file
                return true;//operation succeed
            }
            return true;//operation failed
        }
    }

    //Database modifiers
    private static void saveToDataBase(Article article) {
        //add article's descriptive information to DB
        resultDao.insertResult(article.getArticleExtra());//save base information object

        for (Media media : article.getArticleExtra().getMedia()) {
            //attach media object to owning object
            media.setParentEntity(article.getArticleExtra().getId());
            Long id = mediumDao.insert(media);

            for (MediaMetadata metadata : media.getMediaMetadata()) {
                metadata.setParentEntity(id);//attach mediaData's object to owning object
                metadataDao.insert(metadata);
            }
        }
    }

    private static void deleteFromDataBase(Article article) {
        //Remove article information's base entity. Other will be removed cascade
        resultDao.deleteResult(article.getArticleExtra());
    }

    //Local storage files' modifiers
    private static boolean downloadFile(String sUrl, String path) {//Downloading file from Network
        boolean isDownloaded = false;//downloading status
        if (isExternalStorageWritable()) {
            try {
                FileOutputStream os = null;
                InputStream is = null;
                try {
                    makeFolders(path);//make sure all folders are exist
                    os = new FileOutputStream(path);
                    URL url = new URL(sUrl);
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.connect();
                    is = connection.getInputStream();
                    int temp;
                    while ((temp = is.read()) != -1)
                        os.write(temp);

                    isDownloaded = true;//downloading has finished successfully
                } finally {
                    if (os != null) {
                        os.flush();
                        os.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                }
            } catch (IOException e) {//Catch exceptions from downloading file process or finally block
                Log.e("downloadFile", e.toString());
            }
        }
        return isDownloaded;
    }

    private static boolean deleteFile(String path) {
        boolean isSucceed = false;
        if (isExternalStorageWritable()) {
            File file;
            //Remove file and all empty directories above
            for (int i = 0; i < 3; i++) {
                file = new File(path);
                if (file.exists() && (file.list() == null || file.list().length == 0)) {
                    isSucceed = file.delete();
                }
                path = path.substring(0, path.lastIndexOf('/'));
            }
        }
        return isSucceed;
    }


    //Service
    private static void makeFolders(String path) {
        File folders = new File(path.substring(0, path.lastIndexOf('/')));
        if (!folders.exists()) {
            folders.mkdirs();
        }
    }

    private static String getPath(Article article) {//From path to article's .html file
        String title = article.getArticleExtra().getTitle().trim();
        String res = baseFilePath + File.separator + article.getArticleExtra().getPublishedDate()
                + File.separator + title
                + File.separator + title + ".html";
        return res;
    }

    private static boolean checkFileExist(String path) {
        File file = new File(path);
        if (file.exists()) {
            return true;
        } else {
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
