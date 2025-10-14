package com.example.planforplant.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.planforplant.DTO.UpdateUserRequest;
import com.example.planforplant.DTO.UserResponse;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCity;
    private Button btnUseCurrentLocation;
    private ProgressBar progressBar;
    private TextView tvResult;

    private ApiService apiService;

    private FusedLocationProviderClient fusedLocationClient;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final int LOCATION_PERMISSION_REQUEST = 1000;


    private final String[] cities = new String[]{
            "Hà Nội", "Hồ Chí Minh", "Đà Nẵng", "Hải Phòng",
            "Cần Thơ", "Huế", "Nha Trang", "Hòa Bình", "Hội An",
            "Vũng Tàu", "Biên Hòa", "Buôn Ma Thuột"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        apiService = ApiClient.getLocalClient(this).create(ApiService.class);

        autoCity = findViewById(R.id.autoCity);
        btnUseCurrentLocation = findViewById(R.id.btnUseCurrentLocation);
        progressBar = findViewById(R.id.progressBar);
        tvResult = findViewById(R.id.tvResult);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, cities);
        autoCity.setAdapter(adapter);
        autoCity.setThreshold(1);

        autoCity.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCity = (String) parent.getItemAtPosition(position);
            resolveCityToLatLon(selectedCity);
        });

        btnUseCurrentLocation.setOnClickListener(v -> useCurrentLocation());
    }

    private void resolveCityToLatLon(String cityName) {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        executorService.execute(() -> {
            Geocoder geocoder = new Geocoder(LocationActivity.this, new Locale("vi", "VN"));
            try {
                List<Address> addresses = geocoder.getFromLocationName(cityName, 1);
                runOnUiThread(() -> progressBar.setVisibility(ProgressBar.GONE));
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    double lat = address.getLatitude();
                    double lon = address.getLongitude();
                    runOnUiThread(() -> showConfirmDialog(cityName, lat, lon));
                } else {
                    runOnUiThread(() -> tvResult.setText("Không tìm thấy địa điểm"));
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    tvResult.setText("Lỗi kết nối");
                });
            }
        });
    }

    private void useCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                showConfirmDialog("vị trí hiện tại", lat, lon);
            } else {
                tvResult.setText("Không lấy được vị trí hiện tại");
            }
        });
    }

    private void showConfirmDialog(String locationName, double lat, double lon) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận vị trí")
                .setMessage("Bạn có muốn đặt \"" + locationName + "\" làm vị trí của bạn không?")
                .setPositiveButton("Đồng ý", (dialog, which) -> updateUserLocation(lat, lon))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateUserLocation(double lat, double lon) {
        UpdateUserRequest request = new UpdateUserRequest(lat, lon);

        apiService.updateUserProfile(request).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(LocationActivity.this,
                            "Vị trí đã được cập nhật!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(LocationActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(LocationActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                useCurrentLocation();
            } else {
                Toast.makeText(this, "Quyền truy cập vị trí bị từ chối", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
