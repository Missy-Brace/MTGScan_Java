package com.example.mtg_java.api;

import com.example.mtg_java.model.AuthResponse;
import com.example.mtg_java.model.Card;
import com.example.mtg_java.model.CardResponse;
import com.example.mtg_java.model.Group;
import com.example.mtg_java.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.DELETE;
import retrofit2.http.Header;

public interface ApiService {


    @GET("/api/cards")
    Call<CardResponse> getCards(
            @Query("search") String search,
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("name") String name,
            @Query("text") String text,
            @Query("type") String type,
            @Query("artist") String artist
    );
    @GET("/api/cards")
    Call<CardResponse> searchCards(
            @Query("search") String search,
            @Query("page") int page,
            @Query("limit") int limit,

            @Query("name") String name,
            @Query("text") String text,
            @Query("type") String type,
            @Query("artist") String artist,

            @Query("rarity") String rarity,
            @Query("manaCost") Integer manaCost,

            @Query("colors") List<String> colors,
            @Query("colorIdentity") List<String> colorIdentity
    );


    @GET("/api/cards/{id}")
    Call<Card> getCardDetail(@Path("id") String id);

    @POST("/api/auth/register")
    Call<AuthResponse> register(@Body User user);

    @POST("/api/auth/login")
    Call<AuthResponse> login(@Body User user);
    // ================= COLLECTION API =================

    // GET all collections
    @GET("/api/collections")
    Call<List<Group>> getGroups(
            @Header("Authorization") String token
    );

    // POST create collection
    @POST("/api/collections")
    Call<com.example.mtg_java.model.Group> createGroup(
            @Header("Authorization") String token,
            @Body java.util.Map<String, String> body
    );

    // PUT rename collection
    @PUT("/api/collections/{id}")
    Call<com.example.mtg_java.model.Group> renameGroup(
            @Path("id") String id,
            @Header("Authorization") String token,
            @Body java.util.Map<String, String> body
    );

    // DELETE collection
    @DELETE("/api/collections/{id}")
    Call<Void> deleteGroup(
            @Path("id") String id,
            @Header("Authorization") String token
    );

    // POST add card to collection
    @POST("/api/collections/{groupId}/cards")
    Call<Void> addCardToGroup(
            @Path("groupId") String groupId,
            @Header("Authorization") String token,
            @Body java.util.Map<String, String> body
    );
    @GET("/api/collections/{groupId}/cards")
    Call<List<Card>> getCardsInGroup(
            @Path("groupId") String groupId,
            @Header("Authorization") String token
    );



    // DELETE remove card from collection
    @DELETE("/api/collections/{groupId}/cards/{universalId}")
    Call<Void> removeCardFromGroup(
            @Path("groupId") String groupId,
            @Path("universalId") String universalId,
            @Header("Authorization") String token
    );

}
