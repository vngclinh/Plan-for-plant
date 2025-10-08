package com.example.planforplant.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import com.bumptech.glide.Glide;
import com.example.planforplant.DTO.AddGardenRequest;
import com.example.planforplant.DTO.GardenResponse;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;
import com.example.planforplant.model.Disease;
import com.example.planforplant.model.Plant;
import com.example.planforplant.model.PlantResponse;
import com.example.planforplant.model.Result;
import com.example.planforplant.session.SessionManager;
import com.example.planforplant.weather.WeatherManager;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {
    private ImageView plantImage;
    private Plant plant;
    private TextView tvPlantName, tvOverview, tvFamily, tvGenus, tvSpecies;
    private TextView tvPhylum, tvClass, tvOrder;
    private TextView tvWater, tvLight, tvTemperature, tvCareGuide, tvDiseases;

    // Weather views
    private TextView tvLocation, tvWeather;
    private ImageView ivWeatherIcon;
    private WeatherManager weatherManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        // Bind plant views
        plantImage = findViewById(R.id.plantImage);
        tvPlantName = findViewById(R.id.tvPlantName);
        tvOverview = findViewById(R.id.tvOverview);
        tvFamily = findViewById(R.id.tvFamily);
        tvGenus = findViewById(R.id.tvGenus);
        tvSpecies = findViewById(R.id.tvSpecies);
        tvPhylum = findViewById(R.id.tvPhylum);
        tvClass = findViewById(R.id.tvClass);
        tvOrder = findViewById(R.id.tvOrder);
        tvWater = findViewById(R.id.tvWater);
        tvLight = findViewById(R.id.tvLight);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvCareGuide = findViewById(R.id.tvCareGuide);
        tvDiseases = findViewById(R.id.tvDiseases);

        // Bind weather views
        tvLocation = findViewById(R.id.tvLocation);
        tvWeather = findViewById(R.id.tvWeather);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);

        // Initialize WeatherManager
        weatherManager = new WeatherManager(this, tvLocation, tvWeather, ivWeatherIcon);
        weatherManager.start();

        // Handle intent data
        Intent intent = getIntent();

        String json = intent.getStringExtra("plantResponseJson");
        if (json != null) {
            PlantResponse response = new Gson().fromJson(json, PlantResponse.class);
            bindPlantData(response);

            if (response != null && response.results != null && !response.results.isEmpty()) {
                Result first = response.results.get(0);
                List<String> keywords = new ArrayList<>();

                if (response.bestMatch != null) keywords.add(response.bestMatch);

                if (first.species != null) {
                    if (first.species.scientificName != null)
                        keywords.add(first.species.scientificName);
                    if (first.species.scientificNameWithoutAuthor != null)
                        keywords.add(first.species.scientificNameWithoutAuthor);
                    if (first.species.commonNames != null)
                        keywords.addAll(first.species.commonNames);
                }

                if (first.species.genus != null && first.species.genus.scientificName != null)
                    keywords.add(first.species.genus.scientificName);
                if (first.species.family != null && first.species.family.scientificName != null)
                    keywords.add(first.species.family.scientificName);

                searchWithFallback(keywords);
            }
            return;
        }
        String plantJson = intent.getStringExtra("plantEntityJson");
        if (plantJson != null) {
            plant = new Gson().fromJson(plantJson, Plant.class);
            bindPlantEntity(plant);
        }
        // Bind nút "Thêm vào vườn"
        MaterialButton btnAddToGarden = findViewById(R.id.btnAddToGarden);
        if (plant != null && plant.getId() != null) {
            checkIfPlantInGarden(plant.getId(), btnAddToGarden);
        } else {
            Toast.makeText(this, "Không thể tải thông tin cây", Toast.LENGTH_SHORT).show();
        }
        btnAddToGarden.setOnClickListener(v -> {
            if (plant != null && plant.getId() != null) {
                addPlantToGarden(plant.getId());
            } else {
                Toast.makeText(this, "Không tìm thấy ID của cây", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void checkIfPlantInGarden(Long plantId, MaterialButton button) {
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();

        ApiService apiService = ApiClient.getLocalClient(this).create(ApiService.class);
    }

    private void addPlantToGarden(Long plantId) {
        // Lấy token đã lưu
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Bạn cần đăng nhập để sử dụng chức năng này", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getLocalClient(this).create(ApiService.class);
        AddGardenRequest request = new AddGardenRequest(plantId);

        apiService.addPlantToGarden(request).enqueue(new Callback<GardenResponse>() {
            @Override
            public void onResponse(Call<GardenResponse> call, Response<GardenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(
                            DetailActivity.this,
                            "🌱 Đã thêm cây vào vườn của bạn!",
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    String errorMsg = "Không thể thêm cây";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}

                    Toast.makeText(DetailActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GardenResponse> call, Throwable t) {
                Toast.makeText(DetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Delegate permission results to WeatherManager
        weatherManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // --- Plant data methods ---
    private void searchWithFallback(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) return;

        ApiService apiService = ApiClient.getLocalClient(this).create(ApiService.class);

        // Lấy token từ SessionManager
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();

        for (String keyword : keywords) {
            apiService.searchPlants(keyword).enqueue(new Callback<List<Plant>>() {
                @Override
                public void onResponse(Call<List<Plant>> call, Response<List<Plant>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        bindPlantEntity(response.body().get(0));
                    }
                }

                @Override
                public void onFailure(Call<List<Plant>> call, Throwable t) {
                    // log lỗi hoặc show Toast
                }
            });
        }
    }


    private void bindPlantData(PlantResponse response) {
        if (response == null) return;
        tvPlantName.setText(response.bestMatch != null ? response.bestMatch : "Unknown");

        if (response.results != null && !response.results.isEmpty()) {
            Result first = response.results.get(0);
            if (first.species != null) {
                tvSpecies.setText(first.species.scientificName);
                if (first.species.family != null)
                    tvFamily.setText(first.species.family.scientificName);
                if (first.species.genus != null)
                    tvGenus.setText(first.species.genus.scientificName);

                if (first.species.commonNames != null && !first.species.commonNames.isEmpty())
                    tvOverview.setText("Common names: " + String.join(", ", first.species.commonNames));
                else tvOverview.setText("No common names available");
            }
        }
    }

    private void bindPlantEntity(Plant plant) {
        if (plant == null) return;
        tvPlantName.setText(plant.getCommonName() != null ? plant.getCommonName() : "Unknown");
        tvSpecies.setText(plant.getScientificName() != null ? plant.getScientificName() : "");
        tvOverview.setText(plant.getDescription() != null ? plant.getDescription() : "No description available");

        tvPhylum.setText(plant.getPhylum() != null ? plant.getPhylum() : "");
        tvClass.setText(plant.getPlantClass() != null ? plant.getPlantClass() : "");
        tvOrder.setText(plant.getPlantOrder() != null ? plant.getPlantOrder() : "");
        tvFamily.setText(plant.getFamily() != null ? plant.getFamily() : "");
        tvGenus.setText(plant.getGenus() != null ? plant.getGenus() : "");
        tvSpecies.setText(plant.getSpecies() != null ? plant.getSpecies() : "");

        tvWater.setText(plant.getWaterSchedule() != null ? plant.getWaterSchedule() : "");
        tvLight.setText(plant.getLight() != null ? plant.getLight() : "");
        tvTemperature.setText(plant.getTemperature() != null ? plant.getTemperature() : "");
        tvCareGuide.setText(plant.getCareguide() != null ? plant.getCareguide() : "");
        List<Disease> diseases = plant.getDiseases();

        if (diseases != null && !diseases.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Disease d : diseases) {
                // Add emoji + name + newline
                sb.append("🦠 ").append(d.getName()).append("\n");
            }
            // Remove the last newline
            tvDiseases.setText(sb.toString().trim());
        } else {
            tvDiseases.setText("✅ Không có bệnh được ghi nhận");
        }

        if (plant.getImageUrl() != null && !plant.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(plant.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(plantImage);
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        if (bitmap == null) return null;
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90: matrix.postRotate(90); break;
            case ExifInterface.ORIENTATION_ROTATE_180: matrix.postRotate(180); break;
            case ExifInterface.ORIENTATION_ROTATE_270: matrix.postRotate(270); break;
            default: return bitmap;
        }
        return Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}