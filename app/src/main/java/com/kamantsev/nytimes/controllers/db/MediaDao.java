package com.kamantsev.nytimes.controllers.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.kamantsev.nytimes.models.request_model.Media;

import java.util.List;

@Dao
public interface MediaDao {

    @Query("SELECT * FROM Media WHERE resultId=:resultId")
    List<Media> getMediumsForResult(final long resultId);

    @Insert
    Long insert(Media media);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Media... media);

    @Delete
    void delete(Media... media);
}
