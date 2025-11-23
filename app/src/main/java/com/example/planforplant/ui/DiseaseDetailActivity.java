package com.example.planforplant.ui;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;
import com.example.planforplant.model.Disease;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiseaseDetailActivity extends AppCompatActivity {

    private ImageView btnBack, imgDisease;
    private TextView tvName, tvScientificName, tvSymptoms, tvCauses, tvCareGuide, tvDescription;

    private long diseaseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_detail);

        // Bind View
        btnBack = findViewById(R.id.btn_back);
        imgDisease = findViewById(R.id.imgDisease);
        tvName = findViewById(R.id.tvDiseaseName);
        tvScientificName = findViewById(R.id.tvScientificName);
        tvSymptoms = findViewById(R.id.tvSymptoms);
        tvCauses = findViewById(R.id.tvCauses);
        tvCareGuide = findViewById(R.id.tvCareGuide);
        tvDescription = findViewById(R.id.tvDescription);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        diseaseId = getIntent().getLongExtra("diseaseId", -1);

        if (diseaseId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin bệnh!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadDiseaseDetail();
    }

    private void loadDiseaseDetail() {
        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);

        api.getDiseaseById(diseaseId).enqueue(new Callback<Disease>() {
            @Override
            public void onResponse(@NonNull Call<Disease> call, @NonNull Response<Disease> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bindUI(response.body());
                } else {
                    Toast.makeText(DiseaseDetailActivity.this, "Lỗi khi tải dữ liệu!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Disease> call, @NonNull Throwable t) {
                Toast.makeText(DiseaseDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindUI(Disease d) {
        tvName.setText(d.getName());
        tvScientificName.setText(d.getScientificName());
        tvSymptoms.setText(d.getSymptoms());
        tvCauses.setText(d.getCauses());
        tvCareGuide.setText(d.getCareguide());
        tvDescription.setText(d.getDescription());

        if (d.getImageUrl() != null && !d.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(d.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(imgDisease);
        }
    }
}
