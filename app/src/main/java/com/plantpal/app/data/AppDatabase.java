package com.plantpal.app.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {MyPlant.class, SpeciesCache.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract MyPlantDao myPlantDao();

    public abstract SpeciesCacheDao speciesCacheDao();

    private static volatile AppDatabase INSTANCE;

    // Room does not allow database work on the main (UI) thread.
    // Everywhere in the app we run database code inside AppDatabase.IO.execute(...)
    public static final ExecutorService IO = Executors.newFixedThreadPool(2);

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "plantpal.db")
                            .fallbackToDestructiveMigration() // dev only: if the tables change, wipe instead of crashing
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}