package com.plantpal.app.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SpeciesCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SpeciesCache> species);

    // Finds cached plants whose name contains the typed text
    @Query("SELECT * FROM species_cache WHERE commonName LIKE '%' || :query || '%' ORDER BY commonName COLLATE NOCASE ASC LIMIT 30")
    List<SpeciesCache> search(String query);

    @Query("SELECT COUNT(*) FROM species_cache")
    int count();
}