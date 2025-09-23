package com.example.planforplant.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

        // Init views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        flTogglePasswordWrapper = findViewById(R.id.flTogglePasswordWrapper);
        flForgotPasswordWrapper = findViewById(R.id.flForgotPasswordWrapper);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // Init API and session
        apiService = ApiClient.getLocalClient(this).create(ApiService.class);
        sessionManager = new SessionManager(this);

        // Toggle password visibility using wrapper for larger touch target
        flTogglePasswordWrapper.setOnClickListener(v -> togglePasswordVisibility());

        // Forgot password click
        flForgotPasswordWrapper.setOnClickListener(v -> {
            // TODO: Navigate to ForgotPasswordActivity or show reset dialog
            Toast.makeText(LoginActivity.this, "Forgot password clicked", Toast.LENGTH_SHORT).show();
        });

        // Login click
        btnLogin.setOnClickListener(v -> attemptLogin());

        // Register click
        btnRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivTogglePassword.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_visibility_off));
        } else {
            // Show password
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivTogglePassword.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_visibility));
        }
        // Keep cursor at the end
        etPassword.setSelection(etPassword.getText().length());
        isPasswordVisible = !isPasswordVisible;
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

        apiService.login(loginRequest).enqueue(new Callback<JwtResponse>() {
            @Override
            public void onResponse(Call<JwtResponse> call, Response<JwtResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JwtResponse jwt = response.body();
                    sessionManager.saveTokens(jwt.getToken(), jwt.getRefreshToken());
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
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
}
