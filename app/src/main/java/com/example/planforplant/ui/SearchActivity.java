package com.example.planforplant.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;
import com.example.planforplant.model.Plant;
import com.example.planforplant.session.SessionManager;
import com.example.planforplant.weather.WeatherManager;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private EditText searchBox;
    private ImageView btnSearchIcon;
    private LinearLayout layoutSearchResult;
    private ApiService apiService;

    // --- Weather views ---
    private TextView tvLocation, tvWeather;
    private ImageView ivWeatherIcon;
    private WeatherManager weatherManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        // --- Initialize weather views ---
        tvLocation = findViewById(R.id.tvLocation);
        tvWeather = findViewById(R.id.tvWeather);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);

        // --- Start weather manager ---
        weatherManager = new WeatherManager(this, tvLocation, tvWeather, ivWeatherIcon);
        weatherManager.start();

        searchBox = findViewById(R.id.search_box);
        btnSearchIcon = findViewById(R.id.btnSearchIcon);
        layoutSearchResult = findViewById(R.id.layoutSearchResult);

        apiService = ApiClient.getLocalClient(this).create(ApiService.class);

        // Nếu MainActivity gửi từ khóa
        String keyword = getIntent().getStringExtra("keyword");
        if (keyword != null && !keyword.isEmpty()) {
            searchBox.setText(keyword);
            triggerSearch(keyword);
        }

        // Khi nhấn nút search trên bàn phím
        searchBox.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                triggerSearch(searchBox.getText().toString());
                return true;
            }
            return false;
        });

        // Khi click vào icon tìm kiếm
        btnSearchIcon.setOnClickListener(v -> triggerSearch(searchBox.getText().toString()));
    }

    // --- Delegate permission result to WeatherManager ---
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        weatherManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void triggerSearch(String keyword) {
        if (keyword.isEmpty()) {
            Toast.makeText(this, "Nhập từ khóa để tìm kiếm", Toast.LENGTH_SHORT).show();
            return;
        }
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Bạn cần đăng nhập để tìm kiếm", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.searchPlants(keyword.trim()).enqueue(new Callback<List<Plant>>() {
            @Override
            public void onResponse(Call<List<Plant>> call, Response<List<Plant>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Plant> plants = response.body();
                    if (plants.isEmpty()) {
                        Intent intent = new Intent(SearchActivity.this, NotFoundActivity.class);
                        intent.putExtra("message", "Không tìm thấy cây phù hợp với \"" + keyword + "\"");
                        startActivity(intent);
                        finish();
                    } else {
                        displayResults(plants);
                    }
                } else {
                    Toast.makeText(SearchActivity.this, "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Plant>> call, Throwable t) {
                Intent intent = new Intent(SearchActivity.this, NotFoundActivity.class);
                intent.putExtra("message", "Lỗi khi gọi backend: " + t.getMessage());
                startActivity(intent);
                finish();
            }
        });
    }

    private void displayResults(List<Plant> plants) {
        layoutSearchResult.removeAllViews();
        for (Plant plant : plants) {
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setPadding(8, 8, 8, 8);
            itemLayout.setBackgroundResource(R.drawable.bg_card);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 8);
            itemLayout.setLayoutParams(params);

            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(100, 100);
            imageView.setLayoutParams(imgParams);
            if (plant.getImageUrl() != null && !plant.getImageUrl().isEmpty()) {
                Picasso.get().load(plant.getImageUrl()).into(imageView);
            } else {
                imageView.setImageResource(R.drawable.ic_local_florist);
            }

            TextView textView = new TextView(this);
            textView.setText(plant.getCommonName());
            textView.setTextSize(16);
            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setPadding(12, 0, 0, 0);
            textView.setGravity(Gravity.CENTER_VERTICAL);

            // Click → open DetailActivity with plantEntityJson
            itemLayout.setOnClickListener(v -> {
                Intent intent = new Intent(SearchActivity.this, DetailActivity.class);
                intent.putExtra("plantEntityJson", new Gson().toJson(plant));
                startActivity(intent);
            });

            itemLayout.addView(imageView);
            itemLayout.addView(textView);
            layoutSearchResult.addView(itemLayout);
        }
    }
}
