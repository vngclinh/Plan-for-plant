package com.example.planforplant.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private PlantAdapter plantAdapter;
    private ApiService apiService;
    private WeatherManager weatherManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
        // Weather setup
        TextView tvLocation = findViewById(R.id.tvLocation);
        TextView tvWeather = findViewById(R.id.tvWeather);
        ImageView ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        weatherManager = new WeatherManager(this, tvLocation, tvWeather, ivWeatherIcon);
        weatherManager.start();

        // Initialize views
        searchBox = findViewById(R.id.search_box);
        btnSearchIcon = findViewById(R.id.btnSearchIcon);
        recyclerView = findViewById(R.id.recyclerSearchResults);
        progressBar = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        plantAdapter = new PlantAdapter(this);
        recyclerView.setAdapter(plantAdapter);

        apiService = ApiClient.getLocalClient(this).create(ApiService.class);

        // Handle search icon click
        btnSearchIcon.setOnClickListener(v -> triggerSearch(searchBox.getText().toString()));

        // Handle "search" key on keyboard
        searchBox.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                triggerSearch(searchBox.getText().toString());
                return true;
            }
            return false;
        });

        // Check if keyword passed from another Activity
        String keyword = getIntent().getStringExtra("keyword");
        if (keyword != null && !keyword.isEmpty()) {
            searchBox.setText(keyword);
            triggerSearch(keyword);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        weatherManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void triggerSearch(String keyword) {
        keyword = keyword.trim();
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

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        String finalKeyword = keyword;
        apiService.searchPlants(keyword).enqueue(new Callback<List<Plant>>() {
            @Override
            public void onResponse(Call<List<Plant>> call, Response<List<Plant>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Plant> plants = response.body();
                    if (plants.isEmpty()) {
                        Toast.makeText(SearchActivity.this, "Không tìm thấy cây phù hợp với \"" + finalKeyword + "\"", Toast.LENGTH_SHORT).show();
                    }
                    recyclerView.setVisibility(View.VISIBLE);
                    plantAdapter.setPlants(plants);
                } else {
                    Toast.makeText(SearchActivity.this, "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Plant>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("SearchActivity", "Lỗi kết nối API", t);
                Toast.makeText(SearchActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
