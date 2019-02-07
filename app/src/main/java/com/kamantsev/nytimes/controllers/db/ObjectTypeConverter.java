package com.kamantsev.nytimes.controllers.db;

import android.arch.persistence.room.TypeConverter;

public class ObjectTypeConverter {

    @TypeConverter
    public static Object stringToObject(String data){
        return new Object();//we don't use that field. Their values aren't important
    }

    @TypeConverter
    public static String objectToString(Object data){
        return data.toString();
    }
}
