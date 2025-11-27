package com.example.planforplant.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
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


    /** ======================
     *  SETUP LAUNCHERS
     * ====================== */
    private void setupLaunchers() {
        requestPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) startCamera();
                    else runOnUiThread(() ->
                            Toast.makeText(this, "Cần quyền Camera để sử dụng", Toast.LENGTH_SHORT).show()
                    );
                });

        pickImage = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        runOnUiThread(() -> identifyDisease(uri));
                    }
                });
    }


    /** ======================
     *  START CAMERA
     * ====================== */
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
                runOnUiThread(() ->
                        Toast.makeText(this, "Không thể khởi động Camera", Toast.LENGTH_SHORT).show()
                );
            }
        }, ContextCompat.getMainExecutor(this));
    }


    /** ======================
     *  CAPTURE PHOTO
     * ====================== */
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
                        runOnUiThread(() ->
                                identifyDisease(Uri.fromFile(file))
                        );
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        runOnUiThread(() ->
                                Toast.makeText(HealthCaptureActivity.this, "Chụp ảnh lỗi", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
        );
    }


    /** ==================================================
     *  SEND BASE64 TO PLANT.ID V3 (FIX UI THREAD CRASH)
     * ================================================== */
    private void identifyDisease(Uri uri) {

        File file = getFileFromUri(uri);
        if (file == null) {
            Toast.makeText(this, "Không đọc được ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        runOnUiThread(() -> loading.setVisibility(View.VISIBLE));

        String base64;
        try {
            byte[] bytes = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                bytes = Files.readAllBytes(file.toPath());
            }
            base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            runOnUiThread(() ->
                    Toast.makeText(this, "Lỗi mã hóa ảnh", Toast.LENGTH_SHORT).show()
            );
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
                runOnUiThread(() -> loading.setVisibility(View.GONE));

                if (!res.isSuccessful() || res.body() == null) {
                    runOnUiThread(() ->
                            Toast.makeText(HealthCaptureActivity.this, "Không phát hiện bệnh", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                handleDetectedDisease(res.body());
            }

            @Override
            public void onFailure(Call<HealthResponse> call, Throwable t) {
                runOnUiThread(() -> {
                    loading.setVisibility(View.GONE);
                    Toast.makeText(HealthCaptureActivity.this,
                            "Lỗi API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }


    /** ================================
     *  PARSE RESULT & GO TO DETAIL PAGE
     * ================================ */
    private void handleDetectedDisease(HealthResponse response) {

        List<HealthResponse.Suggestion> list =
                response.getResult().getDisease().getSuggestions();

        if (list == null || list.isEmpty()) {
            runOnUiThread(() ->
                    Toast.makeText(this, "Không nhận diện được bệnh", Toast.LENGTH_SHORT).show()
            );
            return;
        }

        // Pick best suggestion
        HealthResponse.Suggestion best = list.get(0);
        for (HealthResponse.Suggestion s : list)
            if (s.probability > best.probability) best = s;

        String detectedName = best.name;

        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);

        HealthResponse.Suggestion finalBest = best;
        api.searchDiseases(detectedName).enqueue(new Callback<List<Disease>>() {
            @Override
            public void onResponse(Call<List<Disease>> call, Response<List<Disease>> res) {

                if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {

                    Disease matchedDisease = res.body().get(0);

                    Intent intent = new Intent(HealthCaptureActivity.this, DiseaseDetailActivity.class);
                    intent.putExtra("diseaseId", matchedDisease.getId());
                    startActivity(intent);

                } else {

                    // 🔥 CHUYỂN SANG TRANG NOT FOUND
                    Intent i = new Intent(HealthCaptureActivity.this, DiseaseNotFoundActivity.class);
                    i.putExtra("diseaseName", finalBest.name);
                    i.putExtra("probability", finalBest.probability);
                    i.putExtra("description", finalBest.description);
                    i.putExtra("imageUrl", finalBest.url);   // nếu Plant.id có trả URL ảnh
                    startActivity(i);
                }
            }

            @Override
            public void onFailure(Call<List<Disease>> call, Throwable t) {
                runOnUiThread(() ->
                        Toast.makeText(HealthCaptureActivity.this,
                                "Lỗi server: " + t.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }


    /** ======================
     *  Convert URI → File
     * ====================== */
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
            return null;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) cameraExecutor.shutdown();
    }
}
