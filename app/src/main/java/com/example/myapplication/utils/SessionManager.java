package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs  = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveSession(String accessToken, String refreshToken,
                            String userId, String email, String role, String fullName) {
        editor.putString(Constants.KEY_ACCESS_TOKEN,   accessToken)
              .putString(Constants.KEY_REFRESH_TOKEN,  refreshToken)
              .putString(Constants.KEY_USER_ID,        userId)
              .putString(Constants.KEY_USER_EMAIL,     email)
              .putString(Constants.KEY_USER_ROLE,      role)
              .putString(Constants.KEY_USER_FULL_NAME, fullName)
              .apply();
    }

    public void clearSession() {
        editor.remove(Constants.KEY_ACCESS_TOKEN)
              .remove(Constants.KEY_REFRESH_TOKEN)
              .remove(Constants.KEY_USER_ID)
              .remove(Constants.KEY_USER_EMAIL)
              .remove(Constants.KEY_USER_ROLE)
              .remove(Constants.KEY_USER_FULL_NAME)
              .apply();
    }

    public boolean isLoggedIn() {
        String token = prefs.getString(Constants.KEY_ACCESS_TOKEN, null);
        return token != null && !token.isEmpty();
    }

    public String getAccessToken()  { return prefs.getString(Constants.KEY_ACCESS_TOKEN,   null); }
    public String getUserId()       { return prefs.getString(Constants.KEY_USER_ID,        null); }
    public String getUserEmail()    { return prefs.getString(Constants.KEY_USER_EMAIL,     null); }
    public String getUserRole()     { return prefs.getString(Constants.KEY_USER_ROLE,      null); }
    public String getUserFullName() { return prefs.getString(Constants.KEY_USER_FULL_NAME, null); }

    public void setOnboardingDone() {
        editor.putBoolean(Constants.KEY_ONBOARDING_DONE, true).apply();
    }

    public boolean isOnboardingDone() {
        return prefs.getBoolean(Constants.KEY_ONBOARDING_DONE, false);
    }
}
