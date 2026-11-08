package com.plantpal.app;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.plantpal.app.data.SpeciesCache;
import com.plantpal.app.databinding.ItemSpeciesBinding;

import java.util.ArrayList;
import java.util.List;

public class SpeciesAdapter extends RecyclerView.Adapter<SpeciesAdapter.VH> {

    public interface OnSpeciesClickListener {
        void onSpeciesClick(SpeciesCache species);
    }

    private final List<SpeciesCache> items = new ArrayList<>();
    private final OnSpeciesClickListener listener;

    public SpeciesAdapter(OnSpeciesClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<SpeciesCache> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemSpeciesBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        SpeciesCache s = items.get(position);
        holder.binding.tvSpName.setText(s.commonName);
        holder.binding.tvSpDetail.setText(s.scientificName + "  •  Water every "
                + PlantUtils.wateringToDays(s.watering) + " days");
        holder.itemView.setOnClickListener(v -> listener.onSpeciesClick(s));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemSpeciesBinding binding;

        VH(ItemSpeciesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}