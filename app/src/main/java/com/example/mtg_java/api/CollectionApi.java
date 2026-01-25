package com.example.mtg_java.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.mtg_java.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CollectionApi {

    private static final String BASE_URL = "https://mtgscan1.onrender.com/api/collections";
    private final OkHttpClient client;
    private final Handler mainHandler;
    private final String token;

    public CollectionApi(Context context) {
        client = new OkHttpClient();
        mainHandler = new Handler(Looper.getMainLooper());

        // get JWT from SessionManager
        SessionManager session = new SessionManager(context);
        this.token = session.getToken();
    }

    /* ================= CALLBACK ================= */

    public interface ApiCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    /* ================= HELPERS ================= */

    private Request.Builder authRequest(String url) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json");
    }

    private void enqueue(Request request, ApiCallback callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() ->
                        callback.onError("Network error: " + e.getMessage())
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    mainHandler.post(() -> callback.onSuccess(body));
                } else {
                    mainHandler.post(() -> callback.onError(body));
                }
            }
        });
    }

    /* ================= API METHODS ================= */

    // GET /api/collections
    public void getGroups(ApiCallback callback) {
        Request request = authRequest(BASE_URL)
                .get()
                .build();
        enqueue(request, callback);
    }

    // POST /api/collections { name }
    public void createGroup(String name, ApiCallback callback) {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = authRequest(BASE_URL)
                .post(body)
                .build();
        enqueue(request, callback);
    }

    // PUT /api/collections/:id
    public void renameGroup(String groupId, String name, ApiCallback callback) {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = authRequest(BASE_URL + "/" + groupId)
                .put(body)
                .build();
        enqueue(request, callback);
    }

    // DELETE /api/collections/:id
    public void deleteGroup(String groupId, ApiCallback callback) {
        Request request = authRequest(BASE_URL + "/" + groupId)
                .delete()
                .build();
        enqueue(request, callback);
    }

    // POST /api/collections/:id/cards
    public void addCardToGroup(String groupId, String universalId, ApiCallback callback) {
        JsonObject json = new JsonObject();
        json.addProperty("universal_id", universalId);

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = authRequest(BASE_URL + "/" + groupId + "/cards")
                .post(body)
                .build();
        enqueue(request, callback);
    }

    // DELETE /api/collections/:id/cards/:universal_id
    public void removeCardFromGroup(String groupId, String universalId, ApiCallback callback) {
        Request request = authRequest(
                BASE_URL + "/" + groupId + "/cards/" + universalId
        )
                .delete()
                .build();
        enqueue(request, callback);
    }
}
