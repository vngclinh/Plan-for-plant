package com.example.planforplant.api;

import com.example.planforplant.DTO.AddDiaryRequest;
import com.example.planforplant.DTO.AddGardenRequest;
import com.example.planforplant.DTO.ChangepasswordRequest;
import com.example.planforplant.DTO.DiaryResponse;
import com.example.planforplant.DTO.GardenImageResponse;
import com.example.planforplant.DTO.GardenResponse;
import com.example.planforplant.DTO.GardenScheduleRequest;
import com.example.planforplant.DTO.GardenScheduleResponse;
import com.example.planforplant.DTO.GardenUpdateRequest;
import com.example.planforplant.DTO.JwtResponse;
import com.example.planforplant.DTO.LoginRequest;
import com.example.planforplant.DTO.RegisterRequest;
import com.example.planforplant.DTO.UpdateUserRequest;

import com.example.planforplant.DTO.UserProgressResponse;
import com.example.planforplant.DTO.UserResponse;
import com.example.planforplant.model.Disease;
import com.example.planforplant.model.Plant;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("plants/search")
    Call<List<Plant>> searchPlants(@Query("keyword") String keyword);

    @POST("/api/auth/forgot-password")
    Call<String> forgotPassword(@Body Map<String, String> body);

    @POST("/api/auth/verify-reset-code")
    Call<String> verifyResetCode(@Body Map<String, String> body);

    @POST("/api/auth/reset-password")
    Call<String> resetPassword(@Body Map<String, String> body);

    @POST("api/auth/refresh")
    Call<JwtResponse> refreshToken(@Body Map<String, String> body);

    @POST("/api/auth/login")
    Call<JwtResponse> login(@Body LoginRequest loginRequest);

    @POST("/api/auth/register")
    Call<String> register(@Body RegisterRequest registerRequest);

    @POST("/garden/add")
    Call<GardenResponse> addPlantToGarden(
            @Body AddGardenRequest request
    );

    @DELETE("/garden/{gardenId}")
    Call<Void> removePlant(@Path("gardenId") long gardenId);

    @GET("api/diseases")
    Call<List<Disease>> getAllDiseases();

    @GET("api/diseases/{id}")
    Call<Disease> getDiseaseById(@Path("id") Long id);

    @GET("api/diseases/search")
    Call<List<Disease>> searchDiseases(@Query("keyword") String keyword);

    @GET("/garden/my")
    Call<List<GardenResponse>> getMyGarden();

    @PUT("/garden/{gardenId}/")
    Call<GardenResponse> updateGarden(@Path("gardenId") long gardenId, @Body GardenUpdateRequest request);

    @GET("garden/{id}/images")
    Call<List<GardenImageResponse>> getGardenImages(@Path("id") Long gardenId);

    @Multipart
    @POST("garden/{id}/images")
    Call<GardenImageResponse> uploadGardenImage(
            @Path("id") Long gardenId,
            @Part MultipartBody.Part file
    );
    @DELETE("garden/images/{id}")
    Call<Void> deleteGardenImage(@Path("id") Long imageId);
    @POST("api/schedules")
    Call<GardenScheduleResponse> createSchedule(@Body GardenScheduleRequest request);

    @GET("/api/schedules/exists")
    Call<Boolean> checkScheduleExists(@Query("gardenId") Long gardenId, @Query("scheduledTime") String scheduledTime);

    @GET("/api/user/me")
    Call<UserResponse> getProfile();

    @Multipart
    @POST("api/user/avatar")
    Call<Map<String, String>> uploadAvatar(@Part MultipartBody.Part file);

    @POST("/api/user/change-password")
    Call<String> changePassword(@Body ChangepasswordRequest request);

    @GET("api/schedules/by-date")
    Call<List<GardenScheduleResponse>> getSchedulesByDate(@Query("date") String date);

    @POST("/api/schedules/garden/{gardenId}/generate")
    Call<List<GardenScheduleResponse>> generateWeeklyWateringSchedule(
            @Path("gardenId") Long gardenId,
            @Query("lat") double lat,
            @Query("lon") double lon
    );

    @PUT("/api/user/me")
    Call<UserResponse> updateUserProfile(@Body UpdateUserRequest request);

    @PUT("/api/schedules/{id}")
    Call<GardenScheduleResponse> updateSchedule(@Path("id") Long id, @Body GardenScheduleRequest request);

    @GET("/api/schedules")
    Call<List<GardenScheduleResponse>> getAllSchedules();

    /** Lấy kế hoạch theo ID */
    @GET("/api/schedules/{id}")
    Call<GardenScheduleResponse> getScheduleById(@Path("id") Long id);

    /** Xóa kế hoạch */
    @DELETE("/api/schedules/{id}")
    Call<Void> deleteSchedule(@Path("id") Long id);

    /** Lấy kế hoạch theo ID vườn */
    @GET("/api/schedules/garden/{gardenId}")
    Call<List<GardenScheduleResponse>> getSchedulesByGarden(@Path("gardenId") Long gardenId);

    /** Lấy kế hoạch theo trạng thái (Done / NotDone) */
    @GET("/api/schedules/completion/{status}")
    Call<List<GardenScheduleResponse>> getSchedulesByCompletion(@Path("status") String status);

    /** Lấy kế hoạch theo vườn + ngày */
    @GET("/api/schedules/garden/{gardenId}/date")
    Call<List<GardenScheduleResponse>> getSchedulesByGardenAndDate(
            @Path("gardenId") Long gardenId,
            @Query("date") String date
    );
    @POST("garden/{gardenId}/diaries")
    Call<GardenResponse> addDiaryEntry(
            @Path("gardenId") Long gardenId,
            @Body AddDiaryRequest request
    );
    @DELETE("garden/{gardenId}/diaries/{diaryId}")
    Call<Void> removeDiaryEntry(
            @Path("gardenId") Long gardenId,
            @Path("diaryId") Long diaryId
    );
    @GET("garden/{gardenId}/diaries")
    Call<List<DiaryResponse>> getDiariesByGardenId(
            @Path("gardenId") Long gardenId
    );

    @POST("/api/user/water-tree")
    Call<UserProgressResponse> waterTreeStreak();

    @GET("/api/user/progress")
    Call<UserProgressResponse> getProgress();
}
