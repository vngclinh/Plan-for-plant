package com.example.planforplant.api;

import com.example.planforplant.model.PlantResponse;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface PlantIdentifyApi {

    // Trỏ tới /api/plant/identify của backend
    @Multipart
    @POST("api/plant/identify")
    Call<PlantResponse> identify(
            @Part MultipartBody.Part image,
            @Part("organ") RequestBody organ
    );
}
