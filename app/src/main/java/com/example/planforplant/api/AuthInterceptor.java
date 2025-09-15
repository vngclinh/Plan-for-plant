package com.example.planforplant.api;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.example.planforplant.DTO.JwtResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AuthInterceptor implements Interceptor {
    private final SharedPreferences prefs;
    private final ApiService authApi;

    public AuthInterceptor(Context context) {
        this.prefs = context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE);

        // Separate Retrofit for refresh calls (no interceptor to avoid recursion)
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/") // backend base URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authApi = retrofit.create(ApiService.class);
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        String jwt = prefs.getString("JWT_TOKEN", null);
        Request request = chain.request();

        // attach JWT if available
        if (jwt != null) {
            request = request.newBuilder()
                    .addHeader("Authorization", "Bearer " + jwt)
                    .build();
        }

        Response response = chain.proceed(request);

        // if unauthorized -> try refresh
        if (response.code() == 401) {
            response.close();

            String refreshToken = prefs.getString("REFRESH_TOKEN", null);
            if (refreshToken != null) {
                try {
                    Map<String, String> body = new HashMap<>();
                    body.put("refreshToken", refreshToken);

                    retrofit2.Response<JwtResponse> refreshResponse =
                            authApi.refreshToken(body).execute();

                    if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                        JwtResponse jwtResponse = refreshResponse.body();

                        // save new JWT
                        prefs.edit()
                                .putString("JWT_TOKEN", jwtResponse.getToken())
                                .apply();

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