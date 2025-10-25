package com.example.planforplant.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planforplant.DTO.GardenScheduleResponse;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;

import java.text.SimpleDateFormat;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleListActivity extends NavigationBarActivity {

    private CalendarView calendarView;
    private RecyclerView recyclerSchedules;
    private ProgressDialog progressDialog;
    private ScheduleAdapter adapter;
    private List<GardenScheduleResponse> allSchedules = new ArrayList<>();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_list);

        calendarView = findViewById(R.id.calendarView);
        recyclerSchedules = findViewById(R.id.recyclerSchedules);
        recyclerSchedules.setLayoutManager(new LinearLayoutManager(this));

        loadSchedules();

        // Khi chọn ngày
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            showSchedulesForDate(selectedDate);
        });
    }

    /** Tải toàn bộ kế hoạch */
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

                    // Hiển thị lịch hôm nay trước
                    String today = dateFormat.format(new Date());
                    showSchedulesForDate(today);
                    calendarView.setDate(System.currentTimeMillis(), false, true);
                } else {
                    Toast.makeText(ScheduleListActivity.this, "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GardenScheduleResponse>> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(ScheduleListActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Lọc & hiển thị kế hoạch của ngày được chọn */
    private void showSchedulesForDate(String date) {
        List<GardenScheduleResponse> filtered = new ArrayList<>();
        for (GardenScheduleResponse s : allSchedules) {
            if (s.getScheduledTime() != null && s.getScheduledTime().startsWith(date)) {
                filtered.add(s);
            }
        }

        if (filtered.isEmpty()) {
            recyclerSchedules.setAdapter(null);
            Toast.makeText(this, "🌿 Không có lịch cho ngày " + date, Toast.LENGTH_SHORT).show();
            return;
        }

        // Giữ bản mới nhất mỗi loại
        Map<String, GardenScheduleResponse> latestByType = new LinkedHashMap<>();
        for (GardenScheduleResponse s : filtered) {
            if (s.getType() == null) continue;
            String type = s.getType();

            if (!latestByType.containsKey(type)) {
                latestByType.put(type, s);
            } else {
                GardenScheduleResponse existing = latestByType.get(type);
                if (s.getUpdatedAt() != null && existing.getUpdatedAt() != null
                        && s.getUpdatedAt().compareTo(existing.getUpdatedAt()) > 0) {
                    latestByType.put(type, s);
                }
            }
        }

        List<GardenScheduleResponse> latestList = new ArrayList<>(latestByType.values());

        // Hiển thị từng loại dưới dạng card riêng
        adapter = new ScheduleAdapter(latestList, schedule ->
                ScheduleDetailActivity.start(ScheduleListActivity.this, schedule.getScheduledTime()));
        recyclerSchedules.setAdapter(adapter);
    }
}
