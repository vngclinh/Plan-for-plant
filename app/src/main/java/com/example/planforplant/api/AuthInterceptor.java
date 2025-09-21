package com.example.planforplant.api;

import android.content.Context;

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
    private final SessionManager sessionManager;
    private final ApiService authApi;

    public AuthInterceptor(Context context) {
        this.sessionManager = new SessionManager(context);

        // Retrofit for refresh calls (no interceptor → avoids infinite loop)
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/") // backend base URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authApi = retrofit.create(ApiService.class);
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        String jwt = sessionManager.getToken();
        Request request = chain.request();

        // attach JWT if available
        if (jwt != null) {
            request = request.newBuilder()
                    .addHeader("Authorization", "Bearer " + jwt)
                    .build();
        }

        Response response = chain.proceed(request);

        // if unauthorized → try refresh
        if (response.code() == 401) {
            response.close();

            String refreshToken = sessionManager.getRefreshToken();
            if (refreshToken != null) {
                try {
                    Map<String, String> body = new HashMap<>();
                    body.put("refreshToken", refreshToken);

                    retrofit2.Response<JwtResponse> refreshResponse =
                            authApi.refreshToken(body).execute();

                    if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                        JwtResponse jwtResponse = refreshResponse.body();

                        // Save new tokens
                        sessionManager.saveTokens(
                                jwtResponse.getToken(),
                                jwtResponse.getRefreshToken()
                        );

                        // retry original request with new JWT
                        Request newRequest = request.newBuilder()
                                .removeHeader("Authorization")
                                .addHeader("Authorization", "Bearer " + jwtResponse.getToken())
                                .build();

                        return chain.proceed(newRequest);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return response;
    }
}