package com.example.mtg_java.model;

public class CurrentPrice {

    private String usd;
    private String eur;
    private String tix;

    // Getters
    public String getUsd() { return usd; }
    public String getEur() { return eur; }
    public String getTix() { return tix; }

    // Optional setters if needed
    public void setUsd(String usd) { this.usd = usd; }
    public void setEur(String eur) { this.eur = eur; }
    public void setTix(String tix) { this.tix = tix; }
}
