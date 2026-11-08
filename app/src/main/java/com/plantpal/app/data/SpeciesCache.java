package com.plantpal.app.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Stores plant search results from the API so repeat searches work offline.
@Entity(tableName = "species_cache")
public class SpeciesCache {

    @PrimaryKey
    public int apiId;             // the plant's id in the Perenual API

    public String commonName;     // e.g. "Aloe Vera"
    public String scientificName; // e.g. "Aloe barbadensis"
    public String watering;       // "Frequent", "Average", "Minimum"
    public String sunlight;       // e.g. "Full sun, Part shade"
}