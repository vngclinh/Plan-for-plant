package com.example.planforplant.ui;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.planforplant.DTO.GardenScheduleResponse;
import com.example.planforplant.R;
import com.example.planforplant.model.HourGroup;
import com.example.planforplant.utils.ScheduleTypeConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private List<HourGroup> data = new ArrayList<>();

    public void setData(List<HourGroup> newData) {
        data = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule_group, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HourGroup group = data.get(position);
        holder.hourText.setText("🕓 " + group.getHour());

        StringBuilder sb = new StringBuilder();
        for (GardenScheduleResponse s : group.getSchedules()) {
            // ✅ Use converter
            String typeVi = ScheduleTypeConverter.toVietnamese(s.getType());
            String emoji = ScheduleTypeConverter.getEmoji(s.getType());

            sb.append("• ")
                    .append(emoji).append(" ")
                    .append(typeVi)
                    .append(" - ")
                    .append(s.getGardenNickname())
                    .append("\n");

            // Water info
            if (s.getWaterAmount() != null && s.getWaterAmount() > 0) {
                sb.append("   💧 Lượng nước: ")
                        .append(String.format(Locale.getDefault(), "%.1f ml", s.getWaterAmount()))
                        .append("\n");
            }

            // Fertilizer info
            if (s.getFertilityType() != null && !s.getFertilityType().isEmpty()) {
                sb.append("   🌱 Phân bón: ")
                        .append(s.getFertilityType());

                if (s.getFertilityAmount() != null && s.getFertilityAmount() > 0) {
                    sb.append(" (")
                            .append(String.format(Locale.getDefault(), "%.1f g", s.getFertilityAmount()))
                            .append(")");
                }
                sb.append("\n");
            }

            if (s.getNote() != null && !s.getNote().isEmpty()) {
                sb.append("   📝 Ghi chú: ").append(s.getNote()).append("\n");
            }

            sb.append("\n");
        }

        holder.scheduleList.setText(sb.toString().trim());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView hourText, scheduleList;
        ViewHolder(View itemView) {
            super(itemView);
            hourText = itemView.findViewById(R.id.tvHour);
            scheduleList = itemView.findViewById(R.id.tvSchedules);
        }
    }
}
