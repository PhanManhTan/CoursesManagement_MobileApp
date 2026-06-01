package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class SessionManager {

    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_ROLE = "user_role";
    private static final String KEY_FULL_NAME = "profile_full_name";
    private static final String KEY_EMAIL = "profile_email";
    private static final String KEY_BIO = "profile_bio";
    private static final String KEY_AVATAR_URL = "profile_avatar_url";

    private static String cachedFullNameStatic = null;
    private static String cachedEmailStatic = null;
    private static String cachedBioStatic = null;
    private static String cachedAvatarUrlStatic = null;
    private static boolean isProfileLoadedStatic = false;

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String token, String userId, String role) {
        saveSession(token, null, userId, role);
    }

    public void saveSession(String token, String refreshToken, String userId, String role) {
        String currentUserId = prefs.getString(KEY_USER_ID, null);
        boolean userChanged = hasValue(currentUserId) && !currentUserId.equals(userId);

        SharedPreferences.Editor editor = prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_ROLE, role);

        if (userChanged) {
            editor.remove(KEY_FULL_NAME)
                    .remove(KEY_EMAIL)
                    .remove(KEY_BIO)
                    .remove(KEY_AVATAR_URL);
            if (!hasValue(refreshToken)) {
                editor.remove(KEY_REFRESH_TOKEN);
            }
            cachedFullNameStatic = null;
            cachedEmailStatic = null;
            cachedBioStatic = null;
            cachedAvatarUrlStatic = null;
            isProfileLoadedStatic = false;
        }

        if (hasValue(refreshToken)) {
            editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        }

        editor.apply();
    }

    public void saveProfile(String fullName, String email, String bio, String avatarUrl) {
        prefs.edit()
            .putString(KEY_FULL_NAME, fullName)
            .putString(KEY_EMAIL, email)
            .putString(KEY_BIO, bio)
            .putString(KEY_AVATAR_URL, avatarUrl)
            .apply();

        cachedFullNameStatic = fullName;
        cachedEmailStatic = email;
        cachedBioStatic = bio;
        cachedAvatarUrlStatic = avatarUrl;
        isProfileLoadedStatic = true;
    }

    public boolean isProfileLoadedMemory() {
        return isProfileLoadedStatic
                || hasValue(getFullName())
                || hasValue(getEmail())
                || hasValue(getBio())
                || hasValue(getAvatarUrl());
    }

    public String getFullName() {
        if (cachedFullNameStatic == null) {
            cachedFullNameStatic = prefs.getString(KEY_FULL_NAME, null);
            if (cachedFullNameStatic != null) {
                isProfileLoadedStatic = true;
            }
        }
        return cachedFullNameStatic;
    }

    public String getEmail() {
        if (cachedEmailStatic == null) {
            cachedEmailStatic = prefs.getString(KEY_EMAIL, null);
        }
        return cachedEmailStatic;
    }

    public String getBio() {
        if (cachedBioStatic == null) {
            cachedBioStatic = prefs.getString(KEY_BIO, null);
        }
        return cachedBioStatic;
    }

    public String getAvatarUrl() {
        if (cachedAvatarUrlStatic == null) {
            cachedAvatarUrlStatic = prefs.getString(KEY_AVATAR_URL, null);
        }
        return cachedAvatarUrlStatic;
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return (hasValue(token) || hasValue(getRefreshToken()))
                && hasValue(getUserId())
                && hasValue(getRole());
    }

    public void clear() {
        prefs.edit().clear().apply();
        cachedFullNameStatic = null;
        cachedEmailStatic = null;
        cachedBioStatic = null;
        cachedAvatarUrlStatic = null;
        isProfileLoadedStatic = false;
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public boolean isAccessTokenExpired(String token) {
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
