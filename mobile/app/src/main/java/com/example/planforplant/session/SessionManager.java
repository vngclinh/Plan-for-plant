package com.example.planforplant.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import androidx.work.WorkManager;

import org.json.JSONObject;

public class SessionManager {
    private static final String PREF_NAME = "APP_PREF";
    private static final String KEY_TOKEN = "JWT_TOKEN";
    private static final String KEY_REFRESH = "REFRESH_TOKEN";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private Context context;

    public SessionManager(Context context) {
        this.context = context.getApplicationContext();
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Lưu token
    public void saveTokens(String token, String refreshToken) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_REFRESH, refreshToken);
        editor.apply();
    }

    // Lấy access token
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    // Lấy refresh token
    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH, null);
    }

    // Check login
    public boolean isLoggedIn() {
        String accessToken = getToken();
        String refreshToken = getRefreshToken();

        if (accessToken != null && !isTokenExpired(accessToken)) {
            // Access token still valid
            return true;
        }

        if (refreshToken != null && !isTokenExpired(refreshToken)) {
            // Access token expired but refresh token valid → can refresh
            return true;
        }

        // Both invalid or missing
        return false;
    }

    // Xóa token (logout)
    public void clear() {
        editor.clear();
        editor.apply();
        WorkManager.getInstance(context).cancelAllWork();
        Log.d("SessionManager", "Clearing all WorkManager tasks on logout");
    }


    public boolean isTokenExpired(String token) {
        try {
            String[] parts = token.split("\\."); // JWT có 3 phần
            if (parts.length < 2) return true;

            // Decode payload
            String payloadJson = new String(Base64.decode(parts[1], Base64.URL_SAFE));
            JSONObject payload = new JSONObject(payloadJson);

            // Lấy claim exp (tính bằng giây)
            long exp = payload.getLong("exp");

            long now = System.currentTimeMillis() / 1000; // giây hiện tại
            Log.d("SessionManager", "Token exp: " + exp + ", now: " + now);

            return exp < now; // hết hạn thì true
        } catch (Exception e) {
            e.printStackTrace();
            return true; // lỗi parse thì coi như hết hạn
        }
    }
}