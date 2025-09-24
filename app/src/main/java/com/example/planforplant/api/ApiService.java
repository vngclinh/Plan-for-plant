package com.example.planforplant.api;

import com.example.planforplant.DTO.JwtResponse;
import com.example.planforplant.DTO.LoginRequest;
import com.example.planforplant.DTO.RegisterRequest;
import com.example.planforplant.model.Plant;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @GET("plants/search")
    Call<List<Plant>> searchPlants(@Query("keyword") String keyword);

    @POST("api/auth/refresh")
    Call<JwtResponse> refreshToken(@Body Map<String, String> body);

    @POST("/api/auth/login")
    Call<JwtResponse> login(@Body LoginRequest loginRequest);

    @POST("/api/auth/register")
    Call<String> register(@Body RegisterRequest registerRequest);

    @POST("/api/auth/forgot-password")
    Call<String> forgotPassword(@Body Map<String, String> body);

    @POST("/api/auth/verify-reset-code")
    Call<String> verifyResetCode(@Body Map<String, String> body);

    @POST("/api/auth/reset-password")
    Call<String> resetPassword(@Body Map<String, String> body);
}
