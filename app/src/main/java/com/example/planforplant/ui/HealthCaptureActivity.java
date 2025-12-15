package com.example.planforplant.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
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
import com.example.planforplant.DTO.HealthResponse;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;
import com.example.planforplant.api.HealthApi;
import com.example.planforplant.api.HealthClient;
import com.example.planforplant.model.Disease;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HealthCaptureActivity extends NavigationBarActivity {

    private static final String TAG = "HEALTH_CAPTURE";
    private static final String API_KEY = BuildConfig.PLANT_ID_API_KEY;

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    private View loading;

    private ActivityResultLauncher<String> requestCameraPermission;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImage;

    private Uri lastCapturedUri;

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

        cameraExecutor = Executors.newSingleThreadExecutor();
        setupLaunchers();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA);
        }

        btnCamera.setOnClickListener(v -> takePhoto());

        btnGallery.setOnClickListener(v -> pickImage.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        ));
    }

    /** ======================
     *  SETUP LAUNCHERS
     * ====================== */
    private void setupLaunchers() {
        requestCameraPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) startCamera();
                    else toast("Cần quyền Camera để sử dụng");
                }
        );

        pickImage = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        lastCapturedUri = uri;
                        identifyDisease(uri);
                    }
                }
        );
    }

    /** ======================
     *  START CAMERA
     * ====================== */
    private void startCamera() {
        if (previewView == null) {
            Log.e(TAG, "previewView is null - cannot start camera");
            return;
        }

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
                Log.e(TAG, "startCamera failed", e);
                toast("Không thể khởi động Camera");
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /** ======================
     *  CAPTURE PHOTO
     * ====================== */
    private void takePhoto() {
        if (imageCapture == null) {
            toast("Camera chưa sẵn sàng");
            return;
        }
        if (cameraExecutor == null || cameraExecutor.isShutdown()) {
            cameraExecutor = Executors.newSingleThreadExecutor();
        }

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
                        runOnUiThread(() -> {
                            lastCapturedUri = Uri.fromFile(file);
                            identifyDisease(lastCapturedUri);
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "takePicture error", exception);
                        runOnUiThread(() -> toast("Chụp ảnh lỗi"));
                    }
                }
        );
    }

    /** ==================================================
     *  SEND BASE64 TO PLANT.ID V3 (SAFE FOR ALL ANDROID)
     * ================================================== */
    private void identifyDisease(Uri uri) {
        if (uri == null) {
            toast("Ảnh không hợp lệ");
            return;
        }

        showLoading(true);

        // ✅ đọc bytes trực tiếp từ Uri (KHÔNG dùng Files.readAllBytes)
        final byte[] bytes;
        try {
            bytes = readBytesFromUri(uri);
            if (bytes == null || bytes.length == 0) {
                showLoading(false);
                toast("Ảnh rỗng / không đọc được");
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Read bytes from uri failed: " + uri, e);
            showLoading(false);
            toast("Không đọc được ảnh");
            return;
        }

        final String base64;
        try {
            base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, "Base64 encode failed", e);
            showLoading(false);
            toast("Lỗi mã hóa ảnh");
            return;
        }

        String dataUri = "data:image/jpeg;base64," + base64;

        Map<String, Object> body = new HashMap<>();
        body.put("images", Collections.singletonList(dataUri));

        HealthApi api = HealthClient.getClient().create(HealthApi.class);

        api.assessHealth(
                "local_name,description,url,treatment,classification,common_names,cause",
                "en",
                true,
                API_KEY,
                body
        ).enqueue(new Callback<HealthResponse>() {

            @Override
            public void onResponse(Call<HealthResponse> call, Response<HealthResponse> res) {
                showLoading(false);

                if (!res.isSuccessful() || res.body() == null) {
                    Log.e(TAG, "Plant.id response not successful. code=" + res.code());
                    toast("Không phát hiện bệnh");
                    return;
                }

                try {
                    handleDetectedDisease(res.body());
                } catch (Exception e) {
                    Log.e(TAG, "handleDetectedDisease crashed", e);
                    toast("Lỗi xử lý kết quả");
                }
            }

            @Override
            public void onFailure(Call<HealthResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Plant.id API failure", t);

                // t.getMessage() đôi khi null -> show class name cho chắc
                toast("Lỗi API: " + t.getClass().getSimpleName());
            }
        });
    }

    /** ================================
     *  PARSE RESULT & GO TO DETAIL PAGE
     * ================================ */
    private void handleDetectedDisease(HealthResponse response) {
        if (response == null || response.getResult() == null
                || response.getResult().getDisease() == null) {
            toast("Kết quả không hợp lệ");
            return;
        }

        List<HealthResponse.Suggestion> list =
                response.getResult().getDisease().getSuggestions();

        if (list == null || list.isEmpty()) {
            toast("Không nhận diện được bệnh");
            return;
        }

        // Chọn bệnh có xác suất cao nhất
        HealthResponse.Suggestion best = list.get(0);
        for (HealthResponse.Suggestion s : list) {
            if (s != null && s.probability > best.probability) best = s;
        }

        String detectedName = best.name;
        HealthResponse.Suggestion finalBest = best;

        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);

        api.fuzzySearch(detectedName).enqueue(new Callback<List<Disease>>() {

            @Override
            public void onResponse(Call<List<Disease>> call, Response<List<Disease>> res) {
                if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {

                    Disease matchedDisease = res.body().get(0);

                    Intent intent = new Intent(HealthCaptureActivity.this, DiseaseDetailActivity.class);
                    intent.putExtra("diseaseId", matchedDisease.getId());
                    startActivity(intent);

                } else {
                    Intent i = new Intent(HealthCaptureActivity.this, DiseaseNotFoundActivity.class);
                    i.putExtra("diseaseName", finalBest.name);
                    i.putExtra("probability", finalBest.probability);
                    i.putExtra("description", finalBest.description);
                    i.putExtra("imageUrl", finalBest.url);

                    if (lastCapturedUri != null)
                        i.putExtra("capturedImageUri", lastCapturedUri.toString());

                    startActivity(i);
                }
            }

            @Override
            public void onFailure(Call<List<Disease>> call, Throwable t) {
                Log.e(TAG, "Local server fuzzySearch failure", t);
                toast("Lỗi server: " + t.getClass().getSimpleName());
            }
        });
    }

    /** ======================
     *  Read bytes from Uri
     * ====================== */
    private byte[] readBytesFromUri(Uri uri) throws Exception {
        InputStream in = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            if ("file".equals(uri.getScheme())) {
                File f = new File(uri.getPath());
                in = new FileInputStream(f);
            } else {
                in = getContentResolver().openInputStream(uri);
            }

            if (in == null) return null;

            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            return out.toByteArray();

        } finally {
            try { if (in != null) in.close(); } catch (Exception ignored) {}
            try { out.close(); } catch (Exception ignored) {}
        }
    }

    private void showLoading(boolean show) {
        runOnUiThread(() -> {
            if (loading != null) {
                loading.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void toast(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) cameraExecutor.shutdown();
    }
}
