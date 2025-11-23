package com.example.planforplant.api;

import com.example.planforplant.DTO.HealthResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface HealthApi {

    @Multipart
    @POST("api/v3/identification")
    Call<HealthResponse> identifyHealth(
            @Part MultipartBody.Part images,
            @Part("similar_images") RequestBody similarImages,
            @Part("health") RequestBody health,
            @Header("Api-Key") String apiKey
    );
}
