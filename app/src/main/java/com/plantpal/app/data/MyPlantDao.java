package com.plantpal.app.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MyPlantDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MyPlant plant);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MyPlant> plants);

    @Update
    void update(MyPlant plant);

    @Delete
    void delete(MyPlant plant);

    @Query("SELECT * FROM my_plants ORDER BY name COLLATE NOCASE ASC")
    List<MyPlant> getAll();

    @Query("SELECT * FROM my_plants WHERE id = :plantId LIMIT 1")
    MyPlant getById(String plantId);

    @Query("SELECT COUNT(*) FROM my_plants")
    int count();

    @Query("DELETE FROM my_plants")
    void deleteAll();
}