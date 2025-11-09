package com.example.planforplant.api;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ChatApi {

    @POST("api/chat")
    Call<String> sendMessage(@Body MessageRequest body);

    class MessageRequest {
        private String message;
        public MessageRequest(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}