package com.example.mtg_java.model;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String name;
    private List<String> cardIds = new ArrayList<>();

    public Group(String name) { this.name = name; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getCardIds() { return cardIds; }
    public void setCardIds(List<String> cardIds) { this.cardIds = cardIds; }
}