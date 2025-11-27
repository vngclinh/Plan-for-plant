package com.example.planforplant.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planforplant.DTO.GardenDiseaseResponse;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;

import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiseaseHistoryActivity extends AppCompatActivity {

    private RecyclerView rcvHistory;
    private DiseaseHistoryAdapter adapter;
    private Long gardenId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_history);

        rcvHistory = findViewById(R.id.rcvDiseaseHistory);
        rcvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DiseaseHistoryAdapter(this);
        rcvHistory.setAdapter(adapter);

        gardenId = getIntent().getLongExtra("gardenId", -1);

        loadDiseaseHistory();
    }

    private void loadDiseaseHistory() {
        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);

        api.getDiseasesByGardenId(gardenId).enqueue(new Callback<List<GardenDiseaseResponse>>() {
            @Override
            public void onResponse(Call<List<GardenDiseaseResponse>> call,
                                   Response<List<GardenDiseaseResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // Sort newest first
                    List<GardenDiseaseResponse> list = response.body();
                    list.sort(Comparator.comparing(GardenDiseaseResponse::getDetectedDate).reversed());

                    adapter.setData(list);
                }
            }

            @Override
            public void onFailure(Call<List<GardenDiseaseResponse>> call, Throwable t) {
                Toast.makeText(DiseaseHistoryActivity.this,
                        "Failed to load history", Toast.LENGTH_SHORT).show();
            }
        });
    }
}