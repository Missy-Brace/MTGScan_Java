package com.example.mtg_java;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

// FIX: Store the in-flight Retrofit Call as a field and cancel it in onDestroyView().
// The original had no cancellation at all — the callback would fire against a detached
// fragment, which is a common source of crashes and stale-data bugs.
public class FilterResultFragment extends Fragment {

    RecyclerView recycler;
    CardAdapter adapter;
    List<Card> list = new ArrayList<>();

    String name, text, type, artist;

    // FIX: store call reference so we can cancel it
    private Call<CardResponse> activeCall;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_results, container, false);
        recycler = v.findViewById(R.id.recyclerView);
        recycler.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new CardAdapter(getContext(), list);
        recycler.setAdapter(adapter);

        Bundle b = getArguments();
        if (b != null) {
            name   = b.getString("name");
            text   = b.getString("text");
            type   = b.getString("type");
            artist = b.getString("artist");
        }

        load();
        return v;
    }

    private void load() {
        ApiService api = ApiClient.getClient().create(ApiService.class);

        // FIX: cancel any previous in-flight call before starting a new one
        if (activeCall != null && !activeCall.isCanceled()) {
            activeCall.cancel();
        }

        activeCall = api.getCards(null, 1, 20, name, text, type, artist);
        activeCall.enqueue(new Callback<CardResponse>() {
            @Override
            public void onResponse(Call<CardResponse> c, Response<CardResponse> r) {
                // FIX: guard against detached fragment
                if (c.isCanceled() || !isAdded()) return;

                if (r.body() != null && r.body().items != null) {
                    list.clear();
                    for (CardResponse.CardItem i : r.body().items) {
                        Card card = new Card();
                        card.setName(i.name);
                        card.setImageUrl(i.imageUrl);
                        card.setUniversalId(i.id);
                        list.add(card);
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<CardResponse> c, Throwable t) {
                if (!c.isCanceled()) t.printStackTrace();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // FIX: cancel in-flight call to prevent callback firing on detached fragment
        if (activeCall != null) {
            activeCall.cancel();
            activeCall = null;
        }
        recycler.setAdapter(null);
        recycler = null;
    }
}
