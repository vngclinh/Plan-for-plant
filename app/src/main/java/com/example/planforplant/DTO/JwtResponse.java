package com.example.planforplant.DTO;

import com.google.gson.annotations.SerializedName;

public class JwtResponse {
    @SerializedName("accessToken")
    private String token;

    @SerializedName("refreshToken")
    private String refreshToken;

    private String username;

    public String getToken() { return token; }
    public String getRefreshToken() { return refreshToken; }
    public String getUsername() { return username; }
}
