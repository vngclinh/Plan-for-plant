package com.example.planforplant.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONObject;

public class SessionManager {
    private static final String PREF_NAME = "APP_PREF";
    private static final String KEY_TOKEN = "JWT_TOKEN";
    private static final String KEY_REFRESH = "REFRESH_TOKEN";
    private static final String KEY_TOKEN_EXP = "JWT_TOKEN_EXP";
    private static final String KEY_REFRESH_EXP = "REFRESH_TOKEN_EXP";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveTokens(String token, String refreshToken) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_REFRESH, refreshToken);

        // Save expiration times
        editor.putLong(KEY_TOKEN_EXP, getTokenExpiration(token));
        editor.putLong(KEY_REFRESH_EXP, getTokenExpiration(refreshToken));

        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null && !isTokenExpired();
    }

    public void clear() {
        editor.clear();
        editor.apply();
    }

    /**
     * Check if the JWT token has expired
     */
    public boolean isTokenExpired() {
        long exp = prefs.getLong(KEY_TOKEN_EXP, 0);
        return System.currentTimeMillis() / 1000 >= exp;
    }

    /**
     * Check if the refresh token has expired
     */
    public boolean isRefreshTokenExpired() {
        long exp = prefs.getLong(KEY_REFRESH_EXP, 0);
        return System.currentTimeMillis() / 1000 >= exp;
    }

    /**
     * Parse JWT token and return expiration (exp) as Unix timestamp
     */
    private long getTokenExpiration(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return 0;

            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE));
            JSONObject json = new JSONObject(payload);

            return json.optLong("exp", 0); // exp in seconds
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
