package com.example.planforplant.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.planforplant.BuildConfig;
import com.example.planforplant.DTO.HealthResponse;
import com.example.planforplant.R;
import com.example.planforplant.api.HealthApi;
import com.example.planforplant.api.HealthClient;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HealthCaptureActivity extends NavigationBarActivity {

    private static final String TAG = "HealthCaptureActivity";
    private static final String API_KEY = BuildConfig.PLANT_ID_API_KEY;

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private View loadingLayout;

    private ActivityResultLauncher<String> requestCameraPermission;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capture_health);

        // Inflate ViewStub (đảm bảo PreviewView được nạp)
        ViewStub stub = findViewById(R.id.stubPreview);
        if (stub != null) stub.inflate();

        previewView = findViewById(R.id.previewView);
        if (previewView == null) {
            Toast.makeText(this, "Không tìm thấy PreviewView", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadingLayout = findViewById(R.id.loadingLayout);

        ImageButton btnCamera = findViewById(R.id.btnCamera);
        ImageButton btnGallery = findViewById(R.id.btnGallery);

        setupActivityResultLaunchers();

        // Xin quyền CAMERA
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA);
        }

        cameraExecutor = Executors.newSingleThreadExecutor();

        btnCamera.setOnClickListener(v -> takePhoto());
        btnGallery.setOnClickListener(v -> pickImage.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));
    }

    /** Cấu hình quyền truy cập và chọn ảnh **/
    private void setupActivityResultLaunchers() {
        requestCameraPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) startCamera();
                    else Toast.makeText(this, "Cần quyền Camera để sử dụng", Toast.LENGTH_SHORT).show();
                });

        pickImage = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) identifyHealth(uri);
                });
    }

    /** Khởi động camera preview **/
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture);

            } catch (Exception e) {
                Log.e(TAG, "Camera khởi động thất bại", e);
                Toast.makeText(this, "Không thể khởi động camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /** Chụp ảnh **/
    private void takePhoto() {
        if (imageCapture == null) return;

        File photoFile = new File(getExternalFilesDir(null),
                "health_" + System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        identifyHealth(Uri.fromFile(photoFile));
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(HealthCaptureActivity.this, "Chụp ảnh lỗi", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Gửi ảnh đến Plant.id API để phân tích sức khỏe cây **/
    private void identifyHealth(Uri imageUri) {
        File file = getFileFromUri(imageUri);
        if (file == null) {
            Toast.makeText(this, "Không thể đọc ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        runOnUiThread(() -> loadingLayout.setVisibility(View.VISIBLE));

        // Chuẩn bị multipart form
        RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData("images", file.getName(), requestFile);
        RequestBody similarImages = RequestBody.create("true", MultipartBody.FORM);
        RequestBody health = RequestBody.create("all", MultipartBody.FORM);

        // Gọi API Plant.id
        HealthApi api = HealthClient.getClient().create(HealthApi.class);
        Call<HealthResponse> call = api.identifyHealth(body, similarImages, health, API_KEY);

        call.enqueue(new Callback<HealthResponse>() {
            @Override
            public void onResponse(Call<HealthResponse> call, Response<HealthResponse> response) {
                runOnUiThread(() -> loadingLayout.setVisibility(View.GONE));
                if (response.isSuccessful() && response.body() != null) {
                    Intent intent = new Intent(HealthCaptureActivity.this, HealthResultActivity.class);
                    intent.putExtra("result", new Gson().toJson(response.body()));
                    startActivity(intent);
                } else {
                    Toast.makeText(HealthCaptureActivity.this, "Không phát hiện bệnh", Toast.LENGTH_LONG).show();
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Không rõ lỗi";
                        Log.e(TAG, "API error: " + response.code() + " - " + error);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<HealthResponse> call, Throwable t) {
                runOnUiThread(() -> loadingLayout.setVisibility(View.GONE));
                Toast.makeText(HealthCaptureActivity.this, "Lỗi API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "API failure", t);
            }
        });
    }

    /** Đọc file ảnh từ URI **/
    private File getFileFromUri(Uri uri) {
        try {
            if ("file".equals(uri.getScheme())) return new File(uri.getPath());
            String fileName = "picked_" + System.currentTimeMillis() + ".jpg";
            File tempFile = new File(getCacheDir(), fileName);

            try (InputStream inputStream = getContentResolver().openInputStream(uri);
                 OutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                return tempFile;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) cameraExecutor.shutdown();
    }
}
