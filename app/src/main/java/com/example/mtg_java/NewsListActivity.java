package com.example.mtg_java;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mtg_java.adapter.NewsAdapter;
import com.example.mtg_java.api.NewsApiManager;
import com.example.mtg_java.model.NewsResponse;
import com.example.mtg_java.utils.LocalCache;
import com.example.mtg_java.model.News;
import java.util.List;


public class NewsListActivity extends AppCompatActivity {

    private RecyclerView recyclerNews;
    private NewsApiManager api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);

        ImageView btnBack = findViewById(R.id.btnBack);
        recyclerNews = findViewById(R.id.recyclerNews);

        recyclerNews.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());

        LocalCache cache = LocalCache.getInstance(this);
        List<News> cached = cache.getNews();
        if (cached != null) {
            recyclerNews.setAdapter(new NewsAdapter(cached, R.layout.item_news_vertical));
        }


        api = new NewsApiManager();
        if (isNetworkAvailable()) loadNews();
    }

    private void loadNews() {
        api.fetchNews(50, 0, new NewsApiManager.NewsCallback() {
            @Override
            public void onSuccess(NewsResponse response) {
                if (isFinishing() || isDestroyed()) return;
                recyclerNews.setAdapter(
                        new NewsAdapter(response.getItems(), R.layout.item_news_vertical)
                );
            }

            @Override
            public void onError(String message) { }
        });
    }
    private boolean isNetworkAvailable() {
        android.net.ConnectivityManager cm =
                (android.net.ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        android.net.NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (api != null) api.cancel();
    }

}
