package com.example.planforplant.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planforplant.DTO.GardenScheduleResponse;
import com.example.planforplant.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Locale;

/**
 * Adapter hiển thị danh sách kế hoạch được nhóm theo ngày.
 * Nếu có nhiều kế hoạch cùng ngày → chỉ hiển thị bản cập nhật mới nhất của mỗi loại.
 */
public class ScheduleListGroupedAdapter extends RecyclerView.Adapter<ScheduleListGroupedAdapter.ViewHolder> {

    private final List<GroupedSchedule> groupedSchedules;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String scheduledTime);
    }

    public ScheduleListGroupedAdapter(List<GroupedSchedule> groupedSchedules, OnItemClickListener listener) {
        this.groupedSchedules = filterLatestByType(groupedSchedules);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupedSchedule group = groupedSchedules.get(position);
        holder.tvTime.setText("📅 " + group.getTime());

        StringBuilder details = new StringBuilder();
        for (GardenScheduleResponse s : group.getItems()) {
            details.append("• ").append(translateType(s.getType())).append("\n");
        }
        holder.tvDetails.setText(details.toString().trim());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(group.getTime());
        });
    }

    @Override
    public int getItemCount() {
        return groupedSchedules.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDetails = itemView.findViewById(R.id.tvDetails);
        }
    }

    /** Giữ bản cập nhật mới nhất cho mỗi loại trong cùng ngày */
    private List<GroupedSchedule> filterLatestByType(List<GroupedSchedule> originalGroups) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        List<GroupedSchedule> filtered = new ArrayList<>();

        for (GroupedSchedule group : originalGroups) {
            Map<String, GardenScheduleResponse> latestByType = new LinkedHashMap<>();

            for (GardenScheduleResponse s : group.getItems()) {
                if (s.getType() == null) continue;
                String type = s.getType();

                if (!latestByType.containsKey(type)) {
                    latestByType.put(type, s);
                } else {
                    GardenScheduleResponse existing = latestByType.get(type);
                    Date existingDate = parseDate(existing.getUpdatedAt(), existing.getCreatedAt(), sdf);
                    Date currentDate = parseDate(s.getUpdatedAt(), s.getCreatedAt(), sdf);

                    if (existingDate == null || (currentDate != null && currentDate.after(existingDate))) {
                        latestByType.put(type, s);
                    }
                }
            }

            filtered.add(new GroupedSchedule(group.getTime(), new ArrayList<>(latestByType.values())));
        }

        return filtered;
    }

    private Date parseDate(Object updatedAt, Object createdAt, SimpleDateFormat sdf) {
        String dateStr = null;
        if (updatedAt != null) dateStr = updatedAt.toString();
        else if (createdAt != null) dateStr = createdAt.toString();
        if (dateStr == null) return null;

        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    private String translateType(String type) {
        if (type == null) return "(Không rõ)";
        switch (type.toUpperCase(Locale.ROOT)) {
            case "WATERING": return "Tưới nước 💧";
            case "FERTILIZING": return "Bón phân 🌱";
            case "PRUNING": return "Tỉa lá ✂️";
            case "NOTE": return "Ghi chú 📝";
            default: return type;
        }
    }
}
