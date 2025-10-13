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

import java.util.ArrayList;
import java.util.List;

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
            sb.append("• ")
                    .append(s.getType())
                    .append(" - ")
                    .append(s.getGardenNickname())
                    .append("\n");
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
