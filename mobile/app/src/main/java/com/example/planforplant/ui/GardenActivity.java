package com.example.planforplant.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.planforplant.DTO.GardenResponse;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;
import com.example.planforplant.session.SessionManager;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GardenActivity extends NavigationBarActivity {
    private RecyclerView recyclerView;
    private GardenAdapter adapter;
    private List<GardenResponse> gardenList = new ArrayList<>();
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mygarden);
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_garden);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GardenAdapter(this, gardenList);
        recyclerView.setAdapter(adapter);

        sessionManager = new SessionManager(this);
        loadGardenData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Reload lại danh sách sau khi xoá thành công
            loadGardenData();
        }
    }

    private void loadGardenData() {
        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);
        api.getMyGarden()
                .enqueue(new Callback<List<GardenResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<GardenResponse>> call, @NonNull Response<List<GardenResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            gardenList.clear();
                            gardenList.addAll(response.body());
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(GardenActivity.this, "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<GardenResponse>> call, @NonNull Throwable t) {
                        Toast.makeText(GardenActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
