package com.example.planforplant.api;

import com.example.planforplant.DTO.GardenScheduleRequest;
import com.example.planforplant.DTO.GardenScheduleResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GardenScheduleApi {

    @POST("/api/schedules")
    Call<GardenScheduleResponse> createSchedule(@Body GardenScheduleRequest request);

    @GET("/api/schedules/garden/{gardenId}")
    Call<List<GardenScheduleResponse>> getSchedulesByGarden(@Path("gardenId") Long gardenId);

    @POST("/api/schedules/garden/{gardenId}/generate")
    Call<List<GardenScheduleResponse>> generateWeeklySchedule(
            @Path("gardenId") Long gardenId,
            @Query("lat") double lat,
            @Query("lon") double lon
    );
}
