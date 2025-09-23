package com.example.planforplant.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import com.bumptech.glide.Glide;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;
import com.example.planforplant.model.Plant;
import com.example.planforplant.model.PlantResponse;
import com.example.planforplant.model.Result;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {
    private ImageView plantImage;
    private TextView tvPlantName, tvOverview, tvFamily, tvGenus, tvSpecies;
    private TextView tvKingdom, tvPhylum, tvClass, tvOrder;
    private TextView tvWater, tvLight, tvTemperature, tvCareGuide, tvDiseases;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        // Bind views
        plantImage = findViewById(R.id.plantImage);
        tvPlantName = findViewById(R.id.tvPlantName);
        tvOverview = findViewById(R.id.tvOverview);
        tvFamily = findViewById(R.id.tvFamily);
        tvGenus = findViewById(R.id.tvGenus);
        tvSpecies = findViewById(R.id.tvSpecies);

        tvKingdom = findViewById(R.id.tvKingdom);
        tvPhylum = findViewById(R.id.tvPhylum);
        tvClass = findViewById(R.id.tvClass);
        tvOrder = findViewById(R.id.tvOrder);

        tvWater = findViewById(R.id.tvWater);
        tvLight = findViewById(R.id.tvLight);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvCareGuide = findViewById(R.id.tvCareGuide);
        tvDiseases = findViewById(R.id.tvDiseases);

        // Always show image first
        handleImage();

        Intent intent = getIntent();

        // Case 1: CaptureActivity -> PlantResponse
        String json = intent.getStringExtra("plantResponseJson");
        if (json != null) {
            PlantResponse response = new Gson().fromJson(json, PlantResponse.class);
            bindPlantData(response);

            if (response != null && response.bestMatch != null) {
                fetchPlantFromBackend(response.bestMatch);
            }
            return;
        }

        // Case 2: SearchResultsActivity -> Plant
        String plantJson = intent.getStringExtra("plantEntityJson");
        if (plantJson != null) {
            Plant plant = new Gson().fromJson(plantJson, Plant.class);
            bindPlantEntity(plant);
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

                if (first.species.commonNames != null && !first.species.commonNames.isEmpty()) {
                    tvOverview.setText("Common names: " + String.join(", ", first.species.commonNames));
                } else {
                    tvOverview.setText("No common names available");
                }
            }
        }
    }

    private void bindPlantEntity(Plant plant) {
        if (plant == null) return;

        // Basic info
        tvPlantName.setText(plant.getCommonName() != null ? plant.getCommonName() : "Unknown");
        tvSpecies.setText(plant.getScientificName() != null ? plant.getScientificName() : "");
        tvOverview.setText(plant.getDescription() != null ? plant.getDescription() : "No description available");

        // Taxonomy
        tvKingdom.setText("Plantae"); // fixed
        tvPhylum.setText(plant.getPhylum() != null ? plant.getPhylum() : "");
        tvClass.setText(plant.getPlantClass() != null ? plant.getPlantClass() : "");
        tvOrder.setText(plant.getPlantOrder() != null ? plant.getPlantOrder() : "");
        tvFamily.setText(plant.getFamily() != null ? plant.getFamily() : "");
        tvGenus.setText(plant.getGenus() != null ? plant.getGenus() : "");
        tvSpecies.setText(plant.getSpecies() != null ? plant.getSpecies() : "");

        // Growth & care
        tvWater.setText(plant.getWaterSchedule() != null ? plant.getWaterSchedule() : "");
        tvLight.setText(plant.getLight() != null ? plant.getLight() : "");
        tvTemperature.setText(plant.getTemperature() != null ? plant.getTemperature() : "");
        tvCareGuide.setText(plant.getCareGuide() != null ? plant.getCareGuide() : "");

        // Diseases (placeholder for now — backend chưa có trường này)
        tvDiseases.setText("🍂 Bệnh rụng lá sớm\n🕷️ Sâu đục thân\n🦠 Nấm mốc trắng");

        // Load image if available
        if (plant.getImageUrl() != null && !plant.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(plant.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(plantImage);
        }
    }

    private void fetchPlantFromBackend(String keyword) {
        ApiService apiService = ApiClient.getLocalClient(DetailActivity.this).create(ApiService.class);

        apiService.searchPlants(keyword).enqueue(new Callback<List<Plant>>() {
            @Override
            public void onResponse(Call<List<Plant>> call, Response<List<Plant>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    bindPlantEntity(response.body().get(0)); // ✅ show first plant
                } else {
                    // ❌ No result → NotFoundActivity
                    Intent intent = new Intent(DetailActivity.this, NotFoundActivity.class);
                    intent.putExtra("message", "Không tìm thấy cây trong cơ sở dữ liệu cho từ khóa: " + keyword);
                    startActivity(intent);
                    finish(); // close detail page
                }
            }

            @Override
            public void onFailure(Call<List<Plant>> call, Throwable t) {
                // ❌ Error → NotFoundActivity with error message
                Intent intent = new Intent(DetailActivity.this, NotFoundActivity.class);
                intent.putExtra("message", "Lỗi khi gọi backend: " + t.getMessage());
                startActivity(intent);
                finish();
            }
        });
    }
    private void handleImage() {
        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString == null) return;

        Uri imageUri = Uri.parse(imageUriString);
        try {
            Bitmap bitmap;
            if ("file".equals(imageUri.getScheme())) {
                String path = imageUri.getPath();
                bitmap = BitmapFactory.decodeFile(path);
                ExifInterface ei = new ExifInterface(path);
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                bitmap = rotateBitmap(bitmap, orientation);
            } else {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                bitmap = BitmapFactory.decodeStream(inputStream);
                ExifInterface ei = new ExifInterface(getContentResolver().openInputStream(imageUri));
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                bitmap = rotateBitmap(bitmap, orientation);
            }
            plantImage.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
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