package com.example.mtg_java;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mtg_java.adapter.NewsAdapter;
import com.example.mtg_java.api.NewsApiManager;
import com.example.mtg_java.model.Group;
import com.example.mtg_java.model.News;
import com.example.mtg_java.model.NewsResponse;
import com.example.mtg_java.utils.LocalCache;
import com.example.mtg_java.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView newsRecycler;
    private TextView tvCollections;
    private TextView tvCards;
    private ImageView imgAvatar;

    private AuthManager authManager;
    private NewsApiManager newsApi;
    private GroupApiManager groupApi;

    private NewsAdapter adapter;
    private LocalCache cache;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        newsRecycler  = view.findViewById(R.id.newsRecycler);
        tvCollections = view.findViewById(R.id.tvCollections);
        tvCards       = view.findViewById(R.id.tvCards);
        imgAvatar     = view.findViewById(R.id.imgAvatar);

        TextView seeAll    = view.findViewById(R.id.tvSeeAll);
        ImageView btnSearch = view.findViewById(R.id.btnSearch);
        TextView tvUsername = view.findViewById(R.id.tvUsername);
        TextView tvEmail    = view.findViewById(R.id.tvEmail);

        seeAll.setOnClickListener(v ->
                startActivity(new Intent(getContext(), NewsListActivity.class))
        );
        btnSearch.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, new BrowseFragment())
                        .addToBackStack(null)
                        .commit()
        );

        SessionManager sessionManager = SessionManager.getInstance(getContext());
        cache = LocalCache.getInstance(requireContext());

        newsRecycler.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        adapter = new NewsAdapter(new ArrayList<>(), R.layout.item_news_home);
        newsRecycler.setAdapter(adapter);

        newsApi    = new NewsApiManager();
        groupApi   = new GroupApiManager();
        authManager = new AuthManager(requireContext());

        // ── 1. Render cached data immediately ─────────────────────────────────

        // Username / email — always available from SessionManager after first login
        tvUsername.setText("Hi, " + sessionManager.getUsername() + " 👋");
        tvEmail.setText(sessionManager.getEmail());

        // News
        List<News> cachedNews = cache.getNews();
        if (cachedNews != null) adapter.updateData(cachedNews);

        // Avatar
        String cachedAvatar = cache.getProfileImageUrl();
        if (cachedAvatar != null && imgAvatar != null) {
            Glide.with(imgAvatar).load(cachedAvatar).circleCrop().into(imgAvatar);
        }

        // Stats — show last-known counts instantly, replaced by live data below
        int[] cachedStats = cache.getStats();
        if (cachedStats != null) {
            tvCollections.setText(String.valueOf(cachedStats[0]));
            tvCards.setText(String.valueOf(cachedStats[1]));
        }

        // ── 2. Refresh from network if online ─────────────────────────────────
        if (isNetworkAvailable()) {
            loadNews();
            loadStats();
            loadAvatar();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Re-apply cache if the adapter was cleared during a fragment replacement
        if (adapter != null && adapter.getItemCount() == 0) {
            List<News> cached = cache.getNews();
            if (cached != null) adapter.updateData(cached);
            if (isNetworkAvailable()) loadNews();
        }
    }

    // ── Network loaders ───────────────────────────────────────────────────────

    private void loadNews() {
        newsApi.fetchNews(10, 0, new NewsApiManager.NewsCallback() {
            @Override
            public void onSuccess(NewsResponse response) {
                if (!isAdded()) return;
                List<News> items = response.getItems();
                cache.saveNews(items);
                requireActivity().runOnUiThread(() -> adapter.updateData(items));
            }
            @Override public void onError(String msg) {}
        });
    }

    private void loadStats() {
        SessionManager session = SessionManager.getInstance(getContext());

        groupApi.getGroups(session, new GroupApiManager.ListCallback() {
            @Override
            public void onSuccess(List<Group> result) {
                if (!isAdded()) return;

                int collectionCount = result.size();
                int totalCards = 0;
                for (Group g : result) totalCards += g.getCardCount();

                // Persist before touching UI so the values survive the next cold start
                cache.saveStats(collectionCount, totalCards);

                final int finalTotal = totalCards;
                requireActivity().runOnUiThread(() -> {
                    tvCollections.setText(String.valueOf(collectionCount));
                    tvCards.setText(String.valueOf(finalTotal));
                });
            }
            @Override public void onError(String msg) {}
        });
    }

    private void loadAvatar() {
        if (authManager == null || imgAvatar == null) return;

        authManager.getCurrentUser(new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(String t, String id, String username,
                                  String email, String profileImage) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (profileImage != null && !profileImage.isEmpty()) {
                        cache.saveProfileImageUrl(profileImage);
                        Glide.with(imgAvatar).load(profileImage).circleCrop().into(imgAvatar);
                    } else {
                        imgAvatar.setImageResource(android.R.drawable.sym_def_app_icon);
                    }
                });
            }
            @Override public void onError(String message) {}
        });
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (newsApi    != null) newsApi.cancel();
        if (groupApi   != null) groupApi.cancelGetGroups();
        if (authManager != null) authManager.cancelGetCurrentUser();

        newsRecycler.setAdapter(null);
        newsRecycler  = null;
        tvCollections = null;
        tvCards       = null;
        imgAvatar     = null;
    }

    // ── Util ──────────────────────────────────────────────────────────────────

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
}
