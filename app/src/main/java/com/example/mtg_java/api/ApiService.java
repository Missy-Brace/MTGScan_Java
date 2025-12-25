package com.example.mtg_java.api;

import com.example.mtg_java.model.AuthResponse;
import com.example.mtg_java.model.Card;
import com.example.mtg_java.model.CardResponse;
import com.example.mtg_java.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("/api/cards")
    Call<CardResponse> getCards(
            @Query("search") String search,
            @Query("page") int page,
            @Query("limit") int limit
    );

    @GET("/api/cards/{id}")
    Call<Card> getCardDetail(@Path("id") String id);

    @POST("/api/auth/register")
    Call<AuthResponse> register(@Body User user);

    @POST("/api/auth/login")
    Call<AuthResponse> login(@Body User user);
}
