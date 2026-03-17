package com.example.mtg_java.api;

import com.example.mtg_java.model.NewsResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsApiManager {

    private final NewsApi api;
    private Call<NewsResponse> activeCall;

    public interface NewsCallback {
        void onSuccess(NewsResponse response);
        void onError(String msg);
    }

    public NewsApiManager() {
        api = ApiClient.getClient().create(NewsApi.class);
    }

    public void fetchNews(int limit, int skip, NewsCallback callback) {
        activeCall = api.getNews(limit, skip);
        activeCall.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call,
                                   Response<NewsResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to load news");
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                if (!call.isCanceled()) {
                    callback.onError(t.getMessage());
                }
            }
        });
    }

    public void cancel() {
        if (activeCall != null) {
            activeCall.cancel();
            activeCall = null;
        }
    }
}