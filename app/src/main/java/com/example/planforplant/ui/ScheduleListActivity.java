package com.example.planforplant.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planforplant.DTO.GardenScheduleRequest;
import com.example.planforplant.DTO.GardenScheduleResponse;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;
import com.example.planforplant.model.HourGroup;

import java.text.SimpleDateFormat;
import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleListActivity extends NavigationBarActivity {
    private String selectedDate;
    private CalendarView calendarView;
    private RecyclerView recyclerSchedules;
    private TextView tvStatus;
    private ProgressDialog progressDialog;
    private List<GardenScheduleResponse> allSchedules = new ArrayList<>();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.care_calendar);

        calendarView = findViewById(R.id.calendarView);
        recyclerSchedules = findViewById(R.id.recyclerSchedules);
        tvStatus = findViewById(R.id.tvStatus);
        recyclerSchedules.setLayoutManager(new LinearLayoutManager(this));

        loadSchedules();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            showSchedulesGrouped(selectedDate);
        });
    }

    /** Load toàn bộ kế hoạch của người dùng */
    private void loadSchedules() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang tải danh sách kế hoạch...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);
        api.getAllSchedules().enqueue(new Callback<List<GardenScheduleResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<GardenScheduleResponse>> call,
                                   @NonNull Response<List<GardenScheduleResponse>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    allSchedules = response.body();

                    // Nếu chưa chọn ngày, mặc định hôm nay
                    if (selectedDate == null) {
                        selectedDate = dateFormat.format(new Date());
                        calendarView.setDate(System.currentTimeMillis(), false, true);
                    } else {
                        // Giữ nguyên ngày đã chọn
                        try {
                            Date date = dateFormat.parse(selectedDate);
                            if (date != null) {
                                calendarView.setDate(date.getTime(), false, true);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    showSchedulesGrouped(selectedDate);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GardenScheduleResponse>> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(ScheduleListActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** 🔹 Gom các kế hoạch theo khung giờ trong ngày */
    private void showSchedulesGrouped(String date) {
        List<GardenScheduleResponse> filtered = new ArrayList<>();
        for (GardenScheduleResponse s : allSchedules) {
            if (s.getScheduledTime() != null && s.getScheduledTime().startsWith(date)) {
                filtered.add(s);
            }
        }

        if (filtered.isEmpty()) {
            recyclerSchedules.setAdapter(null);
            tvStatus.setText("🌫️ Không có kế hoạch cho ngày này");
            tvStatus.setTextColor(Color.parseColor("#9E9E9E"));
            return;
        }

        Map<String, List<GardenScheduleResponse>> grouped = new TreeMap<>();
        for (GardenScheduleResponse s : filtered) {
            try {
                Date time = timeFormat.parse(s.getScheduledTime());
                Calendar c = Calendar.getInstance();
                c.setTime(time);
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                String key = String.format("%02d:%02d", hour, minute); // ✅ hiển thị cả giờ và phút
                grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<HourGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<GardenScheduleResponse>> entry : grouped.entrySet()) {
            groups.add(new HourGroup(entry.getKey(), entry.getValue()));
        }

        ScheduleGroupAdapter groupAdapter = new ScheduleGroupAdapter(groups, new ScheduleAdapter.ScheduleListener() {
            @Override
            public void onItemClick(GardenScheduleResponse schedule) {}

            @Override
            public void onEdit(GardenScheduleResponse schedule) {
                showEditPopup(schedule);
            }

            @Override
            public void onDelete(GardenScheduleResponse schedule) {
                new AlertDialog.Builder(ScheduleListActivity.this)
                        .setTitle("Xóa kế hoạch")
                        .setMessage("Bạn có chắc muốn xóa kế hoạch này?")
                        .setPositiveButton("Xóa", (d, w) -> deleteSchedule(schedule.getId()))
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        recyclerSchedules.setAdapter(groupAdapter);
        tvStatus.setText("Kế hoạch chi tiết");
    }

    /** Popup sửa kế hoạch */
    private void showEditPopup(GardenScheduleResponse schedule) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_schedule, null);
        EditText et1 = dialogView.findViewById(R.id.etInput1);
        EditText et2 = dialogView.findViewById(R.id.etInput2);
        TextView l1 = dialogView.findViewById(R.id.tvLabel1);
        TextView l2 = dialogView.findViewById(R.id.tvLabel2);

        switch (schedule.getType().toUpperCase(Locale.ROOT)) {
            case "WATERING":
                l1.setText("Lượng nước (ml)");
                et1.setText(schedule.getWaterAmount() != null ? schedule.getWaterAmount().toString() : "");
                l2.setVisibility(View.GONE);
                et2.setVisibility(View.GONE);
                break;

            case "FERTILIZING":
                l1.setText("Loại phân");
                et1.setText(schedule.getFertilityType() != null ? schedule.getFertilityType() : "");
                l2.setText("Lượng phân (ml/g)");
                et2.setText(schedule.getFertilityAmount() != null ? schedule.getFertilityAmount().toString() : "");
                break;

            case "NOTE":
                l1.setText("Nội dung ghi chú");
                et1.setText(schedule.getNote() != null ? schedule.getNote() : "");
                l2.setVisibility(View.GONE);
                et2.setVisibility(View.GONE);
                break;
        }

        new AlertDialog.Builder(this)
                .setTitle("Chỉnh sửa kế hoạch")
                .setView(dialogView)
                .setPositiveButton("Lưu", (d, w) -> saveChanges(schedule, et1.getText().toString(), et2.getText().toString()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    /** Gửi yêu cầu cập nhật kế hoạch */
    private void saveChanges(GardenScheduleResponse schedule, String val1, String val2) {
        GardenScheduleRequest req = new GardenScheduleRequest();
        req.setGardenId(schedule.getGardenId());
        req.setType(schedule.getType());
        req.setScheduledTime(schedule.getScheduledTime());
        req.setCompletion(schedule.getCompletion());

        switch (schedule.getType().toUpperCase(Locale.ROOT)) {
            case "WATERING":
                try {
                    req.setWaterAmount(Double.parseDouble(val1));
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Giá trị không hợp lệ!", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case "FERTILIZING":
                req.setFertilityType(val1);
                try {
                    req.setFertilityAmount(Double.parseDouble(val2));
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Giá trị không hợp lệ!", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case "NOTE":
                req.setNote(val1);
                break;
        }

        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);
        api.updateSchedule(schedule.getId(), req).enqueue(new Callback<GardenScheduleResponse>() {
            @Override
            public void onResponse(@NonNull Call<GardenScheduleResponse> call, @NonNull Response<GardenScheduleResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ScheduleListActivity.this, "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
                    loadSchedules();
                } else {
                    Toast.makeText(ScheduleListActivity.this, "Không thể cập nhật kế hoạch", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GardenScheduleResponse> call, @NonNull Throwable t) {
                Toast.makeText(ScheduleListActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** 🔹 Xóa kế hoạch */
    private void deleteSchedule(Long id) {
        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);
        api.deleteSchedule(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ScheduleListActivity.this, "Đã xóa kế hoạch", Toast.LENGTH_SHORT).show();
                    loadSchedules();
                } else {
                    Toast.makeText(ScheduleListActivity.this, "Không thể xóa kế hoạch", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(ScheduleListActivity.this, "Lỗi khi xóa kế hoạch", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
