package com.example.mtg_java.utils;

import android.content.Context;
import android.content.SharedPreferences;

// FIX: Removed stored `editor` field. SharedPreferences.Editor is lightweight and
// should be obtained fresh per-write to avoid concurrent-write races where a late
// apply() silently clobbers a newer value written by another thread.
public class SessionManager {

    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_LOGGED_IN = "is_logged_in";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";

    private static SessionManager instance;

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        // FIX: No longer storing a shared editor field. Each write creates its own
        // editor inline, ensuring isolated, race-free commits.
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

    public void setLoggedIn(boolean loggedIn) {
        // FIX: fresh editor per call
        prefs.edit().putBoolean(KEY_LOGGED_IN, loggedIn).apply();
    }

    public void saveUserId(String userId) {
        prefs.edit().putString(KEY_USER_ID, userId).apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
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

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void saveUser(String username, String email) {
        prefs.edit()
                .putString(KEY_USERNAME, username)
                .putString(KEY_EMAIL, email)
                .apply();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public void logout() {
        prefs.edit().clear().apply();
    }
}
