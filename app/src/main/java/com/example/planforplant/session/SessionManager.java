package com.example.planforplant.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

public class SessionManager {
    private static final String PREF_NAME = "APP_PREF";
    private static final String KEY_TOKEN = "JWT_TOKEN";
    private static final String KEY_REFRESH = "REFRESH_TOKEN";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // ✅ Lưu token
    public void saveTokens(String token, String refreshToken) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_REFRESH, refreshToken);
        editor.apply();
        Log.d("SessionManager", "✅ Saved access token: " + token);
        Log.d("SessionManager", "✅ Saved refresh token: " + refreshToken);
    }

    // ✅ Lấy access token
    public String getToken() {
        String token = prefs.getString(KEY_TOKEN, null);
        Log.d("SessionManager", "🔑 getToken() -> " + token);
        return token;
    }

    // ✅ Lấy refresh token
    public String getRefreshToken() {
        String refresh = prefs.getString(KEY_REFRESH, null);
        Log.d("SessionManager", "🔁 getRefreshToken() -> " + refresh);
        return refresh;
    }

    // ✅ Xóa session
    public void clear() {
        editor.clear();
        editor.apply();
        Log.d("SessionManager", "🧹 Session cleared");
    }

    // 🕒 Kiểm tra token hết hạn (không tự xóa)
    public boolean isTokenExpired(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return true;

            String payloadJson = new String(Base64.decode(parts[1], Base64.URL_SAFE));
            JSONObject payload = new JSONObject(payloadJson);

            long exp = payload.getLong("exp");
            long now = System.currentTimeMillis() / 1000;

            Log.d("SessionManager", "🕒 Token exp: " + exp + ", now: " + now);
            return exp < now;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }
}
