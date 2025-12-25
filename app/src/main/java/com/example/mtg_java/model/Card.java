package com.example.mtg_java.model;

public class Card {
    private String name;
    private String type;
    private String imageUrl;

    // Constructor
    public Card(String name, String type, String imageUrl) {
        this.name = name;
        this.type = type;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getName() { return name; }
    public String getType() { return type; }
    public String getImageUrl() { return imageUrl; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
