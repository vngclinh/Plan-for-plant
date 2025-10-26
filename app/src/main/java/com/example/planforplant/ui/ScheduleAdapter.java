package com.example.planforplant.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planforplant.DTO.GardenScheduleRequest;
import com.example.planforplant.DTO.GardenScheduleResponse;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private final List<GardenScheduleResponse> schedules;
    private final ScheduleListener listener;

    public interface ScheduleListener {
        void onItemClick(GardenScheduleResponse schedule);
        void onEdit(GardenScheduleResponse schedule);
        void onDelete(GardenScheduleResponse schedule);
    }

    public ScheduleAdapter(List<GardenScheduleResponse> schedules, ScheduleListener listener) {
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

        holder.tvTitle.setText(buildTitle(s));
        holder.tvPlantName.setText(s.getPlantName() != null
                ? "🌿 " + s.getPlantName()
                : "🌿 Không rõ");

        // hiển thị trạng thái checkbox & màu nền
        boolean isDone = s.getCompletion() != null &&
                (s.getCompletion().toString().equalsIgnoreCase("Complete")
                        || s.getCompletion().toString().equalsIgnoreCase("Done"));

        holder.cbDone.setChecked(isDone);
        holder.cardSchedule.setCardBackgroundColor(isDone
                ? Color.parseColor("#A5D6A7")   // xanh lá nhạt
                : Color.parseColor("#CCFFE082")); // vàng nhạt

        // Khi tick / bỏ tick -> gửi API cập nhật completion
        holder.cbDone.setOnCheckedChangeListener((buttonView, checked) -> {
            if (buttonView.isPressed()) { // tránh trigger khi bind
                updateCompletion(holder, s, checked);
            }
        });

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(s));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(s));
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardSchedule;
        TextView tvTitle, tvPlantName;
        ImageButton btnEdit, btnDelete;
        CheckBox cbDone;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardSchedule = itemView.findViewById(R.id.cardSchedule);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPlantName = itemView.findViewById(R.id.tvPlantName);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            cbDone = itemView.findViewById(R.id.cbDone);
        }
    }

    /** Cập nhật trạng thái completion */
    private void updateCompletion(ViewHolder holder, GardenScheduleResponse s, boolean done) {
        ApiService api = ApiClient.getLocalClient(holder.itemView.getContext()).create(ApiService.class);

        GardenScheduleRequest req = new GardenScheduleRequest();
        req.setGardenId(s.getGardenId());
        req.setType(s.getType());
        req.setScheduledTime(s.getScheduledTime());
        req.setCompletion(done ? "Complete" : "NotDone");
        req.setNote(s.getNote());
        req.setWaterAmount(s.getWaterAmount());
        req.setFertilityType(s.getFertilityType());
        req.setFertilityAmount(s.getFertilityAmount());

        api.updateSchedule(s.getId(), req).enqueue(new Callback<GardenScheduleResponse>() {
            @Override
            public void onResponse(@NonNull Call<GardenScheduleResponse> call,
                                   @NonNull Response<GardenScheduleResponse> response) {
                if (response.isSuccessful()) {
                    holder.cardSchedule.setCardBackgroundColor(done
                            ? Color.parseColor("#CCAAF0D1")
                            : Color.parseColor("#CCFFE082"));
                    Toast.makeText(holder.itemView.getContext(),
                            done ? "Đã đánh dấu hoàn thành 🌿" : "Đã bỏ đánh dấu",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(),
                            "Không thể cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GardenScheduleResponse> call, @NonNull Throwable t) {
                Toast.makeText(holder.itemView.getContext(),
                        "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Tạo chuỗi tiêu đề: loại kế hoạch + thông tin chi tiết */
    private String buildTitle(GardenScheduleResponse s) {
        String type = translateType(s.getType());
        String detail = "";

        if ("WATERING".equalsIgnoreCase(s.getType()) && s.getWaterAmount() != null) {
            detail = s.getWaterAmount() + "ml";
        } else if ("FERTILIZING".equalsIgnoreCase(s.getType()) && s.getFertilityType() != null) {
            detail = s.getFertilityType() + " (" + s.getFertilityAmount() + "ml/g)";
        } else if (s.getNote() != null && !s.getNote().trim().isEmpty()) {
            detail = s.getNote();
        }

        return type + (detail.isEmpty() ? "" : ": " + detail);
    }

    private String translateType(String type) {
        if (type == null) return "(Không rõ)";
        switch (type.toUpperCase(Locale.ROOT)) {
            case "WATERING": return "💧 Tưới nước";
            case "FERTILIZING": return "🌱 Bón phân";
            case "NOTE": return "📝 Ghi chú";
            default: return type;
        }
    }
}
