package com.example.planforplant.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.planforplant.R;
import com.example.planforplant.DTO.GardenImageResponse;

import java.util.ArrayList;
import java.util.List;

public class GardenImageAdapter extends RecyclerView.Adapter<GardenImageAdapter.ViewHolder> {

    private final Context context;
    private final List<GardenImageResponse> images;
    private final List<Long> selectedIds = new ArrayList<>();
    private boolean selectionMode = false;

    private final OnSelectionChangedListener listener;
    private final OnImageClickListener imageClickListener;

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int count);
    }

    public interface OnImageClickListener {
        void onImageClick(GardenImageResponse image);
    }

    public GardenImageAdapter(Context context, List<GardenImageResponse> images,
                              OnSelectionChangedListener listener,
                              OnImageClickListener imageClickListener) {
        this.context = context;
        this.images = images;
        this.listener = listener;
        this.imageClickListener = imageClickListener;
    }

    public void setData(List<GardenImageResponse> list) {
        images.clear();
        images.addAll(list);
        selectedIds.clear();
        selectionMode = false;
        notifyDataSetChanged();
    }
    public List<Long> getSelectedIds() {
        return new ArrayList<>(selectedIds);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_garden_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GardenImageResponse image = images.get(position);

        Glide.with(context)
                .load(image.getImageUrl())
                .centerCrop()
                .into(holder.imgThumb);

        boolean isSelected = selectedIds.contains(image.getId());
        holder.viewScrim.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        holder.imgChecked.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        // --- CLICK ---
        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                toggleSelection(image, position);
            } else {
                // Click bình thường -> xem ảnh
                if (imageClickListener != null) {
                    imageClickListener.onImageClick(image);
                }
            }
        });

        // --- GIỮ LÂU ---
        holder.itemView.setOnLongClickListener(v -> {
            if (!selectionMode) {
                selectionMode = true;
                toggleSelection(image, position);
            }
            return true;
        });
    }

    private void toggleSelection(GardenImageResponse image, int position) {
        if (selectedIds.contains(image.getId())) {
            selectedIds.remove(image.getId());
        } else {
            selectedIds.add(image.getId());
        }

        notifyItemChanged(position);

        // Nếu không còn ảnh nào được chọn -> thoát chế độ chọn
        if (selectedIds.isEmpty()) {
            selectionMode = false;
        }

        if (listener != null) {
            listener.onSelectionChanged(selectedIds.size());
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb, imgChecked;
        View viewScrim;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.imgThumb);
            imgChecked = itemView.findViewById(R.id.imgChecked);
            viewScrim = itemView.findViewById(R.id.viewScrim);
        }
    }
}
