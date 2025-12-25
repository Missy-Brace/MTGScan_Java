package com.example.mtg_java.model;

public class CardFace {

    private String name;
    private String type;
    private String mana_cost;
    private String text;
    private String image_url;
    private String power;
    private String toughness;
    private String loyalty;
    private String defense;

    // Getters
    public String getName() { return name; }
    public String getType() { return type; }
    public String getManaCost() { return mana_cost; }
    public String getText() { return text; }
    public String getImageUrl() { return image_url; }
    public String getPower() { return power; }
    public String getToughness() { return toughness; }
    public String getLoyalty() { return loyalty; }
    public String getDefense() { return defense; }

    // Optional setters if you need
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setManaCost(String mana_cost) { this.mana_cost = mana_cost; }
    public void setText(String text) { this.text = text; }
    public void setImageUrl(String image_url) { this.image_url = image_url; }
}
