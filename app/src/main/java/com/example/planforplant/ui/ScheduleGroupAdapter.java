package com.example.planforplant.ui;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planforplant.DTO.GardenScheduleResponse;
import com.example.planforplant.R;
import com.example.planforplant.model.HourGroup;

import java.util.List;

public class ScheduleGroupAdapter extends RecyclerView.Adapter<ScheduleGroupAdapter.GroupViewHolder> {

    private final List<HourGroup> hourGroups;
    private final ScheduleAdapter.ScheduleListener listener;

    public ScheduleGroupAdapter(List<HourGroup> hourGroups, ScheduleAdapter.ScheduleListener listener) {
        this.hourGroups = hourGroups;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule_group, parent, false);
        return new GroupViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        HourGroup group = hourGroups.get(position);
        holder.tvHour.setText("🕒 " + group.getHour());

        ScheduleAdapter adapter = new ScheduleAdapter(group.getSchedules(), listener);
        holder.recyclerSchedulesHour.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.recyclerSchedulesHour.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return hourGroups.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvHour;
        RecyclerView recyclerSchedulesHour;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHour = itemView.findViewById(R.id.tvHour);
            recyclerSchedulesHour = itemView.findViewById(R.id.recyclerSchedulesHour);
        }
    }
}
