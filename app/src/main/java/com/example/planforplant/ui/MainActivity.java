package com.example.planforplant.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;

import com.example.planforplant.R;
import com.example.planforplant.session.SessionManager;
import com.example.planforplant.weather.WeatherManager;

/**
 * MainActivity giờ đây kế thừa từ NavigationBarActivity
 * và không cần chứa bất kỳ code nào về thanh điều hướng nữa.
 */
public class MainActivity extends NavigationBarActivity {

    private EditText searchBox;
    private SessionManager sessionManager;
    private TextView tvLocation, tvWeather;
    private ImageView ivWeatherIcon;
    private WeatherManager weatherManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            sessionManager.clear();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Layout menu.xml sẽ tự động được gắn thanh điều hướng thông qua lớp cha
        setContentView(R.layout.menu);

        setupMainContent();
    }

    private void setupMainContent() {
        // --- Header views ---
        tvLocation = findViewById(R.id.tvLocation);
        tvWeather = findViewById(R.id.tvWeather);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);

        // --- Weather manager ---
        weatherManager = new WeatherManager(this, tvLocation, tvWeather, ivWeatherIcon);
        weatherManager.start();

        // --- Search box ---
        searchBox = findViewById(R.id.search_box);
        searchBox.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String keyword = searchBox.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    startActivity(new Intent(this, SearchActivity.class).putExtra("keyword", keyword));
                }
                return true;
            }
            return false;
        });

        // --- Các nút trên màn hình chính ---
        findViewById(R.id.plant_identifier).setOnClickListener(v -> startActivity(new Intent(this, CaptureActivity.class)));
        findViewById(R.id.btn_capture_disease).setOnClickListener(v -> startActivity(new Intent(this, DiseaseCaptureActivity.class)));
        findViewById(R.id.btnSetting).setOnClickListener(v -> startActivity(new Intent(this, SettingActivity.class)));
        findViewById(R.id.btnCreatePlan).setOnClickListener(v -> startActivity(new Intent(this, PlanActivity.class)));
        findViewById(R.id.btnViewPlan).setOnClickListener(v -> startActivity(new Intent(this, ScheduleListActivity.class)));
        findViewById(R.id.btn_view_my_garden).setOnClickListener(v -> startActivity(new Intent(this, GardenActivity.class)));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (weatherManager != null) {
            weatherManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
