package com.udacity.lineker.wakemethere.database;

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
    List<PlaceEntry> loadAllSync();

    @Insert
    void insert(PlaceEntry placeEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(PlaceEntry placeEntry);

    @Delete()
    void delete(PlaceEntry placeEntry);
}
