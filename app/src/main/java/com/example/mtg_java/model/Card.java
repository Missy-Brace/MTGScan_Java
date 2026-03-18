package com.example.mtg_java.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

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


    private String artist;
    private String language;
    private String layout;

    private List<String> keywords;

    @SerializedName("legal_formats")
    private List<String> legal_formats;

    private Map<String, String> legalities;

    @SerializedName("not_legal_formats")
    private List<String> not_legal_formats;

    @SerializedName("oracle_id")
    private String oracle_id;

    @SerializedName("released_at")
    private String released_at;

    private SetInfo set;

    @SerializedName("set_id")
    private String set_id;

    @SerializedName("set_name")
    private String set_name;

    @SerializedName("created_at")
    private String created_at;

    @SerializedName("updated_at")
    private String updated_at;


    @SerializedName("display_face_index")
    private Integer display_face_index;


    public Card() {}


    public String getUniversalId() { return universal_id; }
    public String getName() { return name; }
    public String getImageUrl() { return image_url; }
    public String getType() { return type; }
    public String getSubtype() { return subtype; }
    public String getManaCost() { return mana_cost; }
    public String getRarity() { return rarity; }
    public String getText() { return text; }
    public CurrentPrice getCurrentPrice() {
        return current_price;
    }



    public String getFlavorText() { return flavor_text; }
    public String getPower() { return power; }
    public String getToughness() { return toughness; }
    public String getLoyalty() { return loyalty; }
    public String getDefense() { return defense; }
    public List<String> getColors() { return colors; }
    public List<String> getColorIdentity() { return color_identity; }
    public List<CardFace> getFaces() { return faces; }

    public static class CurrentPrice {
        private Double usd;
        private Double usd_foil;
        private Double eur;
        private String as_of;

        public Double getUsd() { return usd; }
        public Double getUsdFoil() { return usd_foil; }
        public Double getEur() { return eur; }
        public String getAsOf() { return as_of; }
    }

    // ===== NEW GETTERS =====
    public String getArtist() { return artist; }
    public String getLanguage() { return language; }
    public String getLayout() { return layout; }
    public List<String> getKeywords() { return keywords; }
    public List<String> getLegalFormats() { return legal_formats; }
    public Map<String, String> getLegalities() { return legalities; }
    public List<String> getNotLegalFormats() { return not_legal_formats; }
    public String getOracleId() { return oracle_id; }
    public String getReleasedAt() { return released_at; }
    public SetInfo getSet() { return set; }
    public String getSetId() { return set_id; }
    public String getSetName() { return set_name; }
    public String getCreatedAt() { return created_at; }
    public String getUpdatedAt() { return updated_at; }
    public Integer getDisplayFaceIndex() { return display_face_index; }


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


    public void setArtist(String artist) { this.artist = artist; }
    public void setLanguage(String language) { this.language = language; }
    public void setLayout(String layout) { this.layout = layout; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }
    public void setLegalFormats(List<String> legal_formats) { this.legal_formats = legal_formats; }
    public void setLegalities(Map<String, String> legalities) { this.legalities = legalities; }
    public void setNotLegalFormats(List<String> not_legal_formats) { this.not_legal_formats = not_legal_formats; }
    public void setOracleId(String oracle_id) { this.oracle_id = oracle_id; }
    public void setReleasedAt(String released_at) { this.released_at = released_at; }
    public void setSet(SetInfo set) { this.set = set; }
    public void setSetId(String set_id) { this.set_id = set_id; }
    public void setSetName(String set_name) { this.set_name = set_name; }
    public void setCreatedAt(String created_at) { this.created_at = created_at; }
    public void setUpdatedAt(String updated_at) { this.updated_at = updated_at; }
    public void setDisplayFaceIndex(Integer display_face_index) { this.display_face_index = display_face_index; }
}
