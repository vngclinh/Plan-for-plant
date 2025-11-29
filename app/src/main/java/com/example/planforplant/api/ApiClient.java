package com.example.planforplant.api;

import android.content.Context;

import java.util.Collections;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiClient {
    private static Retrofit localRetrofit = null;
    private static Retrofit meteosourceRetrofit = null;

    //private static final String LOCAL_BASE_URL = "http://192.168.100.90:8080/";

    private static final String LOCAL_BASE_URL = "http://10.0.2.2:8080/";
    private static final String PLANTNET_BASE_URL = "https://my-api.plantnet.org/v2/";
    private static final String METEOSOURCE_BASE_URL = "https://www.meteosource.com/";

    // Local API (with auto-refresh interceptor)
    public static Retrofit getLocalClient(Context context) {
        if (localRetrofit == null) {
            // Logging interceptor for debugging
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                    .addInterceptor(new AuthInterceptor(context)) //JWT/refresh interceptor
                    .addInterceptor(logging) // log request + response
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(90, java.util.concurrent.TimeUnit.SECONDS)
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
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(
                message -> android.util.Log.d("PlantNetAPI", message)
        );
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))  // 👈 ép dùng HTTP/1.1
                .retryOnConnectionFailure(true)
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build();

        return new Retrofit.Builder()
                .baseUrl(PLANTNET_BASE_URL)
                .client(client)  // 👈 phải gắn client này vào
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // Meteosource API
    public static Retrofit getMeteosourceClient() {
        if (meteosourceRetrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build();

            meteosourceRetrofit = new Retrofit.Builder()
                    .baseUrl(METEOSOURCE_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return meteosourceRetrofit;
    }
}