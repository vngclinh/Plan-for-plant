package com.example.planforplant.api;

import com.example.planforplant.model.Plant;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("plants/search")
    Call<List<Plant>> searchPlants(@Query("keyword") String keyword);
}
