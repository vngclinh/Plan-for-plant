package com.example.planforplant.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planforplant.DTO.GardenScheduleResponse;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;

import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleListActivity extends AppCompatActivity {

    private RecyclerView recyclerSchedules;
    private ProgressDialog progressDialog;
    private ScheduleListGroupedAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_list);

        recyclerSchedules = findViewById(R.id.recyclerSchedules);
        recyclerSchedules.setLayoutManager(new LinearLayoutManager(this));

        loadSchedules();
    }

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
                    List<GardenScheduleResponse> schedules = response.body();
                    List<GroupedSchedule> grouped = groupByDay(schedules);
                    adapter = new ScheduleListGroupedAdapter(grouped, scheduledTime -> {
                        ScheduleDetailActivity.start(ScheduleListActivity.this, scheduledTime);
                    });
                    recyclerSchedules.setAdapter(adapter);
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

    /** 🔹 Gom nhóm kế hoạch theo NGÀY (yyyy-MM-dd) */
    private List<GroupedSchedule> groupByDay(List<GardenScheduleResponse> schedules) {
        Map<String, List<GardenScheduleResponse>> map = new LinkedHashMap<>();

        for (GardenScheduleResponse s : schedules) {
            if (s.getScheduledTime() == null) continue;
            String key = s.getScheduledTime().substring(0, 10); // chỉ lấy ngày
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
        }

        List<GroupedSchedule> result = new ArrayList<>();
        for (Map.Entry<String, List<GardenScheduleResponse>> entry : map.entrySet()) {
            result.add(new GroupedSchedule(entry.getKey(), entry.getValue()));
        }

        return result;
    }
}
