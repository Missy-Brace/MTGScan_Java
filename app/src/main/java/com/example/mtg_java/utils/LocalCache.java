package com.example.mtg_java.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mtg_java.model.Card;
import com.example.mtg_java.model.Group;
import com.example.mtg_java.model.News;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

// Single access point for all local cache reads and writes.
// Each "table" is a JSON string stored in its own SharedPreferences key.
// Writes happen only after a successful network response.
public class LocalCache {

    private static final String PREF_NAME       = "local_cache";
    private static final String KEY_NEWS        = "news";
    private static final String KEY_GROUPS      = "groups";
    private static final String KEY_PROFILE_IMG = "profile_image_url";

    // Cards per collection are keyed by group ID so each collection
    // has its own independent cache entry.
    private static final String KEY_CARDS_PREFIX = "cards_";

    private static LocalCache instance;
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    private LocalCache(Context ctx) {
        prefs = ctx.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized LocalCache getInstance(Context ctx) {
        if (instance == null) instance = new LocalCache(ctx);
        return instance;
    }

    // ── News ────────────────────────────────────────────────────

    public void saveNews(List<News> items) {
        prefs.edit().putString(KEY_NEWS, gson.toJson(items)).apply();
    }

    // Returns null if nothing is cached yet (new account / cleared data).
    public List<News> getNews() {
        String json = prefs.getString(KEY_NEWS, null);
        if (json == null) return null;
        Type t = new TypeToken<List<News>>(){}.getType();
        return gson.fromJson(json, t);
    }

    // ── Collections (groups) ────────────────────────────────────

    public void saveGroups(List<Group> groups) {
        prefs.edit().putString(KEY_GROUPS, gson.toJson(groups)).apply();
    }

    public List<Group> getGroups() {
        String json = prefs.getString(KEY_GROUPS, null);
        if (json == null) return null;
        Type t = new TypeToken<List<Group>>(){}.getType();
        return gson.fromJson(json, t);
    }

    // ── Cards per collection ────────────────────────────────────

    public void saveCards(String groupId, List<Card> cards) {
        prefs.edit().putString(KEY_CARDS_PREFIX + groupId, gson.toJson(cards)).apply();
    }

    public List<Card> getCards(String groupId) {
        String json = prefs.getString(KEY_CARDS_PREFIX + groupId, null);
        if (json == null) return null;
        Type t = new TypeToken<List<Card>>(){}.getType();
        return gson.fromJson(json, t);
    }

    // ── Profile ─────────────────────────────────────────────────

    public void saveProfileImageUrl(String url) {
        prefs.edit().putString(KEY_PROFILE_IMG, url).apply();
    }

    public String getProfileImageUrl() {
        return prefs.getString(KEY_PROFILE_IMG, null);
    }

    // ── Lifecycle ───────────────────────────────────────────────

    // Call on logout so stale data from one account never leaks to another.
    public void clear() {
        prefs.edit().clear().apply();
    }
}