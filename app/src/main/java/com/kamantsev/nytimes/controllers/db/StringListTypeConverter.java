package com.kamantsev.nytimes.controllers.db;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class StringListTypeConverter {
    private static final Gson  gson= new Gson();

    @TypeConverter
    public static List<String> stringToList(String data){
        if(data == null){
            return Collections.emptyList();
        }
        Type listType = new TypeToken<List<String>>(){}.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String listToString(List<String> data){
        return gson.toJson(data);
    }

}
