package com.kamantsev.nytimes.controllers;

import android.os.Environment;

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
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

//Клас, що працює з базою даних та локальним сховищем даних
class DeviceStorageDataProvider {

    /*
        Схема файлів додатку:
        NYTimes - папка додатку
            |_Date(or default folder) - папки, що групують статті за датою
                |_name_of_article - папка з файлами для певної статті
                    |_Article.html,... - файли певної статті
    */

    private static final String defaultFolder = "no_date";//Файли зберігаються у цю папку, якщо шлях до них на сервері не містить дати
    private static final String baseFilePath;//шлях до папки файлів додатку

    private static final ResultDao resultDao;
    private static final MediumDao mediumDao;
    private static final MediaMetadataDao metadataDao;

    static {
        baseFilePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "NewYorkTimes";
        Database database = Database.getInstance(DataManager.getContext());
        resultDao = database.getResultDao();
        mediumDao = database.getMediumDao();
        metadataDao = database.getMetadataDao();
    }

    static void loadFavorite() {//Завантаження даних категорії "Favorite"
        new Thread(new Runnable() {
            @Override
            public void run() {

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
                    articles.add(article);
                }

                DataManager.setCategory(Category.FAVORITE, articles);

            }
        }).start();
    }

    static void saveInDataBase(Article article) {//Додаємо інформацію про статтю до бази даних

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

    static void deleteFromDataBase(Article article) {
        //Видаляємо запис про статтю з бази даних. Каскадно видаляться зв'язані дані
        resultDao.deleteResult(article.getArticleExtra());
    }


    //Видалення вважається успішним, якщо видалена уся папка файлу, проте не обов'язково уся категорія
    static boolean deleteFile(String destPath) {
        boolean isSucceed = false;
        if (isExternalStorageWritable()) {
            /*String path=getFullPath(destPath);
            File file = new File(path);
            isSucceed = file.delete();//видаляємо сам файл
            path=path.substring(0,path.lastIndexOf('/'));//шлях до папки файлу
            file=new File(path);
            if(file.list().length==0){//якщо немає інних файлів
                isSucceed&=file.delete();//видаляємо папку статті
                path=path.substring(0,path.lastIndexOf('/'));//шлях до папки певної дати
                file=new File(path);
                if(file.list().length==0)//якщо більше немає статей певної дати
                   file.delete();//видаляємо цю папку певної дати
            }*/
        }
        return isSucceed;
    }

    private static String getPath(Article article) {
        String res = baseFilePath + File.separator + article.getArticleExtra().getPublishedDate()
                + article.getArticleExtra().getTitle();
        return res;
    }

    private static boolean isExternalStorageWritable() {//Чи можливий запис у файлову систему
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private static boolean checkFileExist(String idPath) {//чи наявний файл
        File file = new File(idPath);
        if (file.exists())
            return true;
        else {
            //якщо фалу не існує, то перевірка створить все одно папки, що ведуть до нього, згідно зі шляхом
            try {
                //тому створюємо і видаляємо файл, що не існував а також папки, що ведуть до нього
                file.createNewFile();
                deleteFile(idPath);
            } catch (IOException e) {
            }
            return false;
        }
    }
}
