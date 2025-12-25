package com.example.mtg_java;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.mtg_java.adapter.CardAdapter;
import com.example.mtg_java.api.ApiClient;
import com.example.mtg_java.api.ApiService;
import com.example.mtg_java.model.Card;
import com.example.mtg_java.model.CardResponse;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BrowseFragment extends Fragment {

    RecyclerView recyclerView;
    CardAdapter adapter;
    List<Card> cardList = new ArrayList<>();

    public BrowseFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);

// 2 columns grid
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter = new CardAdapter(getContext(), cardList);
        recyclerView.setAdapter(adapter);


        loadCards();

        return view;
    }

    private void loadCards() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getCards("", 1, 20).enqueue(new Callback<CardResponse>() {
            @Override
            public void onResponse(Call<CardResponse> call, Response<CardResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().items != null) {
                    cardList.clear();
                    for (CardResponse.CardItem c : response.body().items) {
                        Card card = new Card();
                        card.setUniversalId(c.id);
                        card.setName(c.name);
                        card.setImageUrl(c.imageUrl);
                        card.setType(c.type);
                        cardList.add(card);
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<CardResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
