package com.example.planforplant.session;

public class SessionManager {
    private static final String PREF_NAME = "APP_PREF";
    private static final String KEY_TOKEN = "JWT_TOKEN";
    private static final String KEY_REFRESH = "REFRESH_TOKEN";

    private android.content.SharedPreferences prefs;
    private android.content.SharedPreferences.Editor editor;

    public SessionManager(android.content.Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, android.content.Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveTokens(String token, String refreshToken) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_REFRESH, refreshToken);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void clear() {
        editor.clear();
        editor.apply();
    }
}
