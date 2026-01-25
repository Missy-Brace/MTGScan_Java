package com.example.mtg_java;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mtg_java.adapter.CardAdapter;
import com.example.mtg_java.api.ApiClient;
import com.example.mtg_java.api.ApiService;
import com.example.mtg_java.model.Card;
import com.example.mtg_java.model.CardResponse;
import com.example.mtg_java.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCardToCollectionFragment extends Fragment {

    RecyclerView recyclerView;
    CardAdapter adapter;
    List<Card> cardList = new ArrayList<>();

    String groupId;
    SessionManager session;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_card, container, false);

        recyclerView = view.findViewById(R.id.recyclerCards);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter = new CardAdapter(getContext(), cardList, card -> addCard(card));
        recyclerView.setAdapter(adapter);

        session = new SessionManager(requireContext());

        if (getArguments() != null) {
            groupId = getArguments().getString("group_id");
        }

        loadCards();

        return view;
    }

    private void loadCards() {
        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getCards("", 1, 30).enqueue(new Callback<CardResponse>() {
            @Override
            public void onResponse(Call<CardResponse> call, Response<CardResponse> res) {
                if (res.isSuccessful() && res.body() != null && res.body().items != null) {
                    cardList.clear();
                    for (CardResponse.CardItem c : res.body().items) {
                        Card card = new Card();
                        card.setUniversalId(c.id);
                        card.setName(c.name);
                        card.setImageUrl(c.imageUrl);
                        cardList.add(card);
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<CardResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load cards", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addCard(Card card) {
        ApiService api = ApiClient.getClient().create(ApiService.class);

        Map<String, String> body = new HashMap<>();
        body.put("universal_id", card.getUniversalId());

        api.addCardToGroup(
                groupId,
                "Bearer " + session.getToken(),
                body
        ).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> r) {
                Toast.makeText(getContext(), "Added!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
