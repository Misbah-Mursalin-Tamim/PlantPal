package com.plantpal.app;

import com.plantpal.app.data.SpeciesCache;

import java.util.ArrayList;
import java.util.List;

// Small built-in list, used only when the API fails AND the cache has nothing.
// Ids are negative so they can never clash with real API ids.
public class BackupPlants {

    private static final List<SpeciesCache> ALL = new ArrayList<>();

    static {
        add(-1, "Aloe Vera", "Aloe barbadensis", "Minimum", "Full sun, Part shade");
        add(-2, "Snake Plant", "Sansevieria trifasciata", "Minimum", "Low light, Part shade");
        add(-3, "Mint", "Mentha spicata", "Frequent", "Full sun, Part shade");
        add(-4, "Basil", "Ocimum basilicum", "Frequent", "Full sun");
        add(-5, "Money Plant", "Epipremnum aureum", "Average", "Part shade");
        add(-6, "Spider Plant", "Chlorophytum comosum", "Average", "Part shade");
        add(-7, "Peace Lily", "Spathiphyllum wallisii", "Average", "Low light, Part shade");
        add(-8, "Jade Plant", "Crassula ovata", "Minimum", "Full sun");
        add(-9, "Rose", "Rosa", "Average", "Full sun");
        add(-10, "Tulsi (Holy Basil)", "Ocimum tenuiflorum", "Frequent", "Full sun");
        add(-11, "Cactus", "Cactaceae", "Minimum", "Full sun");
        add(-12, "Fern", "Nephrolepis exaltata", "Frequent", "Part shade");
        add(-13, "Rubber Plant", "Ficus elastica", "Average", "Part shade");
        add(-14, "Lavender", "Lavandula", "Minimum", "Full sun");
        add(-15, "Tomato", "Solanum lycopersicum", "Frequent", "Full sun");
    }

    private static void add(int id, String name, String sci, String watering, String sun) {
        SpeciesCache s = new SpeciesCache();
        s.apiId = id;
        s.commonName = name;
        s.scientificName = sci;
        s.watering = watering;
        s.sunlight = sun;
        ALL.add(s);
    }

    public static List<SpeciesCache> search(String query) {
        List<SpeciesCache> out = new ArrayList<>();
        String q = query.toLowerCase().trim();
        for (SpeciesCache s : ALL) {
            if (q.isEmpty() || s.commonName.toLowerCase().contains(q)) {
                out.add(s);
            }
        }
        return out;
    }
}