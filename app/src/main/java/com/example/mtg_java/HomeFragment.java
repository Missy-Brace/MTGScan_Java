package com.example.mtg_java;

import android.content.Intent;
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
import com.example.mtg_java.model.NewsResponse;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        newsRecycler = view.findViewById(R.id.newsRecycler);
        tvCollections = view.findViewById(R.id.tvCollections);
        tvCards = view.findViewById(R.id.tvCards);
        imgAvatar = view.findViewById(R.id.imgAvatar);

        TextView seeAll = view.findViewById(R.id.tvSeeAll);
        ImageView btnSearch = view.findViewById(R.id.btnSearch);
        TextView tvUsername = view.findViewById(R.id.tvUsername);
        TextView tvEmail = view.findViewById(R.id.tvEmail);

        seeAll.setOnClickListener(v ->
                startActivity(new Intent(getContext(), NewsListActivity.class))
        );

        btnSearch.setOnClickListener(v ->
                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, new BrowseFragment())
                        .addToBackStack(null)
                        .commit()
        );

        SessionManager sessionManager = new SessionManager(getContext());
        tvUsername.setText("Hi, " + sessionManager.getUsername() + " 👋");
        tvEmail.setText(sessionManager.getEmail());

        newsRecycler.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        adapter = new NewsAdapter(new ArrayList<>(), R.layout.item_news_home);
        newsRecycler.setAdapter(adapter);

        newsApi = new NewsApiManager();
        groupApi = new GroupApiManager();
        authManager = new AuthManager(requireContext());

        loadNews();
        loadStats();
        loadAvatar();

        return view;
    }

    private void loadNews() {
        newsApi.fetchNews(10, 0, new NewsApiManager.NewsCallback() {
            @Override
            public void onSuccess(NewsResponse response) {
                if (!isAdded()) return;

                requireActivity().runOnUiThread(() -> {
                    adapter.updateData(response.getItems());
                });
            }

            @Override
            public void onError(String msg) {
                // optional: log
            }
        });
    }

    private void loadStats() {
        SessionManager session = new SessionManager(getContext());

        groupApi.getGroups(session, new GroupApiManager.ListCallback() {
            @Override
            public void onSuccess(List<Group> result) {
                if (!isAdded()) return;

                requireActivity().runOnUiThread(() -> {
                    int collectionCount = result.size();

                    int totalCards = 0;
                    for (Group g : result) {
                        totalCards += g.getCardCount();
                    }

                    tvCollections.setText(String.valueOf(collectionCount));
                    tvCards.setText(String.valueOf(totalCards));
                });
            }

            @Override
            public void onError(String msg) {
            }
        });
    }

    private void loadAvatar() {
        if (authManager == null || imgAvatar == null) return;

        authManager.getCurrentUser(new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(String t, String id, String username, String email, String profileImage) {
                if (!isAdded()) return;

                requireActivity().runOnUiThread(() -> {
                    if (profileImage != null && !profileImage.isEmpty()) {
                        Glide.with(imgAvatar)
                                .load(profileImage)
                                .circleCrop()
                                .into(imgAvatar);
                    } else {
                        imgAvatar.setImageResource(android.R.drawable.sym_def_app_icon);
                    }
                });
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        //if (newsApi != null) newsApi.cancel(); // implement later
        //if (groupApi != null) groupApi.cancel(); // optional

        newsRecycler.setAdapter(null);
    }
}