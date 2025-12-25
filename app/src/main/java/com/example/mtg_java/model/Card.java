package com.example.mtg_java.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Card {

    @SerializedName("universal_id")
    private String universal_id;

    private String name;

    @SerializedName("image_url")
    private String image_url;

    private String type;
    private String subtype;

    @SerializedName("mana_cost")
    private String mana_cost;

    private String rarity;
    private String text;

    @SerializedName("flavor_text")
    private String flavor_text;

    private String power;
    private String toughness;
    private String loyalty;
    private String defense;

    private List<String> colors;

    @SerializedName("color_identity")
    private List<String> color_identity;

    private List<CardFace> faces;

    @SerializedName("current_price")
    private CurrentPrice current_price;

    // ✅ No-arg constructor
    public Card() {}

    // ✅ Getters
    public String getUniversalId() { return universal_id; }
    public String getName() { return name; }
    public String getImageUrl() { return image_url; }
    public String getType() { return type; }
    public String getSubtype() { return subtype; }
    public String getManaCost() { return mana_cost; }
    public String getRarity() { return rarity; }
    public String getText() { return text; }
    public String getFlavorText() { return flavor_text; }
    public String getPower() { return power; }
    public String getToughness() { return toughness; }
    public String getLoyalty() { return loyalty; }
    public String getDefense() { return defense; }
    public List<String> getColors() { return colors; }
    public List<String> getColorIdentity() { return color_identity; }
    public List<CardFace> getFaces() { return faces; }
    public CurrentPrice getCurrentPrice() { return current_price; }

    // ✅ Setters
    public void setUniversalId(String universal_id) { this.universal_id = universal_id; }
    public void setName(String name) { this.name = name; }
    public void setImageUrl(String image_url) { this.image_url = image_url; }
    public void setType(String type) { this.type = type; }
    public void setSubtype(String subtype) { this.subtype = subtype; }
    public void setManaCost(String mana_cost) { this.mana_cost = mana_cost; }
    public void setRarity(String rarity) { this.rarity = rarity; }
    public void setText(String text) { this.text = text; }
    public void setFlavorText(String flavor_text) { this.flavor_text = flavor_text; }
    public void setPower(String power) { this.power = power; }
    public void setToughness(String toughness) { this.toughness = toughness; }
    public void setLoyalty(String loyalty) { this.loyalty = loyalty; }
    public void setDefense(String defense) { this.defense = defense; }
    public void setColors(List<String> colors) { this.colors = colors; }
    public void setColorIdentity(List<String> color_identity) { this.color_identity = color_identity; }
    public void setFaces(List<CardFace> faces) { this.faces = faces; }
    public void setCurrentPrice(CurrentPrice current_price) { this.current_price = current_price; }

}
