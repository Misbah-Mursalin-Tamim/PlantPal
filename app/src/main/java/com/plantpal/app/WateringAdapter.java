package com.plantpal.app;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.plantpal.app.data.MyPlant;
import com.plantpal.app.databinding.ItemWateringBinding;

import java.util.ArrayList;
import java.util.List;

public class WateringAdapter extends RecyclerView.Adapter<WateringAdapter.VH> {

    // One row of the checklist. originalLast lets us undo a tick.
    public static class Item {
        public final MyPlant plant;
        public final long originalLast;
        public final String statusAtStart;
        public boolean done = false;

        public Item(MyPlant plant) {
            this.plant = plant;
            this.originalLast = plant.lastWateredMillis;
            this.statusAtStart = PlantUtils.statusText(PlantUtils.daysUntilDue(plant));
        }
    }

    public interface Listener {
        void onToggle(Item item, boolean checked);
    }

    private final List<Item> items = new ArrayList<>();
    private final Listener listener;

    public WateringAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Item> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemWateringBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        final Item item = items.get(position);
        holder.binding.cbWatered.setText(item.plant.name + "  —  " + item.statusAtStart);
        holder.binding.cbWatered.setChecked(item.done);
        holder.binding.cbWatered.setOnClickListener(v -> {
            boolean checked = holder.binding.cbWatered.isChecked();
            item.done = checked;
            listener.onToggle(item, checked);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemWateringBinding binding;

        VH(ItemWateringBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}