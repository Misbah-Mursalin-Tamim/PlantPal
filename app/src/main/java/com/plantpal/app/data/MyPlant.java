package com.plantpal.app.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "my_plants")
public class MyPlant {

    // Unique text id. The same id is used later as the Firestore document id.
    @PrimaryKey
    @NonNull
    public String id = "";

    public String name;           // e.g. "Aloe Vera"
    public String wateringText;   // e.g. "Minimum" (from the API)
    public int intervalDays;      // e.g. 14 (we convert the text to days)
    public String sunlight;       // e.g. "Full sun, Part shade"
    public long lastWateredMillis; // time of last watering (milliseconds)
}
