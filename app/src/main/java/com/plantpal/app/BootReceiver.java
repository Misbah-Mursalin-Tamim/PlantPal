package com.plantpal.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

// Alarms are erased when the phone restarts, so this sets the daily alarm again
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            ReminderScheduler.schedule(context);
            Log.d("PlantPalBoot", "Boot completed: daily reminder rescheduled");
        }
    }
}