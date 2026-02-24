package com.example.mtg_java.model;

import java.util.List;

public class NewsResponse {

    private List<News> items;
    private int total;
    private long cachedAt;

    public List<News> getItems() {
        return items;
    }
}