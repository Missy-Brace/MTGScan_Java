package com.example.mtg_java;




import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mtg_java.R;
import com.example.mtg_java.adapter.NewsAdapter;
import com.example.mtg_java.api.NewsApiManager;
import com.example.mtg_java.model.NewsResponse;

public class NewsListActivity extends AppCompatActivity {

    private RecyclerView recyclerNews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);

        ImageView btnBack = findViewById(R.id.btnBack);
        recyclerNews = findViewById(R.id.recyclerNews);

        recyclerNews.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());

        loadNews();
    }

    private void loadNews() {

        NewsApiManager api = new NewsApiManager();

        api.fetchNews(50, 0, new NewsApiManager.NewsCallback() {
            @Override
            public void onSuccess(NewsResponse response) {
                recyclerNews.setAdapter(
                        new NewsAdapter(response.getItems(), R.layout.item_news_vertical)
                );
            }

            @Override
            public void onError(String message) { }
        });
    }
}