package com.example.planforplant.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.PlantNetApi;
import com.example.planforplant.model.PlantResponse;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.example.planforplant.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CaptureActivity extends AppCompatActivity {

    private static final String TAG = "CaptureActivity";

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    private String selectedOption = "Lá"; // mặc định ban đầu

    // Activity Result APIs
    private ActivityResultLauncher<String> requestCameraPermission;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capture);

        // Inflate ViewStub ngay sau setContentView
        ViewStub stub = findViewById(R.id.stubPreview);
        if (stub != null) {
            stub.inflate();
        }
        previewView = findViewById(R.id.previewView);
        if (previewView == null) {
            Toast.makeText(this, "Không tìm thấy PreviewView", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);

        ImageButton btnCamera = findViewById(R.id.btnCamera);
        ImageButton btnGallery = findViewById(R.id.btnGallery);

        setupCaptureOptions();
        setupActivityResultLaunchers();

        // Xin quyền CAMERA nếu chưa có, có rồi thì khởi động camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA);
        }

        cameraExecutor = Executors.newSingleThreadExecutor();

        btnCamera.setOnClickListener(v -> takePhoto());
        btnGallery.setOnClickListener(v ->
                pickImage.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build())
        );
    }

    private void setupActivityResultLaunchers() {
        // Quyền CAMERA
        requestCameraPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        startCamera();
                    } else {
                        Toast.makeText(this, "Bạn cần cấp quyền Camera để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
                    }
                });

        // Chọn ảnh (Photo Picker – không cần READ_MEDIA_IMAGES)
        pickImage = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        goToDetail(uri);
                    }
                });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Xoay đúng theo display hiện tại (giúp preview/capture ổn định)
                int rotation = previewView.getDisplay() != null
                        ? previewView.getDisplay().getRotation()
                        : Surface.ROTATION_0;

                Preview preview = new Preview.Builder()
                        .setTargetRotation(rotation)
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3) // thường hợp với camera
                        .build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(rotation)
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture
                );
            } catch (Exception e) {
                Log.e(TAG, "Use case binding failed", e);
                Toast.makeText(this, "Không thể khởi động camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile = new File(
                getExternalFilesDir(null),
                "plant_" + System.currentTimeMillis() + ".jpg"
        );

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri uri = Uri.fromFile(photoFile);
                        goToDetail(uri);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
                        Toast.makeText(CaptureActivity.this, "Chụp ảnh thất bại", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

//    private void goToDetail(@NonNull Uri imageUri) {
//        Intent intent = new Intent(CaptureActivity.this, DetailActivity.class);
//        intent.putExtra("imageUri", imageUri.toString());
//        intent.putExtra("selectedOption", selectedOption);
//        startActivity(intent);
//    }
    private void goToDetail(@NonNull Uri imageUri) {
        identifyPlant(imageUri);
    }


    private void setupCaptureOptions() {
        TextView optionFlower = findViewById(R.id.optionFlower);
        TextView optionLeaf = findViewById(R.id.optionLeaf);
        TextView optionWhole = findViewById(R.id.optionWhole);

        View.OnClickListener listener = v -> {
            resetOptionsBackground();
            ((TextView) v).setBackgroundResource(R.drawable.option_selected);
            selectedOption = ((TextView) v).getText().toString();
        };

        optionFlower.setOnClickListener(listener);
        optionLeaf.setOnClickListener(listener);
        optionWhole.setOnClickListener(listener);

        // mặc định highlight "Lá"
        optionLeaf.setBackgroundResource(R.drawable.option_selected);
    }

    private void resetOptionsBackground() {
        findViewById(R.id.optionFlower).setBackgroundResource(R.drawable.option_selector);
        findViewById(R.id.optionLeaf).setBackgroundResource(R.drawable.option_selector);
        findViewById(R.id.optionWhole).setBackgroundResource(R.drawable.option_selector);
    }
    private String mapOptionToOrgan(String option) {
        switch (option) {
            case "Lá":
                return "leaf";
            case "Hoa":
                return "flower";
            case "Cả cây":
                return "whole";
            default:
                return "leaf"; // fallback
        }
    }
    private void identifyPlant(Uri imageUri) {
        File file = getFileFromUri(imageUri);
        if (file == null || !file.exists()) {
            Toast.makeText(this, "Không thể đọc ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuẩn bị multipart file
        RequestBody requestFile = RequestBody.create(file, okhttp3.MediaType.parse("image/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData("images", file.getName(), requestFile);

        // Organ
        String organ = mapOptionToOrgan(selectedOption);
        RequestBody organPart = RequestBody.create(organ, MultipartBody.FORM);

        // Gọi API
        PlantNetApi api = ApiClient.getClient().create(PlantNetApi.class);
        Call<PlantResponse> call = api.identify(body, organPart, BuildConfig.PLANTNET_API_KEY);

        call.enqueue(new Callback<PlantResponse>() {
            @Override
            public void onResponse(Call<PlantResponse> call, Response<PlantResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PlantResponse plantResponse = response.body();
                    Intent intent = new Intent(CaptureActivity.this, DetailActivity.class);
                    intent.putExtra("imageUri", imageUri.toString());
                    intent.putExtra("plantResponseJson", new Gson().toJson(plantResponse));
                    startActivity(intent);
                } else {
                    Toast.makeText(CaptureActivity.this, "API trả về lỗi", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "API error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PlantResponse> call, Throwable t) {
                Toast.makeText(CaptureActivity.this, "Gọi API thất bại", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "API call failed", t);
            }
        });
    }

    private File getFileFromUri(Uri uri) {
        try {
            if ("file".equals(uri.getScheme())) {
                // Trường hợp ảnh từ camera
                return new File(uri.getPath());
            } else if ("content".equals(uri.getScheme())) {
                // Trường hợp ảnh từ thư viện
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        // optional: clear cache images
        File cacheDir = getCacheDir();
        for (File f : cacheDir.listFiles()) {
            if (f.getName().startsWith("picked_")) {
                f.delete();
            }
        }
    }

}
