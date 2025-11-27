package com.example.planforplant.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.planforplant.DTO.AddDiseasesRequest;
import com.example.planforplant.DTO.GardenDiseaseResponse;
import com.example.planforplant.DTO.GardenResponse;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;
import com.example.planforplant.model.Disease;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiseaseDetailActivity extends AppCompatActivity {

    private ImageView btnBack, imgDisease;
    private TextView tvName, tvScientificName, tvSymptoms, tvCauses, tvCareGuide, tvDescription;
    private Button btnAddToGarden;

    private long diseaseId;
    private long gardenId;
    private String nickname;

    private String status;
    private String dateAdded;
    private Long plantId;

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
        btnAddToGarden = findViewById(R.id.btnAddToGarden);


        diseaseId = getIntent().getLongExtra("diseaseId", -1);


        if (diseaseId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin bệnh!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        gardenId = getIntent().getLongExtra("gardenId", -1);
        nickname = getIntent().getStringExtra("nickname");
        status = getIntent().getStringExtra("status");
        dateAdded = getIntent().getStringExtra("dateAdded");
        plantId = getIntent().getLongExtra("plantId", -1);
        Button btnAddDisease = findViewById(R.id.btnAddToGarden);
        checkIfDiseaseExistsInGarden();
        if (gardenId != -1) {
            btnAddDisease.setVisibility(View.VISIBLE);
            btnAddDisease.setText("Thêm vào vườn " + nickname);
            btnAddDisease.setOnClickListener(v -> addDiseaseToGarden());
        } else {
            btnAddDisease.setVisibility(View.GONE);
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


    private void addDiseaseToGarden() {
        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);

        AddDiseasesRequest request = new AddDiseasesRequest();
        request.setGardenId(gardenId);
        request.setDiseaseIds(Collections.singletonList(diseaseId));

        api.addDiseases(request).enqueue(new Callback<List<GardenDiseaseResponse>>() {
            @Override
            public void onResponse(Call<List<GardenDiseaseResponse>> call, Response<List<GardenDiseaseResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(DiseaseDetailActivity.this, "Đã thêm bệnh vào vườn " + nickname, Toast.LENGTH_SHORT).show();

                    // Trở về GardenDetailActivity với các field cần thiết
                    Intent intent = new Intent(DiseaseDetailActivity.this, GardenDetailActivity.class);
                    intent.putExtra("gardenId", gardenId);
                    intent.putExtra("nickname", nickname);
                    intent.putExtra("status", status);
                    intent.putExtra("dateAdded", dateAdded);
                    intent.putExtra("plantId", plantId);


                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(DiseaseDetailActivity.this, "Thêm thất bại! Mã lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<GardenDiseaseResponse>> call, Throwable t) {
                Toast.makeText(DiseaseDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfDiseaseExistsInGarden() {
        if (gardenId == -1) return;

        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);

        api.getDiseasesByGardenId(gardenId).enqueue(new Callback<List<GardenDiseaseResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<GardenDiseaseResponse>> call,
                                   @NonNull Response<List<GardenDiseaseResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GardenDiseaseResponse> diseases = response.body();

                    boolean diseaseExists = diseases.stream()
                            .anyMatch(gd -> gd.getDiseaseId() == diseaseId
                                    && "ACTIVE".equals(gd.getStatus()));

                    if (diseaseExists) {
                        btnAddToGarden.setEnabled(false);
                        btnAddToGarden.setText(nickname + " đang bị bệnh này");
                        btnAddToGarden.setBackgroundColor(
                                ContextCompat.getColor(DiseaseDetailActivity.this, R.color.gray) // disabled color
                        );
                    } else {
                        btnAddToGarden.setEnabled(true);
                        btnAddToGarden.setText("Thêm vào vườn " + nickname);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GardenDiseaseResponse>> call, @NonNull Throwable t) {
                Log.e("DiseaseDetail", "Cannot fetch garden diseases", t);
            }
        });
    }

}
