package com.example.planforplant.api;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ChatApi {

    // 1. Gửi Text (Dùng JSON - Khớp với @RequestBody Map<String, String> ở Backend)
    @POST("api/chat")
    Call<String> sendTextMessage(@Body MessageRequest body);

    // 2. Gửi Ảnh (Dùng Multipart - Khớp với @RequestPart ở Backend)
    @Multipart
    @POST("api/chat")
    Call<String> sendImageMessage(
            @Part("message") RequestBody message, // Khớp với @RequestPart("message")
            @Part MultipartBody.Part image        // Khớp với @RequestPart("image")
    );

    // Class DTO để đóng gói dữ liệu JSON
    class MessageRequest {
        private String message;

        public MessageRequest(String message) {
            this.message = message;
        }
        // Getter/Setter (nếu cần thiết, Gson/Moshi thường tự hiểu)
    }
    @GET("api/chat/today")
    Call<List<ChatHistoryResponse>> getTodayChats();

    public class ChatHistoryResponse {
        public Long id;
        public String role;
        public String message;
        public String response;
        public String createdAt;
    }

}