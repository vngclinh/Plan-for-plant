package com.example.planforplant.api;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.planforplant.DTO.JwtResponse;
import com.example.planforplant.session.SessionManager;
import com.example.planforplant.ui.LoginActivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AuthInterceptor implements Interceptor {
    private static final String TAG = "AuthInterceptor";

    private final SessionManager sessionManager;
    private final ApiService authApi;

    private final Context appContext;

    public AuthInterceptor(Context context) {
        this.appContext = context.getApplicationContext();
        this.sessionManager = new SessionManager(context);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.100.90:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authApi = retrofit.create(ApiService.class);
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String jwt = sessionManager.getToken();

        Log.i(TAG, "================ NEW REQUEST ================");
        Log.i(TAG, "URL: " + originalRequest.url());
        Log.i(TAG, "Method: " + originalRequest.method());
        Log.i(TAG, "Original Headers: " + originalRequest.headers());
        Log.i(TAG, "Token from session: " + jwt);

        Request requestToSend = originalRequest;
        if (jwt != null) {
            requestToSend = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer " + jwt)
                    .build();
            Log.i(TAG, "Added Authorization header with JWT");
        }

        Response response;
        try {
            response = chain.proceed(requestToSend);
        } catch (Exception e) {
            Log.e(TAG, "Request failed: " + e.getMessage(), e);
            throw e;
        }

        Log.i(TAG, "Response Code: " + response.code());
        Log.i(TAG, "Response Headers: " + response.headers());

        // Retry on 401 or 403
        if (response.code() == 401 || response.code() == 403) {
            Log.w(TAG, "Unauthorized or Forbidden detected, attempting refresh...");
            response.close();

            String refreshToken = sessionManager.getRefreshToken();
            Log.i(TAG, "Refresh token from session: " + refreshToken);

            if (refreshToken != null) {
                try {
                    Map<String, String> body = new HashMap<>();
                    body.put("refreshToken", refreshToken);

                    retrofit2.Response<JwtResponse> refreshResponse = authApi.refreshToken(body).execute();
                    if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                        JwtResponse newTokens = refreshResponse.body();
                        sessionManager.saveTokens(newTokens.getToken(), newTokens.getRefreshToken());

                        Log.i(TAG, "Refresh successful. New token: " + newTokens.getToken());

                        Request newRequest = originalRequest.newBuilder()
                                .removeHeader("Authorization")
                                .addHeader("Authorization", "Bearer " + newTokens.getToken())
                                .build();

                        Log.i(TAG, "Retrying original request with new token...");
                        Response retryResponse = chain.proceed(newRequest);
                        Log.i(TAG, "Retry Response Code: " + retryResponse.code());
                        Log.i(TAG, "Retry Response Headers: " + retryResponse.headers());
                        return retryResponse;
                    } else {
                        Log.w(TAG, "Refresh token invalid or expired. Clearing session...");
                        handleSessionExpired();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception during refresh token call: " + e.getMessage(), e);
                    handleSessionExpired();
                }
            } else {
                Log.w(TAG, "No refresh token available. Clearing session...");
                handleSessionExpired();
            }
        }

        return response;
    }

    private void handleSessionExpired() {
        sessionManager.clear();
        Log.i(TAG, "Session cleared due to expired/invalid token");

        // Vì interceptor không chạy trong main thread → cần post ra main looper
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(appContext, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(appContext, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            appContext.startActivity(intent);
        });
    }
}