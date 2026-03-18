package com.example.mtg_java.model;

import java.util.ArrayList;
import java.util.List;

public class Group {

    private String _id;
    private String name;
    private int cardCount;
    private List<String> cardIds = new ArrayList<>();

    public String getId() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public int getCardCount() { return cardCount; }

    public List<String> getCardIds() {
        return cardIds;
    }
    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }

    public void setCardIds(List<String> cardIds) {
        this.cardIds = cardIds;
    }
}
