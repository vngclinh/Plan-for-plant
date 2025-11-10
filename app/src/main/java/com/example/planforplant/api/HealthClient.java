package com.example.planforplant.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HealthClient {
    private static final String BASE_URL = "https://plant.id/";
    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
