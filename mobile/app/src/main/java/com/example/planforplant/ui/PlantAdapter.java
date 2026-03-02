package com.example.planforplant.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.planforplant.R;
import com.example.planforplant.model.Plant;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {

    private final Context context;
    private List<Plant> plantList = new ArrayList<>();

    public PlantAdapter(Context context) {
        this.context = context;
    }

    public void setPlants(List<Plant> plants) {
        this.plantList = plants;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_plant, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = plantList.get(position);
        holder.tvPlantName.setText(plant.getCommonName());
        holder.tvPlantDesc.setText(plant.getDescription());

        if (plant.getImageUrl() != null && !plant.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(plant.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.imgPlant);
        } else {
            holder.imgPlant.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("plantEntityJson", new Gson().toJson(plant));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return plantList.size();
    }

    public static class PlantViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlant;
        TextView tvPlantName, tvPlantDesc;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlant = itemView.findViewById(R.id.imgPlant);
            tvPlantName = itemView.findViewById(R.id.tvPlantName);
            tvPlantDesc = itemView.findViewById(R.id.tvPlantDesc);
        }
    }
}
