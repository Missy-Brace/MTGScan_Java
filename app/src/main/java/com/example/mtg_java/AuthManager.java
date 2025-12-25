package com.example.mtg_java;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

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

public class AuthManager {

    private static final String BASE_URL = "https://mtgscan1.onrender.com/"; // replace with your backend IP
    private final OkHttpClient client;
    private final Handler mainHandler;

    public AuthManager(Context context) {
        client = new OkHttpClient();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public interface AuthCallback {
        void onSuccess();
        void onError(String message);
    }

    public void login(String email, String password, AuthCallback callback) {
        JsonObject json = new JsonObject();
        json.addProperty("email", email);
        json.addProperty("password", password);
        postRequest(BASE_URL + "api/auth/login", json.toString(), callback);
    }

    public void register(String username, String email, String password, AuthCallback callback) {
        JsonObject json = new JsonObject();
        json.addProperty("username", username);
        json.addProperty("email", email);
        json.addProperty("password", password);
        postRequest(BASE_URL + "api/auth/register", json.toString(), callback);
    }


    private void postRequest(String url, String jsonBody, AuthCallback callback) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    mainHandler.post(callback::onSuccess);
                } else {
                    Gson gson = new Gson();
                    try {
                        JsonObject json = gson.fromJson(responseBody, JsonObject.class);
                        String message = json.has("message") ? json.get("message").getAsString() : "Error";
                        mainHandler.post(() -> callback.onError(message));
                    } catch (Exception e) {
                        mainHandler.post(() -> callback.onError("Unexpected error"));
                    }
                }
            }
        });
    }
}
