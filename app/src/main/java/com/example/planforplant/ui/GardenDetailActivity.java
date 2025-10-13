package com.example.planforplant.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;
import com.example.planforplant.model.Disease;
import com.example.planforplant.model.Plant;

import java.util.List;

public class GardenDetailActivity extends AppCompatActivity {

    private ImageView imgPlant;
    private TextView tvCommonName, tvNickname, tvStatus, tvDateAdded;
    private TextView tvOverview, tvFamily, tvGenus, tvSpecies;
    private TextView tvPhylum, tvClass, tvOrder;
    private TextView tvWater, tvLight, tvTemperature, tvCareGuide, tvDiseases;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garden_detail);

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
                } else if (id == R.id.action_delete){
                    ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);

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
}
