package com.example.planforplant.weather;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.planforplant.DTO.WeatherResponse;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.MeteosourceApi;
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

public class WeatherManager {

    private static final int LOCATION_REQUEST_CODE = 100;
    private static final String METEOSOURCE_KEY = "mtxh21l0d0wslp5xjqfvjgb6ir7ysfnafc9o9ceh";

    private final Activity activity;
    private final TextView tvLocation;
    private final TextView tvWeather;
    private final ImageView ivWeatherIcon;

    private FusedLocationProviderClient fusedLocationClient;

    public WeatherManager(Activity activity, TextView tvLocation, TextView tvWeather, ImageView ivWeatherIcon) {
        this.activity = activity;
        this.tvLocation = tvLocation;
        this.tvWeather = tvWeather;
        this.ivWeatherIcon = ivWeatherIcon;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    /** Start fetching location & weather */
    public void start() {
        requestLocation();
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{
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
                    Log.e("WeatherManager", "Location null, using default");
                    useDefaultLocation();
                }
            }
        }, activity.getMainLooper());
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation();
            } else {
                Log.e("WeatherManager", "Location permission denied, using default");
                useDefaultLocation();
            }
        }
    }

    private void useDefaultLocation() {
        double lat = 21.028511;
        double lon = 105.804817;
        tvLocation.setText("📍 Hà Nội");
        fetchWeather(lat, lon);
    }

    private String getCityName(double lat, double lon) {
        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                if (address.getLocality() != null) return address.getLocality();
                if (address.getSubAdminArea() != null) return address.getSubAdminArea();
                if (address.getAdminArea() != null) return address.getAdminArea();
                if (address.getCountryName() != null) return address.getCountryName();
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
                    ivWeatherIcon.setImageResource(WeatherUtils.getIconResource(current.icon_num));
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e("WeatherManager", "Weather fetch failed", t);
            }
        });
    }
}
