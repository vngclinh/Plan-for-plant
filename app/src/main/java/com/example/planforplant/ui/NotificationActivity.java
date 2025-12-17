package com.example.planforplant.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planforplant.R;
import com.example.planforplant.DTO.NotificationResponse;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends NavigationBarActivity {

    private RecyclerView rvNotifications;
    private TextView tvEmpty;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);

        // Back
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Views
        rvNotifications = findViewById(R.id.rvNotifications);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter();
        rvNotifications.setAdapter(adapter);
        Log.d("NOTI_UI", "📢 NotificationActivity OPENED");

        loadNotifications();
    }

    private void loadNotifications() {
        long userId = getSharedPreferences("user", MODE_PRIVATE)
                .getLong("userId", -1);

        if (userId == -1) {
            showEmpty();
            return;
        }
        Log.d("NOTI_UI", "userId = " + userId);

        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);

        api.getNotifications(userId).enqueue(new Callback<List<NotificationResponse>>() {
            @Override
            public void onResponse(
                    Call<List<NotificationResponse>> call,
                    Response<List<NotificationResponse>> response) {

                if (response.isSuccessful()) {
                    List<NotificationResponse> data = response.body();

                    if (data == null || data.isEmpty()) {
                        showEmpty();
                    } else {
                        showList(data);
                    }
                } else {
                    showEmpty();
                }
                Log.d("NOTI_API", "response code=" + response.code());
                Log.d("NOTI_API", "data=" + response.body());
            }

            @Override
            public void onFailure(Call<List<NotificationResponse>> call, Throwable t) {
                Toast.makeText(
                        NotificationActivity.this,
                        "Không tải được thông báo",
                        Toast.LENGTH_SHORT
                ).show();
                showEmpty();
            }
        });
        Log.d("NOTI_UI", "📢 loadNotifications()");
    }

    private void showEmpty() {
        tvEmpty.setVisibility(View.VISIBLE);
        rvNotifications.setVisibility(View.GONE);
    }

    private void showList(List<NotificationResponse> data) {
        tvEmpty.setVisibility(View.GONE);
        rvNotifications.setVisibility(View.VISIBLE);
        adapter.setData(data);
    }
}
