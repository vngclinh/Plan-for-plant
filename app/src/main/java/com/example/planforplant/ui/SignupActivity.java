

package com.example.planforplant.ui;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.planforplant.DTO.RegisterRequest;
import com.example.planforplant.DTO.JwtResponse;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private EditText etFullName, etUsername, etEmail, etPassword, etConfirmPassword;
    private Button btnConfirm;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logup);

        // Bind views
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnConfirm = findViewById(R.id.btnConfirm);

        apiService = ApiClient.getLocalClient(this).create(ApiService.class);

        btnConfirm.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String fullname = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate
        if (fullname.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build request
        RegisterRequest request = new RegisterRequest(username, password, email, "0123456789", fullname);

        // Call API
        Call<String> call = apiService.register(request);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(SignupActivity.this, response.body(), Toast.LENGTH_SHORT).show();

                    // Example: go to LoginActivity
                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                    startActivity(intent);
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

