package com.example.mtg_java;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mtg_java.adapter.CardItemAdapter;
import com.example.mtg_java.api.ApiClient;
import com.example.mtg_java.api.ApiService;
import com.example.mtg_java.model.CardResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchResultActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ApiService api;
    TextView tvSummary;
    TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);


        recyclerView = findViewById(R.id.recycler);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvSummary = findViewById(R.id.tvSummary);
        ImageButton btnBack = findViewById(R.id.btnBack);
        LinearLayout btnRefine = findViewById(R.id.btnRefine);


        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        api = ApiClient.getClient().create(ApiService.class);


        btnBack.setOnClickListener(v -> finish());


        btnRefine.setOnClickListener(v -> finish());


        String summary = getIntent().getStringExtra("summary");
        tvSummary.setText(summary);

        loadResults();
    }

    private void loadResults() {
        Intent i = getIntent();

        String name = i.getStringExtra("name");
        String text = i.getStringExtra("text");
        String type = i.getStringExtra("type");
        String artist = i.getStringExtra("artist");
        String manaStr = i.getStringExtra("mana");
        Integer mana = (manaStr == null || manaStr.isEmpty()) ? null : Integer.parseInt(manaStr);

        ArrayList<String> colors = i.getStringArrayListExtra("colors");
        ArrayList<String> colorIdentity = i.getStringArrayListExtra("colorIdentity");
        String rarity = i.getStringExtra("rarity");

        api.searchCards(
                "", 1, 20,
                name, text, type, artist,
                rarity, mana,
                colors, colorIdentity
        ).enqueue(new Callback<CardResponse>() {
            @Override
            public void onResponse(Call<CardResponse> call, Response<CardResponse> res) {
                if (res.body() == null) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    return;
                }
                List<CardResponse.CardItem> cards = res.body().items;

                if (cards == null || cards.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.setAdapter(
                            new CardItemAdapter(SearchResultActivity.this, cards)
                    );
                }

            }

            @Override
            public void onFailure(Call<CardResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
