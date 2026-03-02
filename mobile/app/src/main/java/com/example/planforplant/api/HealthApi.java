package com.example.planforplant.api;

import com.example.planforplant.DTO.HealthResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface HealthApi {

    @POST("health_assessment")
    Call<HealthResponse> assessHealth(
            @Query("details") String details,
            @Query("language") String language,
            @Query("full_disease_list") boolean fullDiseaseList,
            @Header("Api-Key") String apiKey,
            @Body Map<String, Object> body
    );
}
