

package com.example.planforplant.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyCodeActivity extends AppCompatActivity {
    private EditText etCode;
    private ApiService apiService;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);

        etCode = findViewById(R.id.etCode);
        Button btnVerify = findViewById(R.id.btnVerify);
        TextView tvResend = findViewById(R.id.tvResend);

        apiService = ApiClient.getLocalClient(this).create(ApiService.class);
        email = getIntent().getStringExtra("email");

        btnVerify.setOnClickListener(v -> verifyCode());

        tvResend.setOnClickListener(v -> resendCode());
    }

    private void verifyCode() {
        String code = etCode.getText().toString().trim();
        if (code.isEmpty()) {
            etCode.setError("Vui lòng nhập mã");
            etCode.requestFocus();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("code", code);

        apiService.verifyResetCode(body).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(VerifyCodeActivity.this, "Xác thực thành công", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(VerifyCodeActivity.this, ChangePasswordActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("code", code);
                    startActivity(intent);
                } else {
                    Toast.makeText(VerifyCodeActivity.this, "Mã không đúng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(VerifyCodeActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resendCode() {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);

        apiService.forgotPassword(body).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(VerifyCodeActivity.this, "Mã mới đã được gửi", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(VerifyCodeActivity.this, "Không thể gửi lại mã", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(VerifyCodeActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

