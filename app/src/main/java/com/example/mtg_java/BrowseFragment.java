package com.example.mtg_java;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    EditText searchInput;
    String currentQuery = "";
    int currentPage = 1;

    Call<CardResponse> inFlightCall;
    Handler searchHandler = new Handler(Looper.getMainLooper());
    Runnable searchRunnable;

    public BrowseFragment() {}

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_browse, container, false);

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );
        view.findViewById(R.id.btnFilter).setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, new FilterFragment())
                    .addToBackStack(null)
                    .commit();
        });

        recyclerView = view.findViewById(R.id.recyclerView);
        searchInput = view.findViewById(R.id.searchInput);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new CardAdapter(requireContext(), cardList);

        recyclerView.setAdapter(adapter);

        // Debounced search
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString().trim();

                if (searchRunnable != null)
                    searchHandler.removeCallbacks(searchRunnable);

                searchRunnable = () -> {
                    currentPage = 1;
                    loadCards(true);
                };

                searchHandler.postDelayed(searchRunnable, 400);
            }
        });

        loadCards(true);
        return view;
    }


    private void loadCards(boolean reset) {

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        if (inFlightCall != null && !inFlightCall.isCanceled()) {
            inFlightCall.cancel();
        }

        inFlightCall = apiService.getCards(
                currentQuery,
                currentPage,
                20,
                null,   // name
                null,   // text
                null,   // type
                null    // rarity
        );

        inFlightCall.enqueue(new Callback<CardResponse>() {
            @Override
            public void onResponse(Call<CardResponse> call, Response<CardResponse> response) {

                if (call.isCanceled()) return;

                if (response.isSuccessful() && response.body() != null && response.body().items != null) {

                    cardList.clear(); // ALWAYS replace list

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
                if (!call.isCanceled()) t.printStackTrace();
            }
        });
    }
}
