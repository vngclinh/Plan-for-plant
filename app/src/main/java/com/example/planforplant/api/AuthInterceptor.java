package com.example.planforplant.api;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.planforplant.DTO.JwtResponse;
import com.example.planforplant.session.SessionManager;

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

    public AuthInterceptor(Context context) {
        this.sessionManager = new SessionManager(context);

        // ✅ Dùng localhost của Android Emulator
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/") // ⚙️ KHÔNG DÙNG 192.168.x.x
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
        Log.i(TAG, "Token from session: " + jwt);

        Request requestToSend = originalRequest;

        // ✅ Nếu có token thì thêm header Authorization
        if (jwt != null && !jwt.isEmpty()) {
            requestToSend = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer " + jwt)
                    .build();
            Log.i(TAG, "✅ Added Authorization header with JWT");
        } else {
            Log.w(TAG, "⚠️ Token from session is null or empty");
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

        // ✅ Nếu BE trả về 401 hoặc 403 → thử refresh token
        if ((response.code() == 401 || response.code() == 403) && sessionManager.getRefreshToken() != null) {
            Log.w(TAG, "Unauthorized detected, attempting token refresh...");
            response.close(); // đóng response cũ để tránh leak

            String refreshToken = sessionManager.getRefreshToken();
            Log.i(TAG, "Refresh token from session: " + refreshToken);

            try {
                Map<String, String> body = new HashMap<>();
                body.put("refreshToken", refreshToken);

                retrofit2.Response<JwtResponse> refreshResponse = authApi.refreshToken(body).execute();

                if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                    JwtResponse newTokens = refreshResponse.body();

                    sessionManager.saveTokens(newTokens.getToken(), newTokens.getRefreshToken());
                    Log.i(TAG, "✅ Token refresh successful, retrying original request...");

                    // Gửi lại request với token mới
                    Request newRequest = originalRequest.newBuilder()
                            .removeHeader("Authorization")
                            .addHeader("Authorization", "Bearer " + newTokens.getToken())
                            .build();

                    return chain.proceed(newRequest);
                } else {
                    Log.w(TAG, "❌ Refresh token invalid or expired. Clearing session...");
                    handleSessionExpired();
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Exception during refresh token call: " + e.getMessage(), e);
                handleSessionExpired();
            }
        }

        return response;
    }

    private void handleSessionExpired() {
        sessionManager.clear();
        Log.i(TAG, "🧹 Session cleared due to expired/invalid token");
    }
}
