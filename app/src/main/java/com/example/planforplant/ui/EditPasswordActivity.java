package com.example.planforplant.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.planforplant.DTO.ChangepasswordRequest;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditPasswordActivity extends NavigationBarActivity {
    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnChangePassword;
    private ApiService apiService;
    private ProgressDialog progressDialog;

    // Strong password rule: ≥8 chars, uppercase, lowercase, number, special character
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._-])(?=\\S+$).{8,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);

        apiService = ApiClient.getLocalClient(this).create(ApiService.class);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        initViews();
        setupListeners();
    }

    private void initViews() {
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    private void setupListeners() {
        btnChangePassword.setOnClickListener(v -> {
            String current = etCurrentPassword.getText().toString().trim();
            String newPass = etNewPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirm)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Validate strong password rule
            if (!PASSWORD_PATTERN.matcher(newPass).matches()) {
                etNewPassword.setError("Mật khẩu ≥8 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt");
                etNewPassword.requestFocus();
                return;
            }

            changePassword(current, newPass);
        });
    }

    private void changePassword(String currentPassword, String newPassword) {
        progressDialog.setMessage("Đang đổi mật khẩu...");
        progressDialog.show();

        ChangepasswordRequest request = new ChangepasswordRequest();
        request.setOldPassword(currentPassword);
        request.setNewPassword(newPassword);

        apiService.changePassword(request).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(EditPasswordActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditPasswordActivity.this, "Không thể đổi mật khẩu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(EditPasswordActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
