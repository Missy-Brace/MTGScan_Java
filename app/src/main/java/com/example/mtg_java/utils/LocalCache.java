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

public class LocalCache {

    private static final String PREF_NAME         = "local_cache";
    private static final String KEY_NEWS           = "news";
    private static final String KEY_GROUPS         = "groups";
    private static final String KEY_PROFILE_IMG    = "profile_image_url";
    private static final String KEY_STAT_COLLECTIONS = "stat_collections";
    private static final String KEY_STAT_CARDS       = "stat_cards";
    private static final String KEY_CARDS_PREFIX   = "cards_";

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

    // ── News ─────────────────────────────────────────────────────

    public void saveNews(List<News> items) {
        prefs.edit().putString(KEY_NEWS, gson.toJson(items)).apply();
    }

    public List<News> getNews() {
        String json = prefs.getString(KEY_NEWS, null);
        if (json == null) return null;
        Type t = new TypeToken<List<News>>(){}.getType();
        return gson.fromJson(json, t);
    }

    // ── Collections (groups) ─────────────────────────────────────

    public void saveGroups(List<Group> groups) {
        prefs.edit().putString(KEY_GROUPS, gson.toJson(groups)).apply();
    }

    public List<Group> getGroups() {
        String json = prefs.getString(KEY_GROUPS, null);
        if (json == null) return null;
        Type t = new TypeToken<List<Group>>(){}.getType();
        return gson.fromJson(json, t);
    }

    // ── Cards per collection ──────────────────────────────────────

    public void saveCards(String groupId, List<Card> cards) {
        prefs.edit().putString(KEY_CARDS_PREFIX + groupId, gson.toJson(cards)).apply();
    }

    public List<Card> getCards(String groupId) {
        String json = prefs.getString(KEY_CARDS_PREFIX + groupId, null);
        if (json == null) return null;
        Type t = new TypeToken<List<Card>>(){}.getType();
        return gson.fromJson(json, t);
    }

    // ── Profile ───────────────────────────────────────────────────

    public void saveProfileImageUrl(String url) {
        prefs.edit().putString(KEY_PROFILE_IMG, url).apply();
    }

    public String getProfileImageUrl() {
        return prefs.getString(KEY_PROFILE_IMG, null);
    }

    // ── Home stats ────────────────────────────────────────────────
    // Stored as plain ints — no JSON needed for two numbers.
    // -1 means "never cached" so callers can distinguish a real zero
    // from an absent value.

    public void saveStats(int collectionCount, int totalCards) {
        prefs.edit()
                .putInt(KEY_STAT_COLLECTIONS, collectionCount)
                .putInt(KEY_STAT_CARDS, totalCards)
                .apply();
    }

    /** Returns {collectionCount, totalCards}, or null if never cached. */
    public int[] getStats() {
        int collections = prefs.getInt(KEY_STAT_COLLECTIONS, -1);
        int cards       = prefs.getInt(KEY_STAT_CARDS,       -1);
        if (collections == -1 && cards == -1) return null;
        return new int[]{ Math.max(0, collections), Math.max(0, cards) };
    }

    // ── Lifecycle ─────────────────────────────────────────────────

    public void clear() {
        prefs.edit().clear().apply();
    }
}
