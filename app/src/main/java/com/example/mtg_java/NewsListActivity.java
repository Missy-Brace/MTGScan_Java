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

// FIX: Store the NewsApiManager as a field and cancel the active call in onDestroy().
// Previously the manager was a local variable, making cancel() unreachable and leaving
// an in-flight Retrofit callback that would fire against a destroyed activity.
public class NewsListActivity extends AppCompatActivity {

    private RecyclerView recyclerNews;
    private NewsApiManager api; // FIX: field so we can cancel in onDestroy

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);

        ImageView btnBack = findViewById(R.id.btnBack);
        recyclerNews = findViewById(R.id.recyclerNews);

        recyclerNews.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());

        api = new NewsApiManager(); // FIX: assign to field
        loadNews();
    }

    private void loadNews() {
        api.fetchNews(50, 0, new NewsApiManager.NewsCallback() {
            @Override
            public void onSuccess(NewsResponse response) {
                if (isFinishing() || isDestroyed()) return; // FIX: guard against destroyed activity
                recyclerNews.setAdapter(
                        new NewsAdapter(response.getItems(), R.layout.item_news_vertical)
                );
            }

            @Override
            public void onError(String message) { }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // FIX: cancel the Retrofit call so the callback never fires after destroy
        if (api != null) api.cancel();
    }
}
