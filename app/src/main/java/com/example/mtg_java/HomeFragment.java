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

import com.example.mtg_java.GroupApiManager;
import com.example.mtg_java.NewsListActivity;
import com.example.mtg_java.R;
import com.example.mtg_java.adapter.NewsAdapter;
import com.example.mtg_java.api.NewsApiManager;
import com.example.mtg_java.model.Group;
import com.example.mtg_java.model.NewsResponse;
import com.example.mtg_java.utils.SessionManager;

import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView newsRecycler;
    private TextView tvCollections;
    private TextView tvCards;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        TextView seeAll = view.findViewById(R.id.tvSeeAll);

        seeAll.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), NewsListActivity.class));
        });
        ImageView btnSearch = view.findViewById(R.id.btnSearch);

        btnSearch.setOnClickListener(v -> {

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, new BrowseFragment())
                    .addToBackStack(null)
                    .commit();

        });

        newsRecycler = view.findViewById(R.id.newsRecycler);
        tvCollections = view.findViewById(R.id.tvCollections);
        tvCards = view.findViewById(R.id.tvCards);
        SessionManager sessionManager = new SessionManager(getContext());

        TextView tvUsername = view.findViewById(R.id.tvUsername);
        TextView tvEmail = view.findViewById(R.id.tvEmail);

        tvUsername.setText("Hi, " + sessionManager.getUsername() + " 👋");
        tvEmail.setText(sessionManager.getEmail());

        newsRecycler.setLayoutManager(
                new LinearLayoutManager(
                        getContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        loadNews();
        loadStats();

        return view;
    }
    private void loadNews() {

        NewsApiManager api = new NewsApiManager();

        api.fetchNews(10, 0, new NewsApiManager.NewsCallback() {
            @Override
            public void onSuccess(NewsResponse response) {

                NewsAdapter adapter =
                        new NewsAdapter(response.getItems(), R.layout.item_news_home);

                newsRecycler.setAdapter(adapter);
            }

            @Override
            public void onError(String msg) {
            }
        });
    }
    private void loadStats() {

        GroupApiManager api = new GroupApiManager();
        SessionManager session = new SessionManager(getContext());

        api.getGroups(session, new GroupApiManager.ListCallback() {
            @Override
            public void onSuccess(List<Group> result) {

                int collectionCount = result.size();

                int totalCards = 0;
                for (Group g : result) {
                    totalCards += g.getCardCount(); // uses your model
                }

                tvCollections.setText(String.valueOf(collectionCount));
                tvCards.setText(String.valueOf(totalCards));
            }

            @Override
            public void onError(String msg) {
            }
        });
    }
}
