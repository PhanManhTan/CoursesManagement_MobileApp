package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class SessionManager {

    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_ROLE = "user_role";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String token, String userId, String role) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_ROLE, role)
            .apply();
    }

    public String getToken() {
        String token = prefs.getString(KEY_TOKEN, null);
        if (hasValue(token) && isTokenExpired(token)) {
            clear();
            return null;
        }
        return token;
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return hasValue(token)
                && hasValue(getUserId())
                && hasValue(getRole());
    }

    public void clear() {
        prefs.edit().clear().apply();
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean isTokenExpired(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return false;
            }

            String payload = padBase64(parts[1]);
            byte[] decoded = Base64.decode(payload, Base64.URL_SAFE | Base64.NO_WRAP);
            JSONObject json = new JSONObject(new String(decoded, StandardCharsets.UTF_8));
            long expiresAtSeconds = json.optLong("exp", 0L);
            if (expiresAtSeconds <= 0L) {
                return false;
            }

            long nowSeconds = System.currentTimeMillis() / 1000L;
            return expiresAtSeconds <= nowSeconds;
        } catch (Exception ignored) {
            return false;
        }
    }

    private String padBase64(String value) {
        int padding = (4 - value.length() % 4) % 4;
        StringBuilder builder = new StringBuilder(value);
        for (int i = 0; i < padding; i++) {
            builder.append('=');
        }
        return builder.toString();
    }
}
