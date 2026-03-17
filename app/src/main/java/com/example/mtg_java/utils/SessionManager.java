package com.example.mtg_java.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_LOGGED_IN = "is_logged_in";

    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";

    private static SessionManager instance;

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        // Always use ApplicationContext so this singleton never leaks an Activity
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    /** @deprecated Use getInstance(context) instead. */
    @Deprecated
    public SessionManager(Context context, boolean ignored) {
        this(context);
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
    public boolean isSessionValid() {
        String token = getToken();
        return token != null && !isTokenExpired();
    }
    public boolean isTokenExpired() {
        String token = getToken();

        if (token == null) return true;

        try {
            String[] parts = token.split("\\.");
            String payload = parts[1];

            byte[] decodedBytes = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE);
            String decoded = new String(decodedBytes);

            org.json.JSONObject obj = new org.json.JSONObject(decoded);

            long exp = obj.getLong("exp");
            long currentTime = System.currentTimeMillis() / 1000;

            return currentTime > exp;

        } catch (Exception e) {
            return true;
        }
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
