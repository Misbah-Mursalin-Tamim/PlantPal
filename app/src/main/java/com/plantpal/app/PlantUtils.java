package com.plantpal.app;

import com.plantpal.app.data.MyPlant;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

// Small helper with the date rules used all over the app.
public class PlantUtils {

    public static final long DAY_MILLIS = 24L * 60 * 60 * 1000;

    // Next watering = last watered + interval days
    public static long nextWateringMillis(MyPlant plant) {
        return plant.lastWateredMillis + plant.intervalDays * DAY_MILLIS;
    }

    // Midnight (00:00) of the day that contains the given time
    private static long startOfDay(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    // Negative = overdue, 0 = today, 1 = tomorrow, and so on
    public static int daysUntilDue(MyPlant plant) {
        long diff = startOfDay(nextWateringMillis(plant)) - startOfDay(System.currentTimeMillis());
        return (int) Math.round(diff / (double) DAY_MILLIS);
    }

    // A plant is due if today is its watering day or it is overdue
    public static boolean isDue(MyPlant plant) {
        return daysUntilDue(plant) <= 0;
    }

    public static String formatDate(long millis) {
        return new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(new Date(millis));
    }

    public static String statusText(int days) {
        if (days < 0) {
            int late = -days;
            return "Overdue by " + late + (late == 1 ? " day" : " days");
        } else if (days == 0) {
            return "Water today";
        } else if (days == 1) {
            return "Water tomorrow";
        } else {
            return "In " + days + " days";
        }
    }

    // Frequent = 3 days, Average = 7, Minimum = 14, anything else = 7
    public static int wateringToDays(String wateringText) {
        if (wateringText == null) return 7;
        String w = wateringText.toLowerCase();
        if (w.contains("frequent")) return 3;
        if (w.contains("minimum")) return 14;
        return 7;
    }
    // True if the plant's last watering happened today
    public static boolean wateredToday(MyPlant plant) {
        return startOfDay(plant.lastWateredMillis) == startOfDay(System.currentTimeMillis());
    }
}
