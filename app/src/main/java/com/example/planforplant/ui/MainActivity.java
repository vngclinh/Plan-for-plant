package com.example.planforplant.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.planforplant.DTO.GardenScheduleResponse;
import com.example.planforplant.NotificationHelper;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;
import com.example.planforplant.session.SessionManager;
import com.example.planforplant.weather.WeatherManager;
import com.example.planforplant.weather.WeatherUtils;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends NavigationBarActivity {

    private EditText searchBox;
    private SessionManager sessionManager;
    private TextView tvLocation, tvWeather;
    private ImageView ivWeatherIcon;

    private WeatherManager weatherManager;
    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        String token = sessionManager.getToken();
        String refresh = sessionManager.getRefreshToken();

        if (!sessionManager.isLoggedIn()) {
            sessionManager.clear();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.menu);

        // Initialize NotificationHelper and create channel
        notificationHelper = new NotificationHelper(this);
        notificationHelper.createNotificationChannel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NotificationHelper.REQUEST_CODE_POST_NOTIFICATIONS);
            }
        }

        // --- Header views ---
        tvLocation = findViewById(R.id.tvLocation);
        tvWeather = findViewById(R.id.tvWeather);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);

        // --- Weather manager ---
        weatherManager = new WeatherManager(this, tvLocation, tvWeather, ivWeatherIcon);
        weatherManager.start();
        loadTodayPlans();

        // --- Search box ---
        searchBox = findViewById(R.id.search_box);
        searchBox.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String keyword = searchBox.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    startActivity(new android.content.Intent(this, SearchActivity.class)
                            .putExtra("keyword", keyword));
                }
                return true;
            }
            return false;
        });

        // --- Plant identifier click ---
        MaterialButton plantIdentifier = findViewById(R.id.plant_identifier);
        plantIdentifier.setOnClickListener(v -> startActivity(new android.content.Intent(this, CaptureActivity.class)));

        MaterialButton detectDisease = findViewById(R.id.btn_capture_disease);
        detectDisease.setOnClickListener(v ->
                startActivity(new Intent(this, HealthCaptureActivity.class))
        );

        MaterialButton addPlan = findViewById(R.id.btnTodayCreate);
        addPlan.setOnClickListener(v -> startActivity(new android.content.Intent(this, PlanActivity.class)));

        MaterialButton viewGarden = findViewById(R.id.btn_view_my_garden);
        viewGarden.setOnClickListener(v -> { startActivity(new android.content.Intent(this,GardenActivity.class ));
        });
        MaterialButton doGame = findViewById(R.id.btn_water_game);
        doGame.setOnClickListener(v -> { startActivity(new android.content.Intent(this, WaterGameActivity.class ));
        });
    }

    private void loadTodayPlans() {
        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);

        api.getAllSchedules().enqueue(new Callback<List<GardenScheduleResponse>>() {
            @Override
            public void onResponse(Call<List<GardenScheduleResponse>> call, Response<List<GardenScheduleResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                    List<GardenScheduleResponse> todayList = new ArrayList<>();

                    for (GardenScheduleResponse s : response.body()) {
                        if (s.getScheduledTime() != null && s.getScheduledTime().startsWith(today)) {
                            todayList.add(s);
                        }
                    }

                    Collections.sort(todayList, Comparator.comparing(GardenScheduleResponse::getScheduledTime));

                    showTodayPlans(todayList);
                }
            }

            @Override
            public void onFailure(Call<List<GardenScheduleResponse>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Không thể tải kế hoạch hôm nay", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showTodayPlans(List<GardenScheduleResponse> list) {

        LinearLayout layoutList = findViewById(R.id.todayPlanList);
        LinearLayout layoutEmpty = findViewById(R.id.todayPlanEmpty);
        TextView tvTitle = findViewById(R.id.tvTodayPlanTitle);

        layoutList.removeAllViews();

        if (list == null || list.isEmpty()) {
            layoutList.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            tvTitle.setVisibility(View.GONE);
            return;
        }

        layoutList.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        tvTitle.setVisibility(View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(this);

        for (GardenScheduleResponse s : list) {

            View card = inflater.inflate(R.layout.item_plan_today, layoutList, false);

            TextView tvTime = card.findViewById(R.id.tvPlanTime);
            TextView tvTitle2 = card.findViewById(R.id.tvPlanTitle);
            TextView tvNote = card.findViewById(R.id.tvPlanNote);

            // Time formatting
            String time = s.getScheduledTime().substring(11, 16);
            tvTime.setText(time);

            // Title
            String plant = s.getPlantName() != null ? s.getPlantName() : "Cây #" + s.getGardenId();
            tvTitle2.setText(mapType(s.getType()) + " (" + plant + ")");

            // Note
            if (s.getNote() != null && !s.getNote().trim().isEmpty()) {
                tvNote.setText("Ghi chú: " + s.getNote());
                tvNote.setVisibility(View.VISIBLE);
            }

            layoutList.addView(card);
        }
    }

    private String mapType(String type) {
        switch (type) {
            case "WATERING": return "Tưới cây";
            case "FERTILIZING": return "Bón phân";
            case "PRUNING": return "Tỉa lá";
            case "MIST": return "Phun ẩm";
            case "OTHER": return "Hoạt động khác";
            case "NOTE": return "Ghi chú";
            default: return type;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Delegate to WeatherManager first
        weatherManager.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NotificationHelper.REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Thông báo đã được bật 🌿", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Ứng dụng cần quyền để hiển thị thông báo 🌿", Toast.LENGTH_LONG).show();
            }
        }
    }
}
