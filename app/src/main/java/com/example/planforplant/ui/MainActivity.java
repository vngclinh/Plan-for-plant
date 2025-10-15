package com.example.planforplant.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.planforplant.R;
import com.example.planforplant.session.SessionManager;
import com.example.planforplant.weather.WeatherManager;
import com.example.planforplant.weather.WeatherUtils;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private EditText searchBox;
    private SessionManager sessionManager;
    private TextView tvLocation, tvWeather;
    private ImageView ivWeatherIcon;

    private WeatherManager weatherManager;

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

        MaterialButton Setting = findViewById(R.id.btnSetting);
        Setting.setOnClickListener(v -> startActivity(new android.content.Intent(this, SettingActivity.class)));

        MaterialButton addPlan = findViewById(R.id.btnCreatePlan);
        addPlan.setOnClickListener(v -> startActivity(new android.content.Intent(this, PlanActivity.class)));
        // view garden click
        MaterialButton viewGarden = findViewById(R.id.btn_view_my_garden);
        viewGarden.setOnClickListener(  v -> startActivity(new android.content.Intent(this,GardenActivity.class )));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Delegate to WeatherManager
        weatherManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
