package com.example.mtg_java.api;

import com.example.mtg_java.model.CardResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CardApi {
    @GET("/api/cards")
    Call<CardResponse> getCards(
            @Query("search") String search,
            @Query("page") int page,
            @Query("limit") int limit
    );
}
