package com.example.planforplant.api;

import android.content.Context;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiClient {
    private static Retrofit localRetrofit = null;
    private static Retrofit plantNetRetrofit = null;

//    private static final String LOCAL_BASE_URL = "http://192.168.100.90:8080/";
    // nếu dùng điện thoại ảo
     private static final String LOCAL_BASE_URL = "http://10.0.2.2:8080/";
    private static final String PLANTNET_BASE_URL = "https://my-api.plantnet.org/v2/";

    // Local API (with auto-refresh interceptor)
    public static Retrofit getLocalClient(Context context) {
        if (localRetrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context))
                    .build();

            localRetrofit = new Retrofit.Builder()
                    .baseUrl(LOCAL_BASE_URL)
                    .client(client)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return localRetrofit;
    }

    // PlantNet API (no JWT)
    public static Retrofit getPlantNetClient() {
        if (plantNetRetrofit == null) {
            plantNetRetrofit = new Retrofit.Builder()
                    .baseUrl(PLANTNET_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return plantNetRetrofit;
    }
}
