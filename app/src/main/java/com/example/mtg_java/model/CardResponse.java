package com.example.mtg_java.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CardResponse {
    @SerializedName("items")
    public List<CardItem> items;

    public static class CardItem {
        @SerializedName("universal_id")
        public String id;

        @SerializedName("name")
        public String name;

        @SerializedName("type")
        public String type;

        @SerializedName("image_url")
        public String imageUrl;
    }
}
