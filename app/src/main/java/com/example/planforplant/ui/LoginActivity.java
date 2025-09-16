package com.example.planforplant.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.planforplant.DTO.JwtResponse;
import com.example.planforplant.DTO.LoginRequest;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;
import com.example.planforplant.session.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Init views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister); // Added register button

        // Init ApiService + SessionManager
        apiService = ApiClient.getLocalClient(this).create(ApiService.class);
        sessionManager = new SessionManager(this);

        // Login button click
        btnLogin.setOnClickListener(v -> attemptLogin());

        // Register button click → open SignUpActivity
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

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

        // Call API
        apiService.login(loginRequest).enqueue(new Callback<JwtResponse>() {
            @Override
            public void onResponse(Call<JwtResponse> call, Response<JwtResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JwtResponse jwtResponse = response.body();

                    // Save token into SharedPreferences
                    sessionManager.saveTokens(
                            jwtResponse.getToken(),
                            jwtResponse.getRefreshToken()
                    );

                    Log.d("LoginActivity", "Token saved: " + jwtResponse.getToken());

                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                    // Redirect to MainActivity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
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
}