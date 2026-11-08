package com.plantpal.app;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.plantpal.app.data.AppDatabase;
import com.plantpal.app.data.MyPlant;
import com.plantpal.app.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PlantAdapter.OnPlantClickListener {

    private ActivityMainBinding binding;
    private PlantAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            goToLogin();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("My Plants");

        adapter = new PlantAdapter(this);
        binding.recyclerPlants.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerPlants.setAdapter(adapter);

        binding.btnAddPlant.setOnClickListener(v ->
                startActivity(new Intent(this, AddPlantActivity.class)));

        binding.btnWateringMode.setOnClickListener(v ->
                startActivity(new Intent(this, WateringModeActivity.class)));

        binding.btnTestReminder.setOnClickListener(v -> {
            Intent test = new Intent(this, ReminderReceiver.class);
            test.putExtra(ReminderReceiver.EXTRA_TEST, true);
            sendBroadcast(test);
        });

        binding.btnTodayList.setOnClickListener(v -> showTodayList());

        binding.btnLogout.setOnClickListener(v -> logout());

        CloudSync.syncOnStart(this, () -> {
            if (!isFinishing() && !isDestroyed()) loadPlants();
        });

        NotificationHelper.createChannels(this);
        askNotificationPermission();
        ReminderScheduler.schedule(this);
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            String perm = "android.permission.POST_NOTIFICATIONS";
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{perm}, 100);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding == null) return;
        loadPlants();
    }

    private void loadPlants() {
        AppDatabase.IO.execute(() -> {
            List<MyPlant> list = new ArrayList<>(
                    AppDatabase.getInstance(getApplicationContext()).myPlantDao().getAll());
            runOnUiThread(() -> showPlants(list));
        });
    }

    private void showPlants(List<MyPlant> list) {
        Collections.sort(list, (a, b) ->
                Integer.compare(PlantUtils.daysUntilDue(a), PlantUtils.daysUntilDue(b)));
        adapter.setPlants(list);

        boolean empty = list.isEmpty();
        binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.recyclerPlants.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void showTodayList() {
        AppDatabase.IO.execute(() -> {
            StringBuilder sb = new StringBuilder();
            Cursor c = getContentResolver().query(PlantProvider.CONTENT_URI, null, null, null, null);
            if (c != null) {
                try {
                    while (c.moveToNext()) {
                        String name = c.getString(c.getColumnIndexOrThrow("name"));
                        boolean watered = c.getInt(c.getColumnIndexOrThrow("watered")) == 1;
                        sb.append(watered ? "✅ " : "💧 ").append(name).append("\n");
                        Log.d("PlantPalProvider", name + " watered=" + watered);
                    }
                } finally {
                    c.close();
                }
            }
            final String text = sb.length() == 0 ? "Nothing to show today." : sb.toString();
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                new AlertDialog.Builder(this)
                        .setTitle("Today's list")
                        .setMessage(text)
                        .setPositiveButton("OK", null)
                        .show();
            });
        });
    }

    @Override
    public void onPlantClick(MyPlant plant) {
        Intent intent = new Intent(this, PlantDetailActivity.class);
        intent.putExtra(PlantDetailActivity.EXTRA_PLANT_ID, plant.id);
        startActivity(intent);
    }

    private void logout() {
        AppDatabase.IO.execute(() -> {
            AppDatabase.getInstance(getApplicationContext()).myPlantDao().deleteAll();
            runOnUiThread(() -> {
                FirebaseAuth.getInstance().signOut();
                goToLogin();
            });
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}