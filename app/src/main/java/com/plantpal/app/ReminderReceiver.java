package com.plantpal.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.plantpal.app.data.AppDatabase;
import com.plantpal.app.data.MyPlant;

import java.util.List;

// Runs when the daily alarm fires: lists the plants that need water today
public class ReminderReceiver extends BroadcastReceiver {

    public static final String EXTRA_TEST = "test";

    @Override
    public void onReceive(Context context, Intent intent) {
        final boolean test = intent != null && intent.getBooleanExtra(EXTRA_TEST, false);
        final Context app = context.getApplicationContext();

        // Database work can't run on the main thread, so we keep the receiver alive
        // with goAsync() and finish it when the work is done.
        final PendingResult result = goAsync();
        AppDatabase.IO.execute(() -> {
            try {
                List<MyPlant> all = AppDatabase.getInstance(app).myPlantDao().getAll();
                StringBuilder names = new StringBuilder();
                int count = 0;
                for (MyPlant p : all) {
                    if (PlantUtils.isDue(p)) {
                        if (count > 0) names.append(", ");
                        names.append(p.name);
                        count++;
                    }
                }
                if (count > 0) {
                    NotificationHelper.showReminder(app, "These plants need water today: " + names);
                } else if (test) {
                    NotificationHelper.showReminder(app, "No plants need water today");
                }
            } finally {
                result.finish();
            }
        });
    }
}