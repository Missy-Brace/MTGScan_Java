package com.example.mtg_java.api;

import com.example.mtg_java.model.NewsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApi {

    @GET("api/news")
    Call<NewsResponse> getNews(
            @Query("limit") int limit,
            @Query("skip") int skip
    );
}