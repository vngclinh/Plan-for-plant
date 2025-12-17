package com.example.planforplant.Notification;

import android.support.annotation.NonNull;

import com.example.planforplant.NotificationHelper;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        String title = message.getNotification().getTitle();
        String body = message.getNotification().getBody();

        new NotificationHelper(this)
                .dispatchNotification(title, body);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        registerTokenToBackend(token);
    }

    private void registerTokenToBackend(String token) {
        long userId = getSharedPreferences("user", MODE_PRIVATE)
                .getLong("userId", -1);

        Map<String, String> body = new HashMap<>();
        body.put("userId", String.valueOf(userId));
        body.put("token", token);
        body.put("platform", "ANDROID");

        ApiService api = ApiClient.getLocalClient(this)
                .create(ApiService.class);
        api.registerToken(body).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {}
            @Override public void onFailure(Call<Void> c, Throwable t) {}
        });
    }

}
