package com.example.planforplant.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planforplant.DTO.GardenScheduleResponse;
import com.example.planforplant.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private final Context context;
    private final List<GardenScheduleResponse> schedules;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public ScheduleAdapter(Context context, List<GardenScheduleResponse> schedules) {
        this.context = context;
        this.schedules = schedules;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GardenScheduleResponse item = schedules.get(position);

        holder.tvType.setText("Hoạt động: " + item.getType());
        holder.tvTime.setText("🕒 " + item.getScheduledTime());
        holder.tvStatus.setText("Trạng thái: " + getStatusLabel(item.getCompletion()));
        holder.tvNote.setText("Ghi chú: " + (item.getNote() == null ? "(không có)" : item.getNote()));
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvTime, tvStatus, tvNote;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvType);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvNote = itemView.findViewById(R.id.tvNote);
        }
    }

    private String getStatusLabel(String status) {
        switch (status) {
            case "Done": return "✅ Đã hoàn thành";
            case "Skipped": return "🍂 Bỏ qua";
            case "NotDone": return "🌱 Chưa thực hiện";
            default: return "Không rõ";
        }
    }
}
