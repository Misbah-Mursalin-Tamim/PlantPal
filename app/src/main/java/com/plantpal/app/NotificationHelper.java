package com.plantpal.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

// Shared notification code: channels, flags, and the reminder notification
public class NotificationHelper {

    public static final String CHANNEL_REMINDER = "plant_reminders";
    public static final String CHANNEL_WATERING = "watering_mode";
    public static final int ID_REMINDER = 1;
    public static final int ID_WATERING = 2;

    // Android 8+ requires every notification to belong to a channel
    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            if (nm == null) return;
            nm.createNotificationChannel(new NotificationChannel(
                    CHANNEL_REMINDER, "Watering reminders", NotificationManager.IMPORTANCE_DEFAULT));
            nm.createNotificationChannel(new NotificationChannel(
                    CHANNEL_WATERING, "Watering Mode", NotificationManager.IMPORTANCE_LOW));
        }
    }

    // PendingIntents must be IMMUTABLE on Android 12+
    public static int piFlags() {
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return flags;
    }

    public static void showReminder(Context context, String text) {
        createChannels(context);

        Intent open = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 10, open, piFlags());

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_REMINDER)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Watering Reminder")
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(context).notify(ID_REMINDER, b.build());
    }
}