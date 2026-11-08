package com.plantpal.app;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.plantpal.app.data.MyPlant;
import com.plantpal.app.databinding.ItemPlantBinding;

import java.util.ArrayList;
import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {

    // MainActivity implements this to react when a card is tapped
    public interface OnPlantClickListener {
        void onPlantClick(MyPlant plant);
    }

    private final List<MyPlant> plants = new ArrayList<>();
    private final OnPlantClickListener listener;

    public PlantAdapter(OnPlantClickListener listener) {
        this.listener = listener;
    }

    public void setPlants(List<MyPlant> newPlants) {
        plants.clear();
        plants.addAll(newPlants);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPlantBinding itemBinding = ItemPlantBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PlantViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        MyPlant plant = plants.get(position);
        int days = PlantUtils.daysUntilDue(plant);

        holder.binding.tvName.setText(plant.name);
        holder.binding.tvNext.setText("Next watering: "
                + PlantUtils.formatDate(PlantUtils.nextWateringMillis(plant)));
        holder.binding.tvStatus.setText(PlantUtils.statusText(days));



        // Red = overdue, orange = today, green = later
        int color;
        if (days < 0) {
            color = Color.parseColor("#C62828");
        } else if (days == 0) {
            color = Color.parseColor("#EF6C00");
        } else {
            color = Color.parseColor("#2E7D32");
        }
        holder.binding.tvStatus.setTextColor(color);

        // Drop level: 1.0 = just watered, 0.0 = due or overdue
        float level = 0f;
        if (plant.intervalDays > 0 && days > 0) {
            level = days / (float) plant.intervalDays;
        }
        holder.binding.thirstView.setLevel(level);

        holder.itemView.setOnClickListener(v -> listener.onPlantClick(plant));
    }

    @Override
    public int getItemCount() {
        return plants.size();
    }

    static class PlantViewHolder extends RecyclerView.ViewHolder {
        final ItemPlantBinding binding;

        PlantViewHolder(ItemPlantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}