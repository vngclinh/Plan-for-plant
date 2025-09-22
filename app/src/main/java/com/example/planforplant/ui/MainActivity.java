package com.example.planforplant.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.planforplant.DTO.WeatherResponse;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.MeteosourceApi;
import com.example.planforplant.session.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText searchBox;
    private SessionManager sessionManager;
    private TextView tvLocation, tvWeather;
    private ImageView ivWeatherIcon;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final String METEOSOURCE_KEY = "mtxh21l0d0wslp5xjqfvjgb6ir7ysfnafc9o9ceh";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        String token = sessionManager.getToken();
        String refresh = sessionManager.getRefreshToken();

        Log.d("MainActivity", "Access Token: " + token);
        Log.d("MainActivity", "Refresh Token: " + refresh);

        if (!sessionManager.isLoggedIn()) {
            Log.d("MainActivity", "⚠️ User chưa đăng nhập hoặc token đã hết hạn");
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        } else {
            Log.d("MainActivity", "✅ Token còn hạn, user vẫn đăng nhập");
        }

        setContentView(R.layout.menu);

        // --- Header views ---
        tvLocation = findViewById(R.id.tvLocation);
        tvWeather = findViewById(R.id.tvWeather);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);

        // --- Search box ---
        searchBox = findViewById(R.id.search_box);
        searchBox.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String keyword = searchBox.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    startActivity(new android.content.Intent(this, SearchActivity.class)
                            .putExtra("keyword", keyword));
                }
                return true;
            }
            return false;
        });

        // --- Plant identifier click ---
        LinearLayout plantIdentifier = findViewById(R.id.plant_identifier);
        plantIdentifier.setOnClickListener(v -> startActivity(new android.content.Intent(this, IdentifyActivity.class)));

        // --- Initialize location client ---
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocation();
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_REQUEST_CODE);
            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest.setNumUpdates(1);

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                    double lat = locationResult.getLastLocation().getLatitude();
                    double lon = locationResult.getLastLocation().getLongitude();

                    String cityName = getCityName(lat, lon);
                    tvLocation.setText("📍 " + cityName);

                    fetchWeather(lat, lon);
                } else {
                    Log.e("MainActivity", "Location is null, using default coordinates");
                    useDefaultLocation();
                }
            }
        }, getMainLooper());
    }

    private void useDefaultLocation() {
        double lat = 21.028511;
        double lon = 105.804817;
        tvLocation.setText("📍 Hà Nội");
        fetchWeather(lat, lon);
    }

    private String getCityName(double lat, double lon) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private void fetchWeather(double lat, double lon) {
        MeteosourceApi api = ApiClient.getMeteosourceClient().create(MeteosourceApi.class);

        Call<WeatherResponse> call = api.getWeather(lat, lon, METEOSOURCE_KEY,
                "current", "Asia/Ho_Chi_Minh", "en", "metric");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse.Current current = response.body().current;

                    tvWeather.setText(Math.round(current.temperature) + "°C");
                    ivWeatherIcon.setImageResource(getIconResource(current.icon_num));
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e("MainActivity", "Weather fetch failed", t);
            }
        });
    }

    private int getIconResource(int code) {
        switch (code) {
            case 2: return R.drawable.ic_2;
            case 3: return R.drawable.ic_3;
            case 4: return R.drawable.ic_4;
            case 5: return R.drawable.ic_5;
            case 6: return R.drawable.ic_6;
            case 7: return R.drawable.ic_7;
            case 8: return R.drawable.ic_8;
            case 9: return R.drawable.ic_9;
            case 10: return R.drawable.ic_10;
            case 11: return R.drawable.ic_11;
            case 12: return R.drawable.ic_12;
            case 13: return R.drawable.ic_13;
            case 14: return R.drawable.ic_14;
            case 15: return R.drawable.ic_15;
            case 16: return R.drawable.ic_16;
            case 17: return R.drawable.ic_17;
            case 18: return R.drawable.ic_18;
            case 19: return R.drawable.ic_19;
            case 20: return R.drawable.ic_20;
            case 21: return R.drawable.ic_21;
            case 22: return R.drawable.ic_22;
            case 23: return R.drawable.ic_23;
            case 24: return R.drawable.ic_24;
            case 25: return R.drawable.ic_25;
            case 26: return R.drawable.ic_26;
            case 27: return R.drawable.ic_27;
            case 28: return R.drawable.ic_28;
            case 29: return R.drawable.ic_29;
            case 30: return R.drawable.ic_30;
            case 31: return R.drawable.ic_31;
            case 32: return R.drawable.ic_32;
            case 33: return R.drawable.ic_33;
            case 34: return R.drawable.ic_34;
            case 35: return R.drawable.ic_35;
            case 36: return R.drawable.ic_36;
            default: return R.drawable.ic_1;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation();
            } else {
                Log.e("MainActivity", "Location permission denied, using default");
                useDefaultLocation();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
