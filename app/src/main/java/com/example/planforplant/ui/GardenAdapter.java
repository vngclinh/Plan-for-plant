package com.example.planforplant.ui;

import android.app.Activity;
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_garden_detail, parent, false);
        return new GardenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GardenViewHolder holder, int position) {
        GardenResponse item = gardenList.get(position);

        // --- Tên cây ---
        if (item.getNickname() != null) {
            holder.tvPlantName.setText(
                    item.getNickname() != null
                            ? item.getNickname()
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
        if(item.getStatus().equals("ALIVE")){
            holder.tvStatus.setText("Trạng thái: Đang phát triển");
        } else if (item.getStatus().equals("DEAD")){
            holder.tvStatus.setText("Trạng thái: Cây đã chết");
        } else {
            holder.tvStatus.setText("Trạng thái: Không xác định");
        }

        // --- Ảnh ---
        if (item.getPlant() != null && item.getPlant().getImageUrl() != null) {
            Glide.with(context)
                    .load(item.getPlant().getImageUrl())
                    .placeholder(R.drawable.ic_plant)
                    .into(holder.imgPlant);
        } else {
            holder.imgPlant.setImageResource(R.drawable.ic_plant);
        }
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, GardenDetailActivity.class);

            // Gửi dữ liệu cây sang GardenDetailActivity
            intent.putExtra("gardenId", item.getId());
            intent.putExtra("plant", item.getPlant());
            intent.putExtra("nickname", item.getNickname());
            intent.putExtra("status", item.getStatus());
            intent.putExtra("imageUrl", item.getPlant().getImageUrl());
            intent.putExtra("dateAdded", item.getDateAdded());

            // Mở bằng startActivityForResult để có thể nhận kết quả xoá
            ((Activity) context).startActivityForResult(intent, 100);
        });
    }


    //hàm này để Recycleview biết tong so phan tu trong danh sach
    @Override
    public int getItemCount() {
        return gardenList != null ? gardenList.size() : 0;
    }

    static class GardenViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlant;
        TextView tvPlantName, tvDateAdded, tvStatus;

        public GardenViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlant = itemView.findViewById(R.id.ivPlant);
            tvPlantName = itemView.findViewById(R.id.tvPlantName);
            tvDateAdded = itemView.findViewById(R.id.tvDateAdded);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
