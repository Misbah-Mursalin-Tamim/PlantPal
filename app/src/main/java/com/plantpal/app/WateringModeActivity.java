package com.plantpal.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.plantpal.app.data.AppDatabase;
import com.plantpal.app.data.MyPlant;
import com.plantpal.app.databinding.ActivityWateringModeBinding;

import java.util.ArrayList;
import java.util.List;

public class WateringModeActivity extends AppCompatActivity implements WateringAdapter.Listener {

    private ActivityWateringModeBinding binding;
    private WateringAdapter adapter;
    private final List<WateringAdapter.Item> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWateringModeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Watering Mode");

        adapter = new WateringAdapter(this);
        binding.recyclerWatering.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerWatering.setAdapter(adapter);
        binding.btnFinish.setOnClickListener(v -> finish());

        // Load the plants that are due today (or overdue)
        AppDatabase.IO.execute(() -> {
            List<MyPlant> all = AppDatabase.getInstance(getApplicationContext()).myPlantDao().getAll();
            List<WateringAdapter.Item> due = new ArrayList<>();
            for (MyPlant p : all) {
                if (PlantUtils.isDue(p)) due.add(new WateringAdapter.Item(p));
            }
            runOnUiThread(() -> {
                items.clear();
                items.addAll(due);
                adapter.setItems(items);
                boolean empty = items.isEmpty();
                binding.tvEmptyWatering.setVisibility(empty ? View.VISIBLE : View.GONE);
                binding.recyclerWatering.setVisibility(empty ? View.GONE : View.VISIBLE);
                updateProgress(false);
            });
        });
    }

    @Override
    public void onToggle(WateringAdapter.Item item, boolean checked) {
        final MyPlant p = item.plant;
        p.lastWateredMillis = checked ? System.currentTimeMillis() : item.originalLast;
        AppDatabase.IO.execute(() ->
                AppDatabase.getInstance(getApplicationContext()).myPlantDao().update(p));
        CloudSync.upload(p);
        updateProgress(true);
    }

    private void updateProgress(boolean fromTick) {
        int total = items.size();
        if (total == 0) {
            binding.tvProgress.setText("Nothing needs water today");
            return;
        }
        int done = 0;
        for (WateringAdapter.Item i : items) {
            if (i.done) done++;
        }
        binding.tvProgress.setText("Watered " + done + " of " + total);

        // Start the foreground service, or update its notification if it's already running
        Intent service = new Intent(this, WateringService.class);
        service.putExtra(WateringService.EXTRA_DONE, done);
        service.putExtra(WateringService.EXTRA_TOTAL, total);
        ContextCompat.startForegroundService(this, service);

        if (fromTick && done == total) {
            Toast.makeText(this, "All plants watered! 🌱", Toast.LENGTH_SHORT).show();
        }
    }

    // Leaving Watering Mode for good removes the notification
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            stopService(new Intent(this, WateringService.class));
        }
    }
}