package com.kamantsev.nytimes.controllers.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.kamantsev.nytimes.models.request_model.MediaMetadata;

import java.util.List;


@Dao
public interface MediaMetadataDao {

    @Query("SELECT * FROM media_metadata WHERE mediaId=:mediaId")
    List<MediaMetadata> getMetadataForMedium(final long mediaId);

    @Insert
    void insert(MediaMetadata medium);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(MediaMetadata... metadata);

    @Delete
    void delete(MediaMetadata... metadata);
}
