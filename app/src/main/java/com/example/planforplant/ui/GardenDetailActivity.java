package com.example.planforplant.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ContextThemeWrapper;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.example.planforplant.DTO.GardenScheduleResponse;
import com.example.planforplant.DTO.UserResponse;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;
import com.example.planforplant.model.Disease;
import com.example.planforplant.model.Plant;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GardenDetailActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 200;

    private FusedLocationProviderClient fusedLocationClient;
    private ProgressDialog progressDialog;

    private ImageView imgPlant;
    private TextView tvCommonName, tvNickname, tvStatus, tvDateAdded;
    private TextView tvOverview, tvFamily, tvGenus, tvSpecies;
    private TextView tvPhylum, tvClass, tvOrder;
    private TextView tvWater, tvLight, tvTemperature, tvCareGuide, tvDiseases;

    private Long gardenId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garden_detail);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);


        // === Bind các view chính ===
        imgPlant = findViewById(R.id.imgPlant);
        tvCommonName = findViewById(R.id.tvCommonName);
        tvNickname = findViewById(R.id.tvNickname);
        tvStatus = findViewById(R.id.tvStatus);
        tvDateAdded = findViewById(R.id.tvDateAdded);

        // === Bind các view thông tin chi tiết về cây ===
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

        // === Nhận dữ liệu từ Intent ===
        Intent intent = getIntent();
        String nickname = intent.getStringExtra("nickname");
        String status = intent.getStringExtra("status");
        String dateAdded = intent.getStringExtra("dateAdded");
        gardenId = getIntent().getLongExtra("gardenId", -1);
        if (gardenId == -1) {
            Toast.makeText(this, "Invalid garden ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Plant plant = (Plant) intent.getSerializableExtra("plant");

        // === Gán dữ liệu Garden ===
        tvNickname.setText("Tên riêng: " + (nickname != null ? nickname : "Chưa đặt"));
        tvStatus.setText("Trạng thái: " + (status != null ? status : "Không rõ"));
        tvDateAdded.setText("Ngày thêm: " + (dateAdded != null ? dateAdded : "Không xác định"));

        // === Gọi hàm hiển thị thông tin cây ===
        if (plant != null) {
            bindPlantEntity(plant);
        }

        ImageView btnMore = findViewById(R.id.btn_more);
        btnMore.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, v);
            popupMenu.getMenuInflater().inflate(R.menu.menu_garden_detail, popupMenu.getMenu());

            // Gắn menu resource

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_edit_name) {
                    // TODO: xử lý đổi tên
                    return true;
                } else if (id == R.id.action_update_status) {
                    // TODO: xử lý cập nhật trạng thái
                    return true;
                } else if (id == R.id.action_add_photo) {
                    // TODO: xử lý thêm ảnh
                    return true;
                } else if (id == R.id.action_auto_gen){
                    generateAutoWateringSchedule();
                    return true;

                }
                return false;
            });

            popupMenu.show();
        });
    }

    private void bindPlantEntity(Plant plant) {
        if (plant == null) return;

        // Thông tin cơ bản
        tvCommonName.setText(plant.getCommonName() != null ? plant.getCommonName() : "Unknown");
        tvOverview.setText(plant.getDescription() != null ? plant.getDescription() : "No description available");

        // Phân loại khoa học
        tvPhylum.setText(plant.getPhylum() != null ? plant.getPhylum() : "");
        tvClass.setText(plant.getPlantClass() != null ? plant.getPlantClass() : "");
        tvOrder.setText(plant.getPlantOrder() != null ? plant.getPlantOrder() : "");
        tvFamily.setText(plant.getFamily() != null ? plant.getFamily() : "");
        tvGenus.setText(plant.getGenus() != null ? plant.getGenus() : "");
        tvSpecies.setText(plant.getSpecies() != null ? plant.getSpecies() : "");

        // Điều kiện sinh trưởng
        tvWater.setText(plant.getWaterSchedule() != null ? plant.getWaterSchedule() : "");
        tvLight.setText(plant.getLight() != null ? plant.getLight() : "");
        tvTemperature.setText(plant.getTemperature() != null ? plant.getTemperature() : "");

        // Hướng dẫn chăm sóc
        tvCareGuide.setText(plant.getCareguide() != null ? plant.getCareguide() : "");

        // Bệnh thường gặp
        List<Disease> diseases = plant.getDiseases();
        if (diseases != null && !diseases.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Disease d : diseases) {
                sb.append("🦠 ").append(d.getName()).append("\n");
            }
            tvDiseases.setText(sb.toString().trim());
        } else {
            tvDiseases.setText("✅ Không có bệnh được ghi nhận");
        }

        // Ảnh cây
        if (plant.getImageUrl() != null && !plant.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(plant.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(imgPlant);
        }
    }

    private void generateAutoWateringSchedule() {
        progressDialog.setMessage("Checking your saved location...");
        progressDialog.show();

        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);

        // 1. Fetch user info from backend
        api.getProfile().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    if (user.getLat() == null || user.getLon() == null) {
                        // No location set → show dialog
                        showLocationDialog();
                    } else {
                        // Use saved location
                        callAutoGenerateApi(user.getLat(), user.getLon());
                    }
                } else {
                    Toast.makeText(GardenDetailActivity.this,
                            "Không thể lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(GardenDetailActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLocationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Chưa đặt vị trí")
                .setMessage("Bạn chưa đặt vị trí nhà. Bạn có muốn đặt bây giờ không?")
                .setPositiveButton("Đặt vị trí nhà", (dialog, which) -> {
                    // Open LocationActivity
                    Intent intent = new Intent(this, LocationActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Sử dụng vị trí hiện tại", (dialog, which) -> {
                    fetchCurrentLocationForWatering();
                })
                .show();
    }

    private void fetchCurrentLocationForWatering() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return;
        }

        progressDialog.setMessage("Fetching GPS location...");
        progressDialog.show();

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            progressDialog.dismiss();
            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                callAutoGenerateApi(lat, lon);
            } else {
                Toast.makeText(this,
                        "Không lấy được vị trí hiện tại. Hãy thử lại.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this,
                    "Lỗi khi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void callAutoGenerateApi(double lat, double lon) {
        progressDialog.setMessage("Generating watering schedule...");
        progressDialog.show();
        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);

        api.generateWeeklyWateringSchedule(gardenId, lat, lon)
                .enqueue(new Callback<List<GardenScheduleResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<GardenScheduleResponse>> call,
                                           @NonNull Response<List<GardenScheduleResponse>> response) {
                        progressDialog.dismiss();
                        if (response.isSuccessful() && response.body() != null) {
                            List<GardenScheduleResponse> schedules = response.body();
                            Toast.makeText(GardenDetailActivity.this,
                                    "✅ Generated " + schedules.size() + " tasks!",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(GardenDetailActivity.this,
                                    "❌ Failed to generate schedule", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<GardenScheduleResponse>> call,
                                          @NonNull Throwable t) {
                        progressDialog.dismiss();
                        Toast.makeText(GardenDetailActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
