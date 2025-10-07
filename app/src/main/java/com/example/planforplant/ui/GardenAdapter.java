package com.example.planforplant.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.planforplant.DTO.GardenResponse;
import com.example.planforplant.R;

import java.util.List;

public class GardenAdapter extends RecyclerView.Adapter<GardenAdapter.GardenViewHolder> {

    private final Context context;
    private final List<GardenResponse> gardenList;

    public GardenAdapter(Context context, List<GardenResponse> gardenList) {
        this.context = context;
        this.gardenList = gardenList;
    }

    @NonNull
    @Override
    public GardenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_garden_plant, parent, false);
        return new GardenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GardenViewHolder holder, int position) {
        GardenResponse item = gardenList.get(position);

        // --- Tên cây ---
        if (item.getPlant() != null) {
            holder.tvPlantName.setText(
                    item.getPlant().getCommonName() != null
                            ? item.getPlant().getCommonName()
                            : "Không rõ tên cây"
            );
        } else {
            holder.tvPlantName.setText("Không rõ tên cây");
        }

        // --- Ngày thêm ---
        String date = (item.getDateAdded() != null)
                ? item.getDateAdded().substring(0, 10)
                : "Không rõ";
        holder.tvDateAdded.setText("Ngày thêm: " + date);

        // --- Trạng thái ---
        holder.tvStatus.setText("Trạng thái: " + (item.getStatus() != null ? item.getStatus() : "Không xác định"));

        // --- Ảnh ---
        if (item.getPlant() != null && item.getPlant().getImageUrl() != null) {
            Glide.with(context)
                    .load(item.getPlant().getImageUrl())
                    .placeholder(R.drawable.ic_plant)
                    .into(holder.imgPlant);
        } else {
            holder.imgPlant.setImageResource(R.drawable.ic_plant);
        }
    }


    @Override
    public int getItemCount() {
        return gardenList != null ? gardenList.size() : 0;
    }

    static class GardenViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlant;
        TextView tvPlantName, tvDateAdded, tvStatus;

        public GardenViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlant = itemView.findViewById(R.id.imgPlant);
            tvPlantName = itemView.findViewById(R.id.tvPlantName);
            tvDateAdded = itemView.findViewById(R.id.tvDateAdded);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
