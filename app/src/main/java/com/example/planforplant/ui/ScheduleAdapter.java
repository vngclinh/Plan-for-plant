package com.example.planforplant.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planforplant.DTO.GardenScheduleResponse;
import com.example.planforplant.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private final List<GardenScheduleResponse> schedules;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(GardenScheduleResponse schedule);
    }

    public ScheduleAdapter(List<GardenScheduleResponse> schedules, OnItemClickListener listener) {
        this.schedules = schedules;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GardenScheduleResponse s = schedules.get(position);

        holder.tvType.setText(translateType(s.getType()));
        holder.tvPlant.setText("🌿 Cây: " + (s.getPlantName() != null ? s.getPlantName() : "Không rõ"));
        holder.tvTime.setText("🕒 " + formatTime(s.getScheduledTime()));
        holder.tvNote.setText(s.getNote() != null ? s.getNote() : "(Không có ghi chú)");

        holder.itemView.setOnClickListener(v -> listener.onItemClick(s));
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvPlant, tvTime, tvNote;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardSchedule);
            tvType = itemView.findViewById(R.id.tvType);
            tvPlant = itemView.findViewById(R.id.tvPlant);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvNote = itemView.findViewById(R.id.tvNote);
        }
    }

    private String translateType(String type) {
        if (type == null) return "(Không rõ)";
        switch (type.toUpperCase(Locale.ROOT)) {
            case "WATERING": return "💧 Tưới nước";
            case "FERTILIZING": return "🌱 Bón phân";
            case "PRUNING": return "✂️ Tỉa lá";
            case "NOTE": return "📝 Ghi chú";
            default: return type;
        }
    }

    private String formatTime(String timeStr) {
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
            return out.format(in.parse(timeStr));
        } catch (Exception e) {
            return timeStr;
        }
    }
}
