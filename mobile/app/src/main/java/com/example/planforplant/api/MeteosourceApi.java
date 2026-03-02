package com.example.planforplant.api;

import com.example.planforplant.DTO.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MeteosourceApi {
        @GET("api/v1/free/point")
        Call<WeatherResponse> getWeather(
                @Query("lat") double latitude,
                @Query("lon") double longitude,
                @Query("key") String apiKey,
                @Query("sections") String sections,
                @Query("timezone") String timezone,
                @Query("language") String language,
                @Query("units") String units
        );

}

