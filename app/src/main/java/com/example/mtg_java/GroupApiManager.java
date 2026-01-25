package com.example.mtg_java;

import android.os.Handler;
import android.os.Looper;

import com.example.mtg_java.model.Group;
import com.example.mtg_java.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.*;

public class GroupApiManager {

    private static final String BASE_URL = "https://mtgscan1.onrender.com/api/collections";
    private final OkHttpClient client = new OkHttpClient();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();

    public interface ListCallback {
        void onSuccess(List<Group> groups);
        void onError(String msg);
    }
    public interface ObjectCallback {
        void onSuccess(Group group);
        void onError(String msg);
    }

    public interface SimpleCallback {
        void onDone();
        void onError(String msg);
    }

    // GET /api/collections
    public void getGroups(SessionManager session, ListCallback cb) {
        Request req = new Request.Builder()
                .url(BASE_URL)
                .addHeader("Authorization", "Bearer " + session.getToken())
                .build();

        client.newCall(req).enqueue(listCallback(cb));
    }

    // POST /api/collections
    // POST /api/collections
    public void createGroup(SessionManager session, String name, ObjectCallback cb) {
        RequestBody body = RequestBody.create(
                gson.toJson(new NameBody(name)),
                MediaType.get("application/json")
        );

        Request req = new Request.Builder()
                .url(BASE_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + session.getToken())
                .build();

        client.newCall(req).enqueue(objectCallback(cb));
    }


    // PUT /api/collections/:id
    public void renameGroup(SessionManager session, String id, String name, SimpleCallback cb) {
        RequestBody body = RequestBody.create(
                gson.toJson(new NameBody(name)),
                MediaType.get("application/json")
        );

        Request req = new Request.Builder()
                .url(BASE_URL + "/" + id)
                .put(body)
                .addHeader("Authorization", "Bearer " + session.getToken())
                .build();

        client.newCall(req).enqueue(simple(cb));
    }

    // DELETE /api/collections/:id
    public void deleteGroup(SessionManager session, String id, SimpleCallback cb) {
        Request req = new Request.Builder()
                .url(BASE_URL + "/" + id)
                .delete()
                .addHeader("Authorization", "Bearer " + session.getToken())
                .build();

        client.newCall(req).enqueue(simple(cb));
    }

    // POST /api/collections/:id/cards
    public void addCard(SessionManager session, String groupId, String universalId, SimpleCallback cb) {
        RequestBody body = RequestBody.create(
                gson.toJson(new CardBody(universalId)),
                MediaType.get("application/json")
        );

        Request req = new Request.Builder()
                .url(BASE_URL + "/" + groupId + "/cards")
                .post(body)
                .addHeader("Authorization", "Bearer " + session.getToken())
                .build();

        client.newCall(req).enqueue(simple(cb));
    }

    // DELETE /api/collections/:id/cards/:universal_id
    public void removeCard(SessionManager session, String groupId, String universalId, SimpleCallback cb) {
        Request req = new Request.Builder()
                .url(BASE_URL + "/" + groupId + "/cards/" + universalId)
                .delete()
                .addHeader("Authorization", "Bearer " + session.getToken())
                .build();

        client.newCall(req).enqueue(simple(cb));
    }


    // ---------- helpers ----------
    private Callback objectCallback(ObjectCallback cb) {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                main.post(() -> cb.onError(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response r) throws IOException {
                String body = r.body().string();
                try {
                    Group g = gson.fromJson(body, Group.class);
                    main.post(() -> cb.onSuccess(g));
                } catch (Exception e) {
                    main.post(() -> cb.onError("Bad server response"));
                }
            }
        };
    }

    private Callback listCallback(ListCallback cb) {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                main.post(() -> cb.onError(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response r) throws IOException {
                String body = r.body().string();

                try {
                    // if server returned error object instead of array
                    if (body.trim().startsWith("{")) {
                        main.post(() -> cb.onError("Server error: " + body));
                        return;
                    }

                    Type t = new TypeToken<List<Group>>(){}.getType();
                    List<Group> g = gson.fromJson(body, t);

                    main.post(() -> cb.onSuccess(g));

                } catch (Exception e) {
                    main.post(() -> cb.onError("Bad server response"));
                }
            }
        };
    }


    private Callback simple(SimpleCallback cb) {
        return new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                main.post(() -> cb.onError(e.getMessage()));
            }

            @Override public void onResponse(Call call, Response r) {
                main.post(cb::onDone);
            }
        };
    }

    static class NameBody { String name; NameBody(String n){name=n;} }
    static class CardBody { String universal_id; CardBody(String u){universal_id=u;} }
}
