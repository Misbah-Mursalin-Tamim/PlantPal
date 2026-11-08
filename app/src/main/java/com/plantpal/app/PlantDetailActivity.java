package com.plantpal.app;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.plantpal.app.data.AppDatabase;
import com.plantpal.app.data.MyPlant;
import com.plantpal.app.databinding.ActivityPlantDetailBinding;

public class PlantDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PLANT_ID = "plant_id";

    private ActivityPlantDetailBinding binding;
    private MyPlant plant; // null until loaded from Room

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlantDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Plant Details");

        final String id = getIntent().getStringExtra(EXTRA_PLANT_ID);
        if (id == null) {
            finish();
            return;
        }

        binding.btnWatered.setOnClickListener(v -> markWatered());
        binding.btnInfo.setOnClickListener(v -> openInfoPage());
        binding.btnDelete.setOnClickListener(v -> confirmDelete());

        AppDatabase.IO.execute(() -> {
            MyPlant p = AppDatabase.getInstance(getApplicationContext()).myPlantDao().getById(id);
            runOnUiThread(() -> {
                if (p == null) {
                    finish();
                    return;
                }
                plant = p;
                showPlant();
            });
        });
    }

    private void showPlant() {
        int days = PlantUtils.daysUntilDue(plant);
        binding.tvName.setText(plant.name);
        binding.tvStatus.setText(PlantUtils.statusText(days));
        binding.tvStatus.setTextColor(days < 0 ? Color.parseColor("#C62828")
                : days == 0 ? Color.parseColor("#EF6C00") : Color.parseColor("#2E7D32"));
        binding.tvCare.setText("Watering: " + plant.wateringText
                + " (every " + plant.intervalDays + " days)");
        binding.tvLast.setText("Last watered: " + PlantUtils.formatDate(plant.lastWateredMillis));
        binding.tvNext.setText("Next watering: "
                + PlantUtils.formatDate(PlantUtils.nextWateringMillis(plant)));
    }

    private void markWatered() {
        if (plant == null) return;
        final MyPlant p = plant;
        p.lastWateredMillis = System.currentTimeMillis();
        showPlant();
        AppDatabase.IO.execute(() ->
                AppDatabase.getInstance(getApplicationContext()).myPlantDao().update(p));
        CloudSync.upload(p);
        Toast.makeText(this, "Marked as watered", Toast.LENGTH_SHORT).show();
    }

    // IMPLICIT Intent: we don't name an app, Android opens whichever browser the user has
    private void openInfoPage() {
        if (plant == null) return;
        Uri uri = Uri.parse("https://www.google.com/search?q=" + Uri.encode(plant.name + " plant care"));
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No browser found", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete() {
        if (plant == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Delete plant?")
                .setMessage("Remove " + plant.name + " from your garden?")
                .setPositiveButton("Delete", (d, w) -> {
                    final MyPlant p = plant;
                    AppDatabase.IO.execute(() -> {
                        AppDatabase.getInstance(getApplicationContext()).myPlantDao().delete(p);
                        runOnUiThread(this::finish);
                    });
                    CloudSync.delete(p.id);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}