// PlantNetApi.java
package com.example.planforplant.api;

import com.example.planforplant.model.PlantResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface PlantNetApi {
    @Multipart
    @POST("identify/all") // endpoint thực tế Pl@ntNet
    Call<PlantResponse> identify(
            @Part MultipartBody.Part images,
            @Part("organs") RequestBody organs,
            @Query("api-key") String apiKey
    );
}
