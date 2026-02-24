package com.example.mtg_java.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_LOGGED_IN = "is_logged_in";

    // ✅ ADDED (do not break old code)
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // 🔹 EXISTING (unchanged)
    public void setLoggedIn(boolean loggedIn) {
        editor.putBoolean(KEY_LOGGED_IN, loggedIn);
        editor.apply();
    }
    public void saveUserId(String userId) {
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    // 🔹 EXISTING (unchanged)
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    // ✅ ADDED
    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    // ✅ ADDED
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }
    public void saveUser(String username, String email) {
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }
    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    // 🔹 EXISTING (unchanged)
    public void logout() {
        editor.clear();
        editor.apply();
    }
}
