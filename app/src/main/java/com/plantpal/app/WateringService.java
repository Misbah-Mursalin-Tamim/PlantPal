package com.plantpal.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

// Foreground service: keeps the "Watered X of Y" notification while Watering Mode is open
public class WateringService extends Service {

    public static final String EXTRA_DONE = "done";
    public static final String EXTRA_TOTAL = "total";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int done = intent != null ? intent.getIntExtra(EXTRA_DONE, 0) : 0;
        int total = intent != null ? intent.getIntExtra(EXTRA_TOTAL, 0) : 0;

        NotificationHelper.createChannels(this);
        // Must be called quickly after startForegroundService()
        startForeground(NotificationHelper.ID_WATERING, buildNotification(done, total));
        return START_NOT_STICKY;
    }

    private Notification buildNotification(int done, int total) {
        Intent open = new Intent(this, WateringModeActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 20, open, NotificationHelper.piFlags());

        String text = (total > 0 && done >= total) ? "All done!" : "Watered " + done + " of " + total;

        return new NotificationCompat.Builder(this, NotificationHelper.CHANNEL_WATERING)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Watering Mode")
                .setContentText(text)
                .setProgress(total, done, false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // not a bound service
    }
}