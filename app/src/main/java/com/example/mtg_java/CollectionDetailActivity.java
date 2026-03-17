package com.example.mtg_java;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mtg_java.adapter.CardAdapter;
import com.example.mtg_java.api.ApiClient;
import com.example.mtg_java.api.ApiService;
import com.example.mtg_java.model.Card;
import com.example.mtg_java.utils.LocalCache;
import com.example.mtg_java.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// FIX: Added dirty-flag pattern (needsRefresh) so onResume() only reloads the card
// list when the user has added or removed a card. The original called loadGroupCards()
// unconditionally on every resume — including app-switching and screen unlock.
public class CollectionDetailActivity extends AppCompatActivity {

    String groupId;
    String groupName;
    SessionManager session;

    RecyclerView recyclerView;
    CardAdapter adapter;
    List<Card> cardList = new ArrayList<>();

    TextView txtTitle;
    GroupApiManager api;

    // FIX: dirty flag — prevents redundant reloads on incidental resumes
    private boolean needsRefresh = true;
    private LocalCache cache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_detail);

        groupId   = getIntent().getStringExtra("group_id");
        session   = new SessionManager(this);
        api       = new GroupApiManager();
        cache = LocalCache.getInstance(this);
        groupName = getIntent().getStringExtra("group_name");

        txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText(groupName);

        recyclerView = findViewById(R.id.recyclerCards);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new CardAdapter(this, cardList, groupId);
        recyclerView.setAdapter(adapter);

        LocalCache cache = LocalCache.getInstance(this);
        List<Card> cached = cache.getCards(groupId);
        if (cached != null) {
            cardList.addAll(cached);
            adapter.notifyDataSetChanged();
            setActionsEnabled(false);  // viewing only until network confirms
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnAdd).setOnClickListener(v -> {
            // FIX: mark dirty so the list reloads when we return from AddCard
            needsRefresh = true;
            startActivity(new Intent(this, AddCardToCollectionActivity.class)
                    .putExtra("group_id", groupId));
        });

        findViewById(R.id.btnMore).setOnClickListener(v -> showCollectionOptions());

        loadGroupCards();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // FIX: only reload if a mutation may have happened
        if (needsRefresh) {
            loadGroupCards();
        }
    }

    private void loadGroupCards() {
        needsRefresh = false; // FIX: clear before the call
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getCardsInGroup(groupId, "Bearer " + session.getToken())
                .enqueue(new Callback<List<Card>>() {
                    @Override
                    public void onResponse(Call<List<Card>> call, Response<List<Card>> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            cardList.clear();
                            cardList.addAll(response.body());
                            cache.saveCards(groupId, response.body());
                            setActionsEnabled(true);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Card>> call, Throwable t) {
                        if (!isFinishing()) {
                            needsRefresh = true; // FIX: allow retry next resume on failure
                            Toast.makeText(CollectionDetailActivity.this,
                                    "Failed to load collection", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showCollectionOptions() {
        String[] options = {"Rename", "Delete"};
        new AlertDialog.Builder(this)
                .setTitle("Collection Options")
                .setItems(options, (d, which) -> {
                    if (which == 0) showRenameDialog();
                    else showDeleteDialog();
                })
                .show();
    }

    private void showRenameDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(txtTitle.getText().toString());

        new AlertDialog.Builder(this)
                .setTitle("Rename Collection")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) renameCollection(name);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void renameCollection(String newName) {
        api.renameGroup(session, groupId, newName, new GroupApiManager.SimpleCallback() {
            @Override
            public void onDone() {
                if (!isFinishing()) txtTitle.setText(newName);
            }

            @Override
            public void onError(String msg) {
                if (!isFinishing())
                    Toast.makeText(CollectionDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete this collection?")
                .setMessage("This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> deleteCollection())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCollection() {
        api.deleteGroup(session, groupId, new GroupApiManager.SimpleCallback() {
            @Override
            public void onDone() { finish(); }

            @Override
            public void onError(String msg) {
                if (!isFinishing())
                    Toast.makeText(CollectionDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setActionsEnabled(boolean enabled) {
        findViewById(R.id.btnAdd).setEnabled(enabled);
        findViewById(R.id.btnMore).setEnabled(enabled);
    }
}
