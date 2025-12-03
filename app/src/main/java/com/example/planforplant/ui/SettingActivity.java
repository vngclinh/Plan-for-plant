package com.example.planforplant.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.planforplant.DTO.UserResponse;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;

import com.example.planforplant.session.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingActivity extends NavigationBarActivity {

    private SessionManager sessionManager;
    private TextView tvName, tvEmail;
    private ImageView imgAvatar;
    // ...existing code... keep only fields that are used outside onCreate
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting1);

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getLocalClient(this).create(ApiService.class);

        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        imgAvatar = findViewById(R.id.imgAvatar);
        // Convert these UI controls to local variables since they're only used in onCreate
        View cardManageAccount = findViewById(R.id.cardManageAccount);
        View cardChangePassword = findViewById(R.id.cardChangePassword);
        View cardSupport = findViewById(R.id.cardSupport);
        View cardStatistics = findViewById(R.id.cardStatistics);
        Button btnLogout = findViewById(R.id.btnLogout);

        loadUserProfile();

        cardManageAccount.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        cardChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, EditPasswordActivity.class);
            startActivity(intent);
        });

        cardSupport.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, LocationActivity.class);
            startActivity(intent);
        });

        // Mở màn hình Thống kê khi người dùng bấm vào card
        cardStatistics.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> logout());
    }

    private void loadUserProfile() {
        apiService.getProfile().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();

                    tvName.setText(user.getFullname());
                    tvEmail.setText(user.getEmail());

                    // 🖼 Load avatar using Glide
                    if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                        Glide.with(SettingActivity.this)
                                .load(user.getAvatarUrl())
                                .placeholder(R.drawable.ic_user) // default image
                                .circleCrop()
                                .into(imgAvatar);
                    }
                } else {
                    Toast.makeText(SettingActivity.this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                    Log.e("SettingActivity", "Error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                Toast.makeText(SettingActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
                Log.e("SettingActivity", "Failure: " + t.getMessage(), t);
            }
        });
    }

    private void logout() {
        sessionManager.clear();
        Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}