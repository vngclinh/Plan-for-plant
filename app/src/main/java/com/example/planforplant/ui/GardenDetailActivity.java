package com.example.planforplant.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;

import androidx.appcompat.app.AlertDialog;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.planforplant.DTO.AddDiaryRequest;
import com.example.planforplant.DTO.DiaryResponse;
import com.example.planforplant.DTO.GardenDiseaseResponse;
import com.example.planforplant.DTO.GardenImageResponse;
import com.example.planforplant.DTO.GardenResponse;
import com.example.planforplant.DTO.GardenScheduleResponse;
import com.example.planforplant.DTO.UserResponse;
import com.example.planforplant.DTO.GardenUpdateRequest;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;
import com.example.planforplant.model.Disease;
import com.example.planforplant.model.Plant;
import com.example.planforplant.utils.ImagePreviewDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GardenDetailActivity extends NavigationBarActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 200;

    private FusedLocationProviderClient fusedLocationClient;
    private ProgressDialog progressDialog;

    private ImageView imgPlant;
    private TextView tvCommonName, tvNickname, tvStatus, tvDateAdded;
    private TextView tvOverview, tvFamily, tvGenus, tvSpecies;
    private TextView tvPhylum, tvClass, tvOrder;
    private TextView tvWater, tvLight, tvTemperature, tvCareGuide;
    private TextView tvHealthy;

    private RecyclerView recyclerDiseases;
    private GardenDiseaseAdapter diseaseAdapter;

    private RecyclerView recyclerGallery;
    private TextView tvEmptyGallery;
    private com.example.planforplant.ui.GardenImageAdapter imageAdapter;
    private static final int REQUEST_PICK_IMAGE = 101;
    private Long gardenId;

    private List<GardenDiseaseResponse> currentActiveDiseases = new ArrayList<>();

    private ActivityResultLauncher<String> requestCameraPermission;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMultipleLauncher;


    private ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri photoUri;
    // === Diary ===
    private RecyclerView recyclerDiary;
    private TextView tvEmptyDiary;
    private ImageView btnAddDiary, btnDeleteDiary;
    private DiaryAdapter diaryAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garden_detail);
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);


        // === Bind các view chính ===
        imgPlant = findViewById(R.id.imgPlant);
        tvCommonName = findViewById(R.id.tvCommonName);
        tvNickname = findViewById(R.id.tvNickname);
        tvStatus = findViewById(R.id.tvStatus);
        tvDateAdded = findViewById(R.id.tvDateAdded);

        // === Bind các view thông tin chi tiết về cây ===
        tvOverview = findViewById(R.id.tvOverview);
        tvFamily = findViewById(R.id.tvFamily);
        tvGenus = findViewById(R.id.tvGenus);
        tvSpecies = findViewById(R.id.tvSpecies);
        tvPhylum = findViewById(R.id.tvPhylum);
        tvClass = findViewById(R.id.tvClass);
        tvOrder = findViewById(R.id.tvOrder);
        tvWater = findViewById(R.id.tvWater);
        tvLight = findViewById(R.id.tvLight);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvCareGuide = findViewById(R.id.tvCareGuide);
        recyclerDiseases = findViewById(R.id.recyclerDiseases);
        tvHealthy = findViewById(R.id.tvHealthy);

        diseaseAdapter = new GardenDiseaseAdapter(this);
        recyclerDiseases.setLayoutManager(new LinearLayoutManager(this));
        recyclerDiseases.setAdapter(diseaseAdapter);




        // === Nhận dữ liệu từ Intent ===
        Intent intent = getIntent();
        String nickname = intent.getStringExtra("nickname");
        String status = intent.getStringExtra("status");
        String dateAdded = (intent.getStringExtra("dateAdded") != null)
                ? intent.getStringExtra("dateAdded").substring(0, 10)
                : "Không rõ";

        gardenId = getIntent().getLongExtra("gardenId", -1);
        if (gardenId == -1) {
            Toast.makeText(this, "Invalid garden ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadDiseases(gardenId);
        long plantId = intent.getLongExtra("plantId", -1);


        // === Gán dữ liệu Garden ===
        tvNickname.setText("Tên riêng: " + (nickname != null ? nickname : "Chưa đặt"));
        tvStatus.setText("Trạng thái: "+ getStatusDisplay(status));
        tvDateAdded.setText("Ngày thêm: " + (dateAdded != null ? dateAdded : "Không xác định"));


        if (plantId != -1) {
            loadPlantFromAPI(plantId);
        }



        ImageView btnMore = findViewById(R.id.btn_more);
        btnMore.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, v);
            popupMenu.getMenuInflater().inflate(R.menu.menu_garden_detail, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_edit) {
                    showEditGardenDialog();
                    return true;
                } else if (id == R.id.action_delete){
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Xác nhận xoá")
                            .setMessage("Bạn có chắc chắn muốn xoá cây này khỏi vườn không?")
                            .setPositiveButton("Xoá", (dialog, which) -> {
                                deleteGarden(gardenId);
                            })
                            .setNegativeButton("Huỷ", (dialog, which) -> dialog.dismiss())
                            .show();
                    return true;
                } else if (id == R.id.action_auto_gen){
                    generateAutoWateringSchedule();
                    return true;
                } else if (id == R.id.action_gen_disease){
                    showGenerateConfirmationDialog(gardenId);
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });

        recyclerGallery = findViewById(R.id.recycler_gallery);
        tvEmptyGallery = findViewById(R.id.tvEmptyGallery);
        ImageView btnCamera = findViewById(R.id.btnCamera);
        ImageView btnAdd = findViewById(R.id.btnAdd);
        ImageView btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setAlpha(0.3f);
        btnDelete.setEnabled(false);

        recyclerGallery.setLayoutManager(new GridLayoutManager(this, 3));
        imageAdapter = new com.example.planforplant.ui.GardenImageAdapter(
                this,
                new ArrayList<>(),
                count -> {
                    boolean hasSelection = count > 0;
                    btnDelete.setAlpha(hasSelection ? 1f : 0.3f);
                    btnDelete.setEnabled(hasSelection);
                },
                image -> {
                    ImagePreviewDialog.show(this, image.getImageUrl());
                }
        );
        recyclerGallery.setAdapter(imageAdapter);
        loadGardenImages();
        setupActivityResultLaunchers();

        btnCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                requestCameraPermission.launch(Manifest.permission.CAMERA);
            }
        });

        btnAdd.setOnClickListener(v -> {
            // chọn nhiều ảnh
            pickMultipleLauncher.launch(
                    new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build());
        });
        btnDelete.setOnClickListener(v -> {
            List<Long> selectedIds = imageAdapter.getSelectedIds();
            if (selectedIds.isEmpty()) return;

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Xoá ảnh")
                    .setMessage("Bạn có chắc muốn xoá " + selectedIds.size() + " ảnh không?")
                    .setPositiveButton("Xoá", (dialog, which) -> deleteSelectedImages(selectedIds))
                    .setNegativeButton("Huỷ", null)
                    .show();
        });
        // === Setup Diary ===
        recyclerDiary = findViewById(R.id.recycler_diary);
        tvEmptyDiary = findViewById(R.id.tvEmptyDiary);
        btnAddDiary = findViewById(R.id.btnAddDiary);
        btnDeleteDiary = findViewById(R.id.btnDeleteDiary);
        btnDeleteDiary.setAlpha(0.3f);
        btnDeleteDiary.setEnabled(false);

        recyclerDiary.setLayoutManager(new LinearLayoutManager(this));
        diaryAdapter = new DiaryAdapter(
                this,
                new ArrayList<>(),
                count -> {
                    boolean hasSelection = count > 0;
                    btnDeleteDiary.setAlpha(hasSelection ? 1f : 0.3f);
                    btnDeleteDiary.setEnabled(hasSelection);
                }
        );
        recyclerDiary.setAdapter(diaryAdapter);

// Load danh sách nhật ký
        loadGardenDiaries();

// Thêm nhật ký
        btnAddDiary.setOnClickListener(v -> showAddDiaryDialog());

// Xoá nhật ký
        btnDeleteDiary.setOnClickListener(v -> {
            List<Long> selectedIds = diaryAdapter.getSelectedIds();
            if (selectedIds.isEmpty()) return;

            new AlertDialog.Builder(this)
                    .setTitle("Xoá nhật ký")
                    .setMessage("Bạn có chắc muốn xoá " + selectedIds.size() + " nhật ký không?")
                    .setPositiveButton("Xoá", (dialog, which) -> deleteSelectedDiaries(selectedIds))
                    .setNegativeButton("Huỷ", null)
                    .show();
        });

        ActivityResultLauncher<Intent> searchDiseaseLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // DiseaseDetail đã thêm bệnh thành công
                        loadDiseases(gardenId); // reload danh sách bệnh
                    }
                }
        );


        ImageView btnAddDisease = findViewById(R.id.btnAddDisease);
        btnAddDisease.setOnClickListener(v -> {
            Intent searchDiseaseIntent = new Intent(GardenDetailActivity.this, SearchDiseaseActivity.class);
            searchDiseaseIntent.putExtra("gardenId", gardenId);
            searchDiseaseIntent.putExtra("nickname", nickname);
            searchDiseaseIntent.putExtra("status", status);
            searchDiseaseIntent.putExtra("dateAdded", dateAdded);
            searchDiseaseIntent.putExtra("plantId", plantId);
            startActivity(searchDiseaseIntent);
        });
    }

    private void loadDiseases(long gardenId) {
        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);

        api.getDiseasesByGardenId(gardenId).enqueue(new Callback<List<GardenDiseaseResponse>>() {
            @Override
            public void onResponse(Call<List<GardenDiseaseResponse>> call, Response<List<GardenDiseaseResponse>> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    // Show error text
                    tvHealthy.setText("Không thể tải thông tin bệnh");
                    tvHealthy.setTextColor(Color.RED);
                    tvHealthy.setVisibility(View.VISIBLE);
                    diseaseAdapter.setData(Collections.emptyList());
                    return;
                }

                // Filter ACTIVE diseases
                List<GardenDiseaseResponse> activeList = new ArrayList<>();
                for (GardenDiseaseResponse d : response.body()) {
                    if ("ACTIVE".equalsIgnoreCase(d.getStatus())) {
                        activeList.add(d);
                    }
                }

                currentActiveDiseases = activeList;

                if (activeList.isEmpty()) {
                    // No active disease → show healthy
                    tvHealthy.setText("Hiện tại cây khoẻ mạnh 🌱");
                    tvHealthy.setTextColor(Color.parseColor("#2E7D32")); // green
                    tvHealthy.setVisibility(View.VISIBLE);
                } else {
                    // Show disease list
                    tvHealthy.setVisibility(View.GONE);
                }

                recyclerDiseases.setVisibility(View.VISIBLE);
                diseaseAdapter.setData(activeList);
            }

            @Override
            public void onFailure(Call<List<GardenDiseaseResponse>> call, Throwable t) {
                // API failure → show error
                tvHealthy.setText("Không thể tải thông tin bệnh");
                tvHealthy.setTextColor(Color.RED);
                tvHealthy.setVisibility(View.VISIBLE);
                diseaseAdapter.setData(Collections.emptyList());
            }
        });
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (gardenId != -1) {
            loadDiseases(gardenId);
        }
    }

    private void loadGardenDiaries() {
        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);
        api.getDiariesByGardenId(gardenId).enqueue(new Callback<List<DiaryResponse>>() {
            @Override
            public void onResponse(Call<List<DiaryResponse>> call, Response<List<DiaryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DiaryResponse> list = response.body();
                    if (list.isEmpty()) {
                        recyclerDiary.setVisibility(View.GONE);
                        tvEmptyDiary.setVisibility(View.VISIBLE);
                    } else {
                        recyclerDiary.setVisibility(View.VISIBLE);
                        tvEmptyDiary.setVisibility(View.GONE);
                        diaryAdapter.setData(list);
                    }
                } else {
                    Toast.makeText(GardenDetailActivity.this, "Không tải được nhật ký", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<DiaryResponse>> call, Throwable t) {
                Toast.makeText(GardenDetailActivity.this, "Lỗi tải nhật ký: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showAddDiaryDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_diary, null);
        EditText etContent = dialogView.findViewById(R.id.etDiaryContent);

        new AlertDialog.Builder(this)
                .setTitle("Thêm nhật ký mới")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String content = etContent.getText().toString().trim();
                    if (content.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập nội dung", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    addDiaryEntry(content);
                })
                .setNegativeButton("Huỷ", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void addDiaryEntry(String content) {
        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);
        AddDiaryRequest request = new AddDiaryRequest(content);

        api.addDiaryEntry(gardenId, request).enqueue(new Callback<GardenResponse>() {
            @Override
            public void onResponse(Call<GardenResponse> call, Response<GardenResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(GardenDetailActivity.this, "Đã thêm nhật ký 🌿", Toast.LENGTH_SHORT).show();
                    loadGardenDiaries();
                } else {
                    Toast.makeText(GardenDetailActivity.this, "Lỗi khi thêm nhật ký", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GardenResponse> call, Throwable t) {
                Toast.makeText(GardenDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void deleteSelectedDiaries(List<Long> diaryIds) {
        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);

        for (Long id : diaryIds) {
            api.removeDiaryEntry(gardenId, id).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        loadGardenDiaries(); // Refresh danh sách
                    } else {
                        Toast.makeText(GardenDetailActivity.this, "Không xoá được nhật ký", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(GardenDetailActivity.this, "Lỗi xoá: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String getStatusDisplay(String status) {
        if (status == null) return "Không xác định";
        switch (status.toUpperCase()) {
            case "ALIVE":
                return "Đang phát triển";
            case "DEAD":
                return "Cây đã chết";
            default:
                return "Không xác định";
        }
    }
    private void deleteSelectedImages(List<Long> ids) {
        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);

        for (Long id : ids) {
            api.deleteGardenImage(id).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        loadGardenImages(); // Refresh gallery
                    }
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(GardenDetailActivity.this, "Lỗi xoá ảnh: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupActivityResultLaunchers() {
        requestCameraPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) openCamera();
                    else Toast.makeText(this, "Cần quyền camera để chụp ảnh", Toast.LENGTH_SHORT).show();
                }
        );

        // chọn 1 ảnh
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) uploadSelectedImages(List.of(uri));
                }
        );

        // chọn nhiều ảnh
        pickMultipleLauncher = registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) uploadSelectedImages(uris);
                }
        );
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && photoUri != null) {
                        uploadSelectedImages(List.of(photoUri));
                    }
                }
        );
    }

    private void openCamera() {
        try {
            File photoFile = new File(getExternalFilesDir(null),
                    "garden_" + System.currentTimeMillis() + ".jpg");
            photoUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    photoFile
            );
            takePictureLauncher.launch(photoUri);
        } catch (Exception e) {
            Toast.makeText(this, "Không mở được camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadSelectedImages(List<Uri> uris) {
        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);

        for (Uri uri : uris) {
            File file = getFileFromUri(uri);
            if (file == null) continue;

            RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            api.uploadGardenImage(gardenId, body).enqueue(new Callback<GardenImageResponse>() {
                @Override
                public void onResponse(Call<GardenImageResponse> call, Response<GardenImageResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(GardenDetailActivity.this, "Tải ảnh thành công", Toast.LENGTH_SHORT).show();
                        loadGardenImages(); // refresh lại recyclerView
                    } else {
                        Toast.makeText(GardenDetailActivity.this, "Lỗi tải ảnh", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<GardenImageResponse> call, Throwable t) {
                    Toast.makeText(GardenDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
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
    private void loadGardenImages() {
        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);
        api.getGardenImages(gardenId).enqueue(new Callback<List<GardenImageResponse>>() {
            @Override
            public void onResponse(Call<List<GardenImageResponse>> call, Response<List<GardenImageResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GardenImageResponse> list = response.body();
                    if (list.isEmpty()) {
                        recyclerGallery.setVisibility(View.GONE);
                        tvEmptyGallery.setVisibility(View.VISIBLE);
                    } else {
                        recyclerGallery.setVisibility(View.VISIBLE);
                        tvEmptyGallery.setVisibility(View.GONE);
                        imageAdapter.setData(list);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<GardenImageResponse>> call, Throwable t) {
                Toast.makeText(GardenDetailActivity.this, "Không tải được ảnh", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindPlantEntity(Plant plant) {
        if (plant == null) return;

        // Thông tin cơ bản
        tvCommonName.setText(plant.getCommonName() != null ? plant.getCommonName() : "Unknown");
        tvOverview.setText(plant.getDescription() != null ? plant.getDescription() : "No description available");

        // Phân loại khoa học
        tvPhylum.setText(plant.getPhylum() != null ? plant.getPhylum() : "");
        tvClass.setText(plant.getPlantClass() != null ? plant.getPlantClass() : "");
        tvOrder.setText(plant.getPlantOrder() != null ? plant.getPlantOrder() : "");
        tvFamily.setText(plant.getFamily() != null ? plant.getFamily() : "");
        tvGenus.setText(plant.getGenus() != null ? plant.getGenus() : "");
        tvSpecies.setText(plant.getSpecies() != null ? plant.getSpecies() : "");

        // Điều kiện sinh trưởng
        tvWater.setText(plant.getWaterSchedule() != null ? plant.getWaterSchedule() : "");
        tvLight.setText(plant.getLight() != null ? plant.getLight() : "");
        tvTemperature.setText(plant.getTemperature() != null ? plant.getTemperature() : "");

        // Hướng dẫn chăm sóc
        tvCareGuide.setText(plant.getCareguide() != null ? plant.getCareguide() : "");
        // Ảnh cây
        if (plant.getImageUrl() != null && !plant.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(plant.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(imgPlant);
        }
    }

    private void loadPlantFromAPI(long plantId) {
        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);

        api.getPlantById(plantId).enqueue(new Callback<Plant>() {
            @Override
            public void onResponse(Call<Plant> call, Response<Plant> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bindPlantEntity(response.body());
                } else {
                    Toast.makeText(GardenDetailActivity.this,
                            "Failed to load plant", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Plant> call, Throwable t) {
                Toast.makeText(GardenDetailActivity.this,
                        "Cannot load plant details", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String buildDiseaseListText(List<Disease> diseases) {
        StringBuilder sb = new StringBuilder();
        for (Disease d : diseases) {
            sb.append("🦠 ").append(d.getName()).append("\n");
        }
        return sb.toString();
    }


    private void generateAutoWateringSchedule() {
        progressDialog.setMessage("Checking your saved location...");
        progressDialog.show();

        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);

        // 1. Fetch user info from backend
        api.getProfile().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    if (user.getLat() == null || user.getLon() == null) {
                        // No location set → show dialog
                        showLocationDialog();
                    } else {
                        // Use saved location
                        callAutoGenerateApi(user.getLat(), user.getLon());
                    }
                } else {
                    Toast.makeText(GardenDetailActivity.this,
                            "Không thể lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(GardenDetailActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void showLocationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Chưa đặt vị trí")
                .setMessage("Bạn chưa đặt vị trí nhà. Bạn có muốn đặt bây giờ không?")
                .setPositiveButton("Đặt vị trí nhà", (dialog, which) -> {
                    // Open LocationActivity
                    Intent intent = new Intent(this, LocationActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Sử dụng vị trí hiện tại", (dialog, which) -> {
                    fetchCurrentLocationForWatering();
                })
                .show();
    }

    private void fetchCurrentLocationForWatering() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return;
        }

        progressDialog.setMessage("Fetching GPS location...");
        progressDialog.show();

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            progressDialog.dismiss();
            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                callAutoGenerateApi(lat, lon);
            } else {
                Toast.makeText(this,
                        "Không lấy được vị trí hiện tại. Hãy thử lại.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this,
                    "Lỗi khi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void callAutoGenerateApi(double lat, double lon) {
        progressDialog.setMessage("Generating watering schedule...");
        progressDialog.show();
        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);

        api.generateWeeklyWateringSchedule(gardenId, lat, lon)
                .enqueue(new Callback<List<GardenScheduleResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<GardenScheduleResponse>> call,
                                           @NonNull Response<List<GardenScheduleResponse>> response) {
                        progressDialog.dismiss();
                        if (response.isSuccessful() && response.body() != null) {
                            List<GardenScheduleResponse> schedules = response.body();
                            Toast.makeText(GardenDetailActivity.this,
                                    "✅ Generated " + schedules.size() + " tasks!",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(GardenDetailActivity.this,
                                    "❌ Failed to generate schedule", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<GardenScheduleResponse>> call,
                                          @NonNull Throwable t) {
                        progressDialog.dismiss();
                        Toast.makeText(GardenDetailActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showGenerateConfirmationDialog(long gardenId) {

        String message;
        String title;

        if (currentActiveDiseases == null || currentActiveDiseases.isEmpty()) {
            // No disease
            title = "🌱 Cây khỏe mạnh!";
            message = "Cây của bạn hoàn toàn khỏe mạnh.\nBạn có muốn tạo lịch chăm sóc tự động không?";
        } else {
            // Has disease
            title = "⚠ Cảnh báo";
            message = "Việc tự động điều chỉnh lịch có thể ảnh hưởng "
                    + "tới các hoạt động điều trị khác.\nBạn có chắc muốn tiếp tục?";
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("Tiếp tục", (dialog, which) -> {
                    callAutoGenerateDiseaseApi(gardenId);
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void callAutoGenerateDiseaseApi(Long gardenId) {
        progressDialog.setMessage("Generating watering schedule...");
        progressDialog.show();
        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);

        api.generatewithdisease(gardenId)
                .enqueue(new Callback<List<GardenScheduleResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<GardenScheduleResponse>> call,
                                           @NonNull Response<List<GardenScheduleResponse>> response) {
                        progressDialog.dismiss();
                        if (response.isSuccessful() && response.body() != null) {
                            List<GardenScheduleResponse> schedules = response.body();
                            Toast.makeText(GardenDetailActivity.this,
                                    "✅ Generated " + schedules.size() + " tasks!",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(GardenDetailActivity.this,
                                    "❌ Failed to generate schedule", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<GardenScheduleResponse>> call,
                                          @NonNull Throwable t) {
                        progressDialog.dismiss();
                        Toast.makeText(GardenDetailActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void deleteGarden(long gardenId) {
        progressDialog.setMessage("Đang xoá cây...");
        progressDialog.show();

        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);
        api.removePlant(gardenId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(GardenDetailActivity.this, "Đã xoá thành công!", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
                    finish(); // quay lại màn hình trước
                } else {
                    Toast.makeText(GardenDetailActivity.this, "Xoá thất bại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(GardenDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditGardenDialog() {
        // Inflate layout custom
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_garden, null);

        EditText etNickname = dialogView.findViewById(R.id.etNickname);
        Spinner spStatus = dialogView.findViewById(R.id.spStatus);
        Spinner spType = dialogView.findViewById(R.id.spType);
        Spinner spPotType = dialogView.findViewById(R.id.spPotType);

        // Gán giá trị hiện tại
        String currentNickname = tvNickname.getText().toString().replace("Tên riêng: ", "");
        etNickname.setText(currentNickname);

        // Tạo adapter cho spinner

        String[] statusDisplay = {"Đang phát triển", "Cây đã chết"};
        String[] statusValue = {"ALIVE", "DEAD"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                statusDisplay
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatus.setAdapter(statusAdapter);

        String[] positionDisplay = {"Trong nhà", "Ngoài trời"};
        String[] positionValue = {"Indoor", "Outdoor"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                positionDisplay);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(typeAdapter);

        String[] potDisplay = {"Chậu nhỏ", "Chậu trung bình", "Chậu to"};
        String[] potValue = {"SMALL", "MEDIUM", "LARGE"};
        ArrayAdapter<String> potAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                potDisplay);
        potAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPotType.setAdapter(potAdapter);

        // Dialog hiển thị
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Chỉnh sửa thông tin cây")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newNickname = etNickname.getText().toString().trim();
                    int selectedIndex = spStatus.getSelectedItemPosition();
                    String newStatus = statusValue[selectedIndex];

                    int selectedPosition = spType.getSelectedItemPosition();
                    String newPosition = positionValue[selectedPosition];

                    int selectedPot = spPotType.getSelectedItemPosition();
                    String newPotType = potValue[selectedPot];

                    updateGardenInfo(newNickname, newStatus, newPosition, newPotType);
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void updateGardenInfo(String nickname, String status, String type, String potType) {
        progressDialog.setMessage("Đang cập nhật thông tin...");
        progressDialog.show();

        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);
        GardenUpdateRequest request = new GardenUpdateRequest();
        request.setNickname(nickname);
        request.setStatus(status);
        request.setType(type);
        request.setPotType(potType);

        api.updateGarden(gardenId, request).enqueue(new Callback<GardenResponse>() {
            @Override
            public void onResponse(@NonNull Call<GardenResponse> call, @NonNull Response<GardenResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    GardenResponse updated = response.body();

                    // Cập nhật lại giao diện
                    tvNickname.setText("Tên riêng: " + updated.getNickname());
                    tvStatus.setText("Trạng thái: " + getStatusDisplay(updated.getStatus()));
                    Toast.makeText(GardenDetailActivity.this, "Đã cập nhật thông tin cây!", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
//                    finish();
                } else {
                    Toast.makeText(GardenDetailActivity.this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GardenResponse> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(GardenDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
