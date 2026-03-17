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
import android.widget.TextView;
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

// ADDED: Filter button wired to FilterFragment.forCollection(), which calls back
// with the selected params and triggers a filtered loadCards() without leaving
// this screen or launching SearchResultActivity.
//
// Active filters are shown in a summary bar below the search input so the user
// knows which filters are in effect. Tapping the filter button again opens the
// sheet with fields pre-populated (state is kept in the filter* fields).
public class AddCardToCollectionFragment extends Fragment {

    RecyclerView recyclerView;
    CardAdapter adapter;
    List<Card> cardList = new ArrayList<>();

    String groupId;
    SessionManager session;
    EditText searchInput;
    TextView tvFilterSummary;

    Handler searchHandler = new Handler(Looper.getMainLooper());
    Runnable searchRunnable;

    // ── Active query / filter state ───────────────────────────────────────────
    String currentQuery = "";

    // Filter params — null means "not set", populated by FilterFragment callback
    String  filterName          = null;
    String  filterText          = null;
    String  filterType          = null;
    String  filterArtist        = null;
    String  filterRarity        = null;
    Integer filterMana          = null;
    List<String> filterColors        = null;
    List<String> filterColorIdentity = null;

    // In-flight call reference for cancellation
    private Call<CardResponse> activeCall;

    // Single ApiService instance (not created per-call)
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_card, container, false);

        apiService = ApiClient.getClient().create(ApiService.class);

        // searchInput lives in the parent activity layout (activity_add_card.xml)
        searchInput     = requireActivity().findViewById(R.id.edtSearch);
        tvFilterSummary = requireActivity().findViewById(R.id.tvFilterSummary);

        recyclerView = view.findViewById(R.id.recyclerCards);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new CardAdapter(getContext(), cardList, card -> addCard(card));
        recyclerView.setAdapter(adapter);

        // ── Debounced text search ──────────────────────────────────────────────
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString().trim();
                if (searchRunnable != null)
                    searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> loadCards();
                searchHandler.postDelayed(searchRunnable, 400);
            }
        });

        // ── Filter button ──────────────────────────────────────────────────────
        // The button lives in activity_add_card.xml alongside edtSearch
        View btnFilter = requireActivity().findViewById(R.id.btnFilter);
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> openFilterSheet());
        }

        session = new SessionManager(requireContext());

        if (getArguments() != null) {
            groupId = getArguments().getString("group_id");
        }

        updateFilterSummary();
        loadCards();
        return view;
    }

    // ── Filter sheet ───────────────────────────────────────────────────────────

    private void openFilterSheet() {
        FilterFragment filter = FilterFragment.forCollection(
                (name, text, type, artist, rarity, mana, colors, colorIdentity) -> {
                    // Store returned params
                    filterName          = nullIfEmpty(name);
                    filterText          = nullIfEmpty(text);
                    filterType          = nullIfEmpty(type);
                    filterArtist        = nullIfEmpty(artist);
                    filterRarity        = rarity;
                    filterMana          = mana;
                    filterColors        = (colors        != null && !colors.isEmpty())        ? colors        : null;
                    filterColorIdentity = (colorIdentity != null && !colorIdentity.isEmpty()) ? colorIdentity : null;

                    updateFilterSummary();
                    loadCards(); // re-query with new filters immediately
                }
        );

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, filter)
                .addToBackStack(null)
                .commit();
    }

    // ── Load ───────────────────────────────────────────────────────────────────

    private void loadCards() {
        if (activeCall != null && !activeCall.isCanceled()) {
            activeCall.cancel();
        }

        activeCall = apiService.searchCards(
                currentQuery, 1, 30,
                filterName, filterText, filterType, filterArtist,
                filterRarity, filterMana,
                filterColors, filterColorIdentity
        );

        activeCall.enqueue(new Callback<CardResponse>() {
            @Override
            public void onResponse(Call<CardResponse> call, Response<CardResponse> res) {
                if (call.isCanceled() || !isAdded()) return;
                if (res.isSuccessful() && res.body() != null && res.body().items != null) {
                    cardList.clear();
                    for (CardResponse.CardItem c : res.body().items) {
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
                if (!call.isCanceled() && isAdded()) {
                    Toast.makeText(getContext(), "Failed to load cards", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // ── Add card to collection ─────────────────────────────────────────────────

    private void addCard(Card card) {
        Map<String, String> body = new HashMap<>();
        body.put("universal_id", card.getUniversalId());

        apiService.addCardToGroup(
                groupId,
                "Bearer " + session.getToken(),
                body
        ).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> r) {
                if (isAdded())
                    Toast.makeText(getContext(), "Added!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (isAdded())
                    Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Filter summary bar ─────────────────────────────────────────────────────

    private void updateFilterSummary() {
        if (tvFilterSummary == null) return;

        boolean anyActive = filterName != null || filterText != null || filterType != null
                || filterArtist != null || filterRarity != null || filterMana != null
                || filterColors != null || filterColorIdentity != null;

        if (!anyActive) {
            tvFilterSummary.setVisibility(View.GONE);
            return;
        }

        List<String> parts = new ArrayList<>();
        if (filterName   != null) parts.add("Name: " + filterName);
        if (filterText   != null) parts.add("Text: " + filterText);
        if (filterType   != null) parts.add("Type: " + filterType);
        if (filterArtist != null) parts.add("Artist: " + filterArtist);
        if (filterRarity != null) parts.add("Rarity: " + filterRarity);
        if (filterMana   != null) parts.add("Mana: " + filterMana);
        if (filterColors != null && !filterColors.isEmpty())
            parts.add("Colors: " + String.join("", filterColors));
        if (filterColorIdentity != null && !filterColorIdentity.isEmpty())
            parts.add("Color ID: " + String.join("", filterColorIdentity));

        tvFilterSummary.setText(String.join(" · ", parts));
        tvFilterSummary.setVisibility(View.VISIBLE);
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        if (activeCall != null) { activeCall.cancel(); activeCall = null; }
        recyclerView.setAdapter(null);
        recyclerView = null;
    }

    // ── Util ───────────────────────────────────────────────────────────────────

    private static String nullIfEmpty(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
