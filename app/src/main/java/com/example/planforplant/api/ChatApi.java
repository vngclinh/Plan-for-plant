package com.example.planforplant.api;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ChatApi {

    @Multipart
    @POST("api/chat")
    Call<String> sendMessage(
            @Part("message") RequestBody message
    );

    @Multipart
    @POST("api/chat")
    Call<String> sendImageMessage(
            @Part("message") RequestBody message,
            @Part MultipartBody.Part image
    );
    class MessageRequest {
        private String message;

        public MessageRequest(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
