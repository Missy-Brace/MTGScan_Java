package com.example.mtg_java;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mtg_java.adapter.CardAdapter;
import com.example.mtg_java.api.ApiClient;
import com.example.mtg_java.api.ApiService;
import com.example.mtg_java.model.Card;
import com.example.mtg_java.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CollectionDetailActivity extends AppCompatActivity {

    String groupId;
    SessionManager session;

    RecyclerView recyclerView;
    CardAdapter adapter;
    List<Card> cardList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_detail);

        groupId = getIntent().getStringExtra("group_id");
        session = new SessionManager(this);

        recyclerView = findViewById(R.id.recyclerCards);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new CardAdapter(this, cardList);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnAdd).setOnClickListener(v -> {
            startActivity(new Intent(this, AddCardToCollectionActivity.class)
                    .putExtra("group_id", groupId));
        });

        loadGroupCards();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGroupCards();   // 🔥 THIS is what refreshes after back
    }

    private void loadGroupCards() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getCardsInGroup(groupId, "Bearer " + session.getToken())
                .enqueue(new Callback<List<Card>>() {
                    @Override
                    public void onResponse(Call<List<Card>> call, Response<List<Card>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            cardList.clear();
                            cardList.addAll(response.body());
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Card>> call, Throwable t) {
                        Toast.makeText(CollectionDetailActivity.this,
                                "Failed to load collection", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
