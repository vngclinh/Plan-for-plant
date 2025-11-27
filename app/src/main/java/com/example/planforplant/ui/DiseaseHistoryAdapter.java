package com.example.planforplant.ui;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.planforplant.DTO.GardenDiseaseResponse;
import com.example.planforplant.R;

import java.util.ArrayList;
import java.util.List;

public class DiseaseHistoryAdapter extends RecyclerView.Adapter<DiseaseHistoryAdapter.ViewHolder> {

    private List<GardenDiseaseResponse> list = new ArrayList<>();
    private final Context context;

    public DiseaseHistoryAdapter(Context ctx) {
        this.context = ctx;
    }

    public void setData(List<GardenDiseaseResponse> data) {
        this.list = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_disease_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GardenDiseaseResponse d = list.get(position);

        holder.tvName.setText("🦠 " + d.getDiseaseName());
        holder.tvStatus.setText("Trạng thái: " + d.getStatus());

        holder.tvDetectedDate.setText("Ngày mắc: " + d.getDetectedDate().substring(0, 10));

        if (d.getCuredDate() != null) {
            holder.tvCuredDate.setVisibility(View.VISIBLE);
            holder.tvCuredDate.setText("Ngày khỏi: " + d.getCuredDate().substring(0, 10));
        } else {
            holder.tvCuredDate.setVisibility(View.GONE);
        }

        if ("CURED".equalsIgnoreCase(d.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#388E3C")); // Green
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#D32F2F")); // Red
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvDetectedDate, tvCuredDate, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDiseaseName);
            tvDetectedDate = itemView.findViewById(R.id.tvDetectedDate);
            tvCuredDate = itemView.findViewById(R.id.tvCuredDate);
            tvStatus = itemView.findViewById(R.id.tvDiseaseStatus);
        }
    }
}
