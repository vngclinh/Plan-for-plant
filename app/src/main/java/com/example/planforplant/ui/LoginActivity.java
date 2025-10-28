package com.example.planforplant.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.planforplant.DTO.GardenScheduleResponse;
import com.example.planforplant.DTO.JwtResponse;
import com.example.planforplant.DTO.LoginRequest;
import com.example.planforplant.Notification.NotificationWorker;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;
import com.example.planforplant.session.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private ImageView ivTogglePassword;
    private FrameLayout flTogglePasswordWrapper, flForgotPasswordWrapper;
    private Button btnLogin, btnRegister;
    private boolean isPasswordVisible = false;

    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // === Init views ===
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        flTogglePasswordWrapper = findViewById(R.id.flTogglePasswordWrapper);
        flForgotPasswordWrapper = findViewById(R.id.flForgotPasswordWrapper);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // === Init API + Session ===
        apiService = ApiClient.getLocalClient(this).create(ApiService.class);
        sessionManager = new SessionManager(this);

        // Toggle password visibility
        flTogglePasswordWrapper.setOnClickListener(v -> togglePasswordVisibility());

        // Forgot password
        flForgotPasswordWrapper.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));

        // Login
        btnLogin.setOnClickListener(v -> attemptLogin());

        // Register
        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    // ==========================================================
    // 👉 Toggle password visibility
    // ==========================================================
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivTogglePassword.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_visibility_off));
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivTogglePassword.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_visibility));
        }
        etPassword.setSelection(etPassword.getText().length());
        isPasswordVisible = !isPasswordVisible;
    }

    // ==========================================================
    // 👉 Attempt login
    // ==========================================================
    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Nhập tên đăng nhập");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Nhập mật khẩu");
            return;
        }

        LoginRequest loginRequest = new LoginRequest(username, password);

        apiService.login(loginRequest).enqueue(new Callback<JwtResponse>() {
            @Override
            public void onResponse(Call<JwtResponse> call, Response<JwtResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JwtResponse jwt = response.body();
                    sessionManager.saveTokens(jwt.getToken(), jwt.getRefreshToken());
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                    // Fetch and register schedules
                    fetchAndRegisterSchedules();

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JwtResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ==========================================================
    // 👉 Fetch schedules after login
    // ==========================================================
    private void fetchAndRegisterSchedules() {
        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);

        api.getAllSchedules().enqueue(new Callback<List<GardenScheduleResponse>>() {
            @Override
            public void onResponse(Call<List<GardenScheduleResponse>> call,
                                   Response<List<GardenScheduleResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    registerFutureSchedules(response.body());
                } else {
                    Log.e("Login", "Failed to get schedules: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<GardenScheduleResponse>> call, Throwable t) {
                Log.e("Login", "Error fetching schedules", t);
            }
        });
    }

    // ==========================================================
    // 👉 Register notifications for future schedules
    // ==========================================================
    private void registerFutureSchedules(List<GardenScheduleResponse> schedules) {
        Context context = this;
        WorkManager workManager = WorkManager.getInstance(context);

        for (GardenScheduleResponse s : schedules) {
            try {

                if ("DONE".equalsIgnoreCase(s.getCompletion())) continue;


                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = sdf.parse(s.getScheduledTime());
                if (date == null) {
                    Log.w("WorkManager", "Skip schedule " + s.getId() + " vì thời gian null");
                    continue;
                }

                long scheduleMillis = date.getTime();
                long now = System.currentTimeMillis();

                // Chỉ đặt lịch nếu là thời gian tương lai
                if (scheduleMillis > now) {
                    long delay = scheduleMillis - now;

                    Data inputData = new Data.Builder()
                            .putString("gardenName", s.getGardenNickname())
                            .putString("type", s.getType())
                            .build();

                    OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                            .setInputData(inputData)
                            .build();

                    workManager.enqueue(workRequest);

                    Log.d("WorkManager", "✅ Scheduled " + s.getType() + " cho " + s.getGardenNickname() +
                            " lúc " + s.getScheduledTime());
                } else {
                    Log.d("WorkManager", "⏩ Bỏ qua " + s.getType() + " (đã quá hạn)");
                }

            } catch (ParseException e) {
                Log.e("WorkManager", "❌ Lỗi parse thời gian: " + s.getScheduledTime(), e);
            } catch (Exception e) {
                Log.e("WorkManager", "❌ Lỗi khác khi schedule id=" + s.getId(), e);
            }
        }
    }
}