package com.example.mtg_java;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mtg_java.adapter.CardAdapter;
import com.example.mtg_java.api.CardApi;
import com.example.mtg_java.api.ApiClient;
import com.example.mtg_java.model.Card;
import com.example.mtg_java.model.CardResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class BrowseFragment extends Fragment {

    private RecyclerView recyclerView;
    private CardAdapter adapter;
    private List<Card> cardList;

    public BrowseFragment() {
        super(R.layout.fragment_browse);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        cardList = new ArrayList<>();
        adapter = new CardAdapter(cardList, card -> {
            // Handle card click
        });
        recyclerView.setAdapter(adapter);

        // Fetch real cards from server
        fetchCardsFromServer();
    }

    private void fetchCardsFromServer() {
        Retrofit retrofit = ApiClient.getClient();
        CardApi api = retrofit.create(CardApi.class);

        api.getCards("", 1, 20).enqueue(new Callback<CardResponse>() {
            @Override
            public void onResponse(Call<CardResponse> call, Response<CardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cardList.clear();
                    for (CardResponse.CardItem c : response.body().items) {
                        cardList.add(new Card(c.name, c.type, c.imageUrl));
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
