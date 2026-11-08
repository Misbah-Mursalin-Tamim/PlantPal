package com.plantpal.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.plantpal.app.data.AppDatabase;
import com.plantpal.app.data.MyPlant;
import com.plantpal.app.data.SpeciesCache;
import com.plantpal.app.databinding.ActivityAddPlantBinding;

import java.util.UUID;

public class AddPlantActivity extends AppCompatActivity {

    private ActivityAddPlantBinding binding;
    private SpeciesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPlantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Add Plant");

        adapter = new SpeciesAdapter(this::onSpeciesPicked);
        binding.recyclerSpecies.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSpecies.setAdapter(adapter);

        binding.btnSearch.setOnClickListener(v -> doSearch());
        binding.etQuery.setOnEditorActionListener((v, actionId, event) -> {
            doSearch();
            return true;
        });
    }

    private void doSearch() {
        String query = binding.etQuery.getText().toString().trim();
        if (query.isEmpty()) {
            binding.etQuery.setError("Type a plant name");
            return;
        }
        binding.progress.setVisibility(View.VISIBLE);
        binding.btnSearch.setEnabled(false);

        SpeciesRepository.search(this, query, (list, source) -> {
            if (isFinishing() || isDestroyed()) return;
            binding.progress.setVisibility(View.GONE);
            binding.btnSearch.setEnabled(true);
            adapter.setItems(list);

            String where = source.equals("api") ? "online"
                    : source.equals("cache") ? "offline, saved results"
                    : "offline, built-in list";
            binding.tvInfo.setText(list.isEmpty()
                    ? "No plants found. Try another name."
                    : "Tap a plant to add it (" + where + ")");
        });
    }

    private void onSpeciesPicked(SpeciesCache s) {
        final MyPlant plant = new MyPlant();
        plant.id = UUID.randomUUID().toString();
        plant.name = s.commonName;
        plant.wateringText = s.watering;
        plant.intervalDays = PlantUtils.wateringToDays(s.watering);
        plant.sunlight = s.sunlight;
        plant.lastWateredMillis = System.currentTimeMillis(); // new plant = watered today

        AppDatabase.IO.execute(() -> {
            AppDatabase.getInstance(getApplicationContext()).myPlantDao().insert(plant);
            CloudSync.upload(plant);
            runOnUiThread(() -> {
                Toast.makeText(this, plant.name + " added", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}