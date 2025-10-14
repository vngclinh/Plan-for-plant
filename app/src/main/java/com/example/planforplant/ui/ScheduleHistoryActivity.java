package com.example.planforplant.ui;

import android.content.Intent;
import android.os.Bundle;

import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planforplant.DTO.GardenScheduleResponse;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;
import com.example.planforplant.model.HourGroup;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleHistoryActivity extends AppCompatActivity {
    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private ScheduleAdapter adapter;
    private ApiService apiService;
    private String token;

    private String lastSelectedDate = "";

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.care_calendar); // your XML

        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ScheduleAdapter();
        recyclerView.setAdapter(adapter);

        apiService = ApiClient.getLocalClient(this).create(ApiService.class);
        token = getSharedPreferences("MyApp", MODE_PRIVATE)
                .getString("token", "");

        Calendar calendar = Calendar.getInstance();
        String today = String.format(Locale.US, "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));

        lastSelectedDate = today;
        loadSchedules(today);

        // ✅ When user selects a date
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            loadSchedules(selectedDate);
        });

        TextView tabHistory = findViewById(R.id.tab_history);
        TextView tabPlan = findViewById(R.id.tab_plan);

        tabPlan.setOnClickListener(v -> {
            Intent intent = new Intent(ScheduleHistoryActivity.this, CreatePlanActivity.class);
            startActivity(intent);
            finish();
        });

        tabHistory.setBackgroundResource(R.drawable.bg_tab_selected);
        tabHistory.setTextColor(getColor(R.color.white));

        tabPlan.setBackgroundResource(R.drawable.bg_tab_unselected);
        tabPlan.setTextColor(getColor(R.color.text_secondary));
    }

    private void loadSchedules(String date) {
        Call<List<GardenScheduleResponse>> call =
                apiService.getSchedulesByDate(date);

        call.enqueue(new Callback<List<GardenScheduleResponse>>() {
            @Override
            public void onResponse(Call<List<GardenScheduleResponse>> call, Response<List<GardenScheduleResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GardenScheduleResponse> schedules = response.body();
                    adapter.setData(groupByHour(schedules));
                } else {
                    Toast.makeText(ScheduleHistoryActivity.this, "No schedules found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<GardenScheduleResponse>> call, Throwable t) {
                Toast.makeText(ScheduleHistoryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<HourGroup> groupByHour(List<GardenScheduleResponse> schedules) {
        Map<String, List<GardenScheduleResponse>> grouped = new TreeMap<>();


        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

        for (GardenScheduleResponse s : schedules) {
            try {
                Date date = formatter.parse(s.getScheduledTime());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                String hourKey = String.format("%02d:00", hour);

                grouped.computeIfAbsent(hourKey, k -> new ArrayList<>()).add(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<HourGroup> result = new ArrayList<>();
        for (Map.Entry<String, List<GardenScheduleResponse>> entry : grouped.entrySet()) {
            result.add(new HourGroup(entry.getKey(), entry.getValue()));
        }

        return result;
    }

}
