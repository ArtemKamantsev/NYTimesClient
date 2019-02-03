package com.kamantsev.nytimes.controllers.db;

import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import com.kamantsev.nytimes.models.request_model.AbstractResult;
import com.kamantsev.nytimes.models.request_model.Media;
import com.kamantsev.nytimes.models.request_model.MediaMetadata;


@android.arch.persistence.room.Database(entities = {AbstractResult.class, Media.class, MediaMetadata.class},
                                        version = 1, exportSchema = false)
@TypeConverters({StringListTypeConverter.class})
public abstract class Database extends RoomDatabase {

    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "articles";
    private static Database sInstance;


    public static Database getInstance(Context context){
        if(sInstance==null){
            synchronized (LOCK){
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        Database.class, DATABASE_NAME).build();
            }
        }
        return sInstance;
    }

    public abstract ResultDao getResultDao();

    public abstract MediumDao getMediumDao();

    public abstract MediaMetadataDao getMetadataDao();
}