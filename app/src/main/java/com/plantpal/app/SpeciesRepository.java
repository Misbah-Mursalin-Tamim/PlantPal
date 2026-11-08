package com.plantpal.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.plantpal.app.data.AppDatabase;
import com.plantpal.app.data.SpeciesCache;

import java.util.List;

public class SpeciesRepository {

    public interface Callback {
        // source is "api", "cache" or "backup". Always called on the main thread.
        void onResult(List<SpeciesCache> list, String source);
    }

    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    public static void search(Context context, String query, Callback callback) {
        final Context app = context.getApplicationContext();
        final String q = query.trim();

        AppDatabase.IO.execute(() -> {
            List<SpeciesCache> result;
            String source;
            AppDatabase db = AppDatabase.getInstance(app);

            try {
                result = PlantApi.search(q);
                db.speciesCacheDao().insertAll(result); // save for offline use
                source = "api";
            } catch (Exception e) {
                Log.w("PlantPalApi", "API failed, using cache. Reason: " + e);
                result = db.speciesCacheDao().search(q);
                source = "cache";
                if (result.isEmpty()) {
                    result = BackupPlants.search(q);
                    source = "backup";
                }
            }

            final List<SpeciesCache> finalResult = result;
            final String finalSource = source;
            MAIN.post(() -> callback.onResult(finalResult, finalSource));
        });
    }
}