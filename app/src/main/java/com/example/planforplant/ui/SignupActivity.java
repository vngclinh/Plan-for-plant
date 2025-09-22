package com.example.planforplant.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.planforplant.DTO.RegisterRequest;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private EditText etFullName, etUsername, etEmail, etPhone, etPassword, etConfirmPassword;
    private Button btnConfirm, btnBackHome;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logup);

        // Bind views
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone); // bind phone
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBackHome = findViewById(R.id.btnBackHome); // bind back button

        // Retrofit API
        apiService = ApiClient.getLocalClient(this).create(ApiService.class);

        // Button listeners
        btnConfirm.setOnClickListener(v -> attemptRegister());
        btnBackHome.setOnClickListener(v -> {
            // Go back to login activity
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void attemptRegister() {
        String fullname = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate fullname
        if (fullname.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ tên");
            etFullName.requestFocus();
            return;
        }

        // Validate username
        if (username.isEmpty()) {
            etUsername.setError("Vui lòng nhập tên đăng nhập");
            etUsername.requestFocus();
            return;
        }
        if (username.length() < 4) {
            etUsername.setError("Tên đăng nhập phải có ít nhất 4 ký tự");
            etUsername.requestFocus();
            return;
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            etUsername.setError("Tên đăng nhập chỉ được chứa chữ, số và dấu gạch dưới (_)");
            etUsername.requestFocus();
            return;
        }

        // Validate email
        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return;
        }

        // Validate phone (optional, you can add more rules)
        if (phone.isEmpty()) {
            etPhone.setError("Vui lòng nhập số điện thoại");
            etPhone.requestFocus();
            return;
        }

        // Validate password
        if (password.isEmpty()) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 8) {
            etPassword.setError("Mật khẩu phải có ít nhất 8 ký tự");
            etPassword.requestFocus();
            return;
        }
        if (!password.matches(".*[A-Z].*")) {
            etPassword.setError("Mật khẩu phải chứa ít nhất 1 chữ hoa");
            etPassword.requestFocus();
            return;
        }
        if (!password.matches(".*[a-z].*")) {
            etPassword.setError("Mật khẩu phải chứa ít nhất 1 chữ thường");
            etPassword.requestFocus();
            return;
        }
        if (!password.matches(".*\\d.*")) {
            etPassword.setError("Mật khẩu phải chứa ít nhất 1 chữ số");
            etPassword.requestFocus();
            return;
        }
        if (!password.matches(".*[@#$%^&+=!].*")) {
            etPassword.setError("Mật khẩu phải chứa ít nhất 1 ký tự đặc biệt (@#$%^&+=!)");
            etPassword.requestFocus();
            return;
        }

        // Confirm password
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            etConfirmPassword.requestFocus();
            return;
        }

        // API call
        RegisterRequest request = new RegisterRequest(username, password, email, phone, fullname);
        Call<String> call = apiService.register(request);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(SignupActivity.this, response.body(), Toast.LENGTH_SHORT).show();
                    // Navigate to login
                    startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(SignupActivity.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(SignupActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
