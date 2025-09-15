// ApiClient.java
package com.example.planforplant.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit localRetrofit = null;
    private static Retrofit plantNetRetrofit = null;

    private static final String LOCAL_BASE_URL = "http://10.0.2.2:8080/";
    private static final String PLANTNET_BASE_URL = "https://my-api.plantnet.org/v2/";

    // Local API (your backend at 10.0.2.2)
    public static Retrofit getLocalClient() {
        if (localRetrofit == null) {
            localRetrofit = new Retrofit.Builder()
                    .baseUrl(LOCAL_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return localRetrofit;
    }

    // PlantNet API
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
