package com.udacity.lineker.wakemethere.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface PlaceDao {

    @Query("SELECT * FROM place ORDER BY id")
    LiveData<List<PlaceEntry>> loadAll();

    @Query("SELECT * FROM place where active=1 ORDER BY id")
    List<PlaceEntry> loadAllActivesSync();

    @Query("SELECT * FROM place WHERE placeId=:placeId ORDER BY id")
    PlaceEntry findPlaceByPlaceId(final String placeId);

    @Query("SELECT * FROM place WHERE id=:id ORDER BY id")
    PlaceEntry findPlaceById(final int id);

    @Insert
    void insert(PlaceEntry placeEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(PlaceEntry placeEntry);

    @Delete()
    void delete(PlaceEntry placeEntry);
}
