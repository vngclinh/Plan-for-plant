package com.example.planforplant.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.planforplant.BuildConfig;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;
import com.example.planforplant.api.HealthApi;
import com.example.planforplant.api.HealthClient;
import com.example.planforplant.model.Disease;
import com.example.planforplant.DTO.HealthResponse;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
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

    private View loading;

    private ActivityResultLauncher<String> requestPermission;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImage;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capture_health);

        ViewStub stub = findViewById(R.id.stubPreview);
        if (stub != null) stub.inflate();

        previewView = findViewById(R.id.previewView);
        loading = findViewById(R.id.loadingLayout);

        ImageButton btnCamera = findViewById(R.id.btnCamera);
        ImageButton btnGallery = findViewById(R.id.btnGallery);

        setupLaunchers();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermission.launch(Manifest.permission.CAMERA);
        }

        cameraExecutor = Executors.newSingleThreadExecutor();

        btnCamera.setOnClickListener(v -> takePhoto());
        btnGallery.setOnClickListener(v -> pickImage.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        ));
    }


    private void setupLaunchers() {
        requestPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) startCamera();
                    else Toast.makeText(this, "Cần quyền Camera để sử dụng", Toast.LENGTH_SHORT).show();
                });

        pickImage = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) identifyDisease(uri);
                });
    }


    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider provider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build();

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                provider.unbindAll();
                provider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                );

            } catch (Exception e) {
                Toast.makeText(this, "Không thể khởi động Camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }


    private void takePhoto() {
        if (imageCapture == null) return;

        File file = new File(getExternalFilesDir(null),
                "health_" + System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(file).build();

        imageCapture.takePicture(
                outputOptions,
                cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        identifyDisease(Uri.fromFile(file));
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(HealthCaptureActivity.this, "Chụp ảnh lỗi", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }



    /** =========================
     *  GỬI ẢNH LÊN PLANT.ID
     * ========================= */
    private void identifyDisease(Uri uri) {

        File file = getFileFromUri(uri);
        if (file == null) {
            Toast.makeText(this, "Không đọc được ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        loading.setVisibility(View.VISIBLE);

        RequestBody req = RequestBody.create(file, MediaType.parse("image/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData("images", file.getName(), req);

        RequestBody similar = RequestBody.create("true", MultipartBody.FORM);
        RequestBody health = RequestBody.create("all", MultipartBody.FORM);

        HealthApi api = HealthClient.getClient().create(HealthApi.class);

        api.identifyHealth(body, similar, health, API_KEY)
                .enqueue(new Callback<HealthResponse>() {
                    @Override
                    public void onResponse(Call<HealthResponse> call, Response<HealthResponse> res) {
                        loading.setVisibility(View.GONE);

                        if (!res.isSuccessful() || res.body() == null) {
                            Toast.makeText(HealthCaptureActivity.this, "Không phát hiện bệnh", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        handleDetectedDisease(res.body());
                    }

                    @Override
                    public void onFailure(Call<HealthResponse> call, Throwable t) {
                        loading.setVisibility(View.GONE);
                        Toast.makeText(HealthCaptureActivity.this, "Lỗi API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /** =======================================
     *  LẤY BỆNH TRONG DB VÀ MỞ TRANG CHI TIẾT
     * ======================================= */
    private void handleDetectedDisease(HealthResponse response) {

        if (response == null ||
                response.getResult() == null ||
                response.getResult().getDisease() == null ||
                response.getResult().getDisease().suggestions == null ||
                response.getResult().getDisease().suggestions.isEmpty()) {

            Toast.makeText(this, "Không nhận diện được bệnh", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy bệnh có xác suất cao nhất
        String detectedName = response.getResult().getDisease().suggestions.get(0).name;

        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);

        api.searchDiseases(detectedName).enqueue(new Callback<List<Disease>>() {
            @Override
            public void onResponse(Call<List<Disease>> call, Response<List<Disease>> res) {

                if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {

                    Disease matchedDisease = res.body().get(0);

                    Intent intent = new Intent(HealthCaptureActivity.this, DiseaseDetailActivity.class);
                    intent.putExtra("diseaseId", matchedDisease.getId());
                    startActivity(intent);

                } else {
                    Toast.makeText(HealthCaptureActivity.this,
                            "Bệnh '" + detectedName + "' chưa có trong hệ thống!",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Disease>> call, Throwable t) {
                Toast.makeText(HealthCaptureActivity.this,
                        "Lỗi server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File getFileFromUri(Uri uri) {
        try {
            if ("file".equals(uri.getScheme())) return new File(uri.getPath());

            File temp = new File(getCacheDir(), "picked_" + System.currentTimeMillis() + ".jpg");
            try (InputStream in = getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(temp)) {

                byte[] buf = new byte[4096];
                int len;
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            }
            return temp;

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
