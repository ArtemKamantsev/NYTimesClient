package com.kamantsev.nytimes.controllers.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.kamantsev.nytimes.models.request_model.AbstractResult;

import java.util.List;

@Dao
public interface ResultDao {

    @Query("Select * FROM result")
    List<AbstractResult> loadAllResults();

    @Insert
    void insertResult(AbstractResult result);

    @Delete
    void deleteResult(AbstractResult result);
}
