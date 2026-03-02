package com.example.planforplant.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.planforplant.DTO.UpdateUserRequest;
import com.example.planforplant.DTO.UserResponse;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends NavigationBarActivity {

    private EditText etFullName, etEmail, etPhone, etPassword;
    private Button btnSave;
    private ProgressDialog progressDialog;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        apiService = ApiClient.getLocalClient(this).create(ApiService.class);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnSave = findViewById(R.id.btnSave);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang cập nhật...");
        progressDialog.setCancelable(false);

        // ✅ Load existing info if passed from profile page
        if (getIntent() != null) {
            etFullName.setText(getIntent().getStringExtra("fullname"));
            etEmail.setText(getIntent().getStringExtra("email"));
            etPhone.setText(getIntent().getStringExtra("phoneNumber"));
        }

        btnSave.setOnClickListener(v -> updateUserProfile());
    }

    private void updateUserProfile() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();


        if (fullName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ họ tên và email", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        UpdateUserRequest request = new UpdateUserRequest();
        request.setFullname(fullName);
        request.setEmail(email);
        request.setPhoneNumber(phone);


        apiService.updateUserProfile(request).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // 👈 Return to previous screen
                } else {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}