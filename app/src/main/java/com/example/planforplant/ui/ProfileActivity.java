package com.example.planforplant.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.planforplant.DTO.UserProfileResponse;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;
import com.example.planforplant.utils.FileUtils;

import java.io.File;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 100;

    private ImageView imgAvatar;
    private TextView tvFullname, tvUsername, tvEmail, tvPhone;
    private Button btnEditProfile;

    private ApiService apiService;
    private ProgressDialog progressDialog;
    private Uri selectedImageUri;
    private String currentAvatarUrl = null;  // 🔹 to store current avatar url

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    imgAvatar.setImageURI(selectedImageUri);
                    uploadAvatar();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        apiService = ApiClient.getLocalClient(this).create(ApiService.class);

        initViews();
        setupListeners();
        fetchProfile();
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        tvFullname = findViewById(R.id.tvFullname);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    private void setupListeners() {
        // 🔹 Show options dialog when clicking avatar
        imgAvatar.setOnClickListener(v -> showAvatarOptionsDialog());

        btnEditProfile.setOnClickListener(v ->
                Toast.makeText(this, "Edit feature coming soon!", Toast.LENGTH_SHORT).show()
        );
    }

    // 🔹 Show dialog with 2 options
    private void showAvatarOptionsDialog() {
        String[] options = {"View Avatar", "Change Avatar"};

        new AlertDialog.Builder(this)
                .setTitle("Profile Picture")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // View Avatar
                        if (currentAvatarUrl != null && !currentAvatarUrl.isEmpty()) {
                            Intent intent = new Intent(this, ViewAvatarActivity.class);
                            intent.putExtra("avatarUrl", currentAvatarUrl);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "No avatar to show", Toast.LENGTH_SHORT).show();
                        }
                    } else if (which == 1) {
                        // Change Avatar
                        checkPermissionAndPickImage();
                    }
                })
                .show();
    }

    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_STORAGE_PERMISSION
                );
            } else {
                openGallery();
            }
        } else {
            // Android 12 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION
                );
            } else {
                openGallery();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void fetchProfile() {
        progressDialog.setMessage("Loading profile...");
        progressDialog.show();

        apiService.getProfile().enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserProfileResponse> call,
                                   @NonNull Response<UserProfileResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    UserProfileResponse user = response.body();
                    tvFullname.setText(user.getFullname());
                    tvUsername.setText("@" + user.getUsername());
                    tvEmail.setText(user.getEmail());
                    tvPhone.setText(user.getPhoneNumber());
                    currentAvatarUrl = user.getAvatarUrl(); // 🔹 store for later

                    if (currentAvatarUrl != null && !currentAvatarUrl.isEmpty()) {
                        Glide.with(ProfileActivity.this)
                                .load(currentAvatarUrl)
                                .placeholder(R.drawable.default_avatar)
                                .into(imgAvatar);
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserProfileResponse> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadAvatar() {
        if (selectedImageUri == null) return;

        progressDialog.setMessage("Uploading avatar...");
        progressDialog.show();

        File file = new File(FileUtils.getPath(this, selectedImageUri));
        RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        apiService.uploadAvatar(body).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call,
                                   @NonNull Response<Map<String, String>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    String newUrl = response.body().get("avatarUrl");
                    currentAvatarUrl = newUrl; // 🔹 update the new avatar URL

                    Glide.with(ProfileActivity.this)
                            .load(newUrl)
                            .placeholder(R.drawable.default_avatar)
                            .into(imgAvatar);

                    Toast.makeText(ProfileActivity.this, "Avatar updated!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
