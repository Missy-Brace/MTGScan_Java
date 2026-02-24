package com.example.mtg_java;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.example.mtg_java.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthManager {

    private static final String BASE_URL = "https://mtgscan1.onrender.com/";
    private final OkHttpClient client;
    private final Handler mainHandler;
    private final Context context;

    public AuthManager(Context context) {
        this.context = context;
        client = new OkHttpClient();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    // ================= IMAGE UPLOAD =================

    public interface ImageUploadCallback {
        void onSuccess(String imageUrl);
        void onError(String message);
    }

    public void uploadAvatar(Uri imageUri, ImageUploadCallback callback) {

        try {
            SessionManager session = new SessionManager(context);
            String token = session.getToken();

            File file = FileUtils.getFile(context, imageUri);

            MediaType MEDIA_TYPE_IMAGE = MediaType.parse("image/*");
            RequestBody fileBody = RequestBody.create(file, MEDIA_TYPE_IMAGE);

            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(), fileBody)
                    .build();

            Request request = new Request.Builder()
                    .url(BASE_URL + "api/files/avatar")
                    .addHeader("Authorization", "Bearer " + token)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    mainHandler.post(() ->
                            callback.onError("Network error: " + e.getMessage())
                    );
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response)
                        throws IOException {

                    String responseBody = response.body().string();

                    if (response.isSuccessful()) {
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            String imageUrl = json.getString("url");

// 🔥 Force HTTPS
                            if (imageUrl.startsWith("http://")) {
                                imageUrl = imageUrl.replace("http://", "https://");
                            }

// If backend returns relative path
                            if (!imageUrl.startsWith("http")) {
                                imageUrl = BASE_URL + imageUrl;
                            }
                            android.util.Log.d("SERVER_IMAGE_URL", imageUrl);

                            String finalImageUrl = imageUrl;

                            mainHandler.post(() ->
                                    callback.onSuccess(finalImageUrl)
                            );

                        } catch (Exception e) {
                            mainHandler.post(() ->
                                    callback.onError("Invalid upload response")
                            );
                        }
                    } else {
                        mainHandler.post(() ->
                                callback.onError("Upload failed: " + responseBody)
                        );
                    }
                }
            });

        } catch (Exception e) {
            mainHandler.post(() ->
                    callback.onError("Error: " + e.getMessage())
            );
        }
    }

    // ================= AUTH CALLBACK =================

    public interface AuthCallback {
        void onSuccess(String token, String userId, String username, String email, String profileImage);
        void onError(String message);
    }

    // ================= LOGIN =================

    public void login(String email, String password, AuthCallback callback) {
        JsonObject json = new JsonObject();
        json.addProperty("email", email);
        json.addProperty("password", password);
        postRequest(BASE_URL + "api/auth/login", json.toString(), callback);
    }

    // ================= REGISTER =================

    public void register(String username, String email, String password, AuthCallback callback) {
        JsonObject json = new JsonObject();
        json.addProperty("username", username);
        json.addProperty("email", email);
        json.addProperty("password", password);
        postRequest(BASE_URL + "api/auth/register", json.toString(), callback);
    }

    // ================= DELETE ACCOUNT =================

    public void deleteAccount(AuthCallback callback) {

        SessionManager session = new SessionManager(context);
        String token = session.getToken();

        Request request = new Request.Builder()
                .url(BASE_URL + "api/users/me")
                .addHeader("Authorization", "Bearer " + token)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() ->
                        callback.onError("Network error")
                );
            }

            @Override
            public void onResponse(Call call, Response response) {

                if (response.isSuccessful()) {
                    session.clearSession();
                    mainHandler.post(() ->
                            callback.onSuccess("", "", "", "", "")
                    );
                } else {
                    mainHandler.post(() ->
                            callback.onError("Delete failed")
                    );
                }
            }
        });
    }

    // ================= CHANGE PASSWORD =================

    public void changePassword(String currentPassword, String newPassword, AuthCallback callback) {

        SessionManager session = new SessionManager(context);
        String token = session.getToken();

        JsonObject json = new JsonObject();
        json.addProperty("currentPassword", currentPassword);
        json.addProperty("newPassword", newPassword);

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json.toString(), JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "api/users/me/password")
                .addHeader("Authorization", "Bearer " + token)
                .put(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() ->
                        callback.onError("Network error")
                );
            }

            @Override
            public void onResponse(Call call, Response response) {

                if (response.isSuccessful()) {
                    mainHandler.post(() ->
                            callback.onSuccess("", "", "", "", "")
                    );
                } else {
                    mainHandler.post(() ->
                            callback.onError("Password change failed")
                    );
                }
            }
        });
    }

    // ================= UPDATE PROFILE =================

    public void updateProfile(String username, String email, String profileImage, AuthCallback callback) {

        SessionManager session = new SessionManager(context);
        String token = session.getToken();

        JsonObject json = new JsonObject();
        json.addProperty("username", username);
        json.addProperty("email", email);
        json.addProperty("profileImage", profileImage);

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json.toString(), JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "api/users/me")
                .addHeader("Authorization", "Bearer " + token)
                .put(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() ->
                        callback.onError("Network error")
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responseBody = response.body().string();

                if (response.isSuccessful()) {

                    Gson gson = new Gson();
                    JsonObject user = gson.fromJson(responseBody, JsonObject.class);

                    String image = user.has("profileImage") && !user.get("profileImage").isJsonNull()
                            ? user.get("profileImage").getAsString()
                            : "";

                    mainHandler.post(() ->
                            callback.onSuccess(
                                    "",
                                    user.get("id").getAsString(),
                                    user.get("username").getAsString(),
                                    user.get("email").getAsString(),
                                    image
                            )
                    );

                } else {
                    mainHandler.post(() ->
                            callback.onError("Update failed")
                    );
                }
            }
        });
    }

    // ================= GET CURRENT USER =================

    public void getCurrentUser(AuthCallback callback) {

        SessionManager session = new SessionManager(context);
        String token = session.getToken();

        Request request = new Request.Builder()
                .url(BASE_URL + "api/users/me")
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() ->
                        callback.onError("Network error")
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String body = response.body().string();

                if (response.isSuccessful()) {

                    Gson gson = new Gson();
                    JsonObject user = gson.fromJson(body, JsonObject.class);

                    String image = user.has("profileImage") && !user.get("profileImage").isJsonNull()
                            ? user.get("profileImage").getAsString()
                            : "";

                    mainHandler.post(() ->
                            callback.onSuccess(
                                    "",
                                    user.get("id").getAsString(),
                                    user.get("username").getAsString(),
                                    user.get("email").getAsString(),
                                    image
                            )
                    );

                } else {
                    mainHandler.post(() ->
                            callback.onError("Failed to fetch user")
                    );
                }
            }
        });
    }

    // ================= COMMON POST =================

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
                mainHandler.post(() ->
                        callback.onError("Network error: " + e.getMessage())
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    try {
                        Gson gson = new Gson();
                        JsonObject json = gson.fromJson(responseBody, JsonObject.class);

                        String token = json.has("token")
                                ? json.get("token").getAsString()
                                : "";

                        JsonObject user = json.getAsJsonObject("user");

                        String userId = user.has("id") ? user.get("id").getAsString() : "";
                        String username = user.has("username") ? user.get("username").getAsString() : "";
                        String email = user.has("email") ? user.get("email").getAsString() : "";
                        String image = user.has("profileImage") && !user.get("profileImage").isJsonNull()
                                ? user.get("profileImage").getAsString()
                                : "";

                        SessionManager session = new SessionManager(context);
                        session.saveToken(token);
                        session.saveUserId(userId);
                        session.saveUser(username, email);
                        session.setLoggedIn(true);

                        mainHandler.post(() ->
                                callback.onSuccess(token, userId, username, email, image)
                        );

                    } catch (Exception e) {
                        mainHandler.post(() ->
                                callback.onError("Invalid server response")
                        );
                    }
                } else {
                    mainHandler.post(() ->
                            callback.onError("Authentication failed")
                    );
                }
            }
        });
    }
}