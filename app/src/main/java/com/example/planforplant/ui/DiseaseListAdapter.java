package com.example.planforplant.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.planforplant.R;
import com.example.planforplant.model.Disease;

import java.util.List;

public class DiseaseListAdapter extends RecyclerView.Adapter<DiseaseListAdapter.ViewHolder> {

    private final List<Disease> list;
    private final Context context;
    private final OnDiseaseClickListener listener;

    // Interface callback click
    public interface OnDiseaseClickListener {
        void onDiseaseClick(Disease disease);
    }

    public DiseaseListAdapter(Context context, List<Disease> list, OnDiseaseClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DiseaseListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_health_disease, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DiseaseListAdapter.ViewHolder holder, int position) {
        Disease d = list.get(position);
        holder.tvName.setText("🦠 " + d.getName());

        holder.itemView.setOnClickListener(v -> listener.onDiseaseClick(d));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDiseaseName);
        }
    }
}