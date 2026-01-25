package com.example.mtg_java;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mtg_java.api.ApiClient;
import com.example.mtg_java.api.ApiService;
import com.example.mtg_java.model.Card;
import com.example.mtg_java.model.CardFace;
import com.example.mtg_java.model.Group;
import com.example.mtg_java.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CardDetailActivity extends AppCompatActivity {

    private ImageView imgCard;
    private TextView txtName, txtType, txtMana, txtText, txtStats, txtPrice;
    private ProgressBar progress;
    private ImageButton btnFlip;
    private MaterialToolbar toolbar;

    private Card card;
    private int faceIndex = 0;

    // REAL COLLECTION SYSTEM
    private SessionManager session;
    private GroupApiManager groupApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_detail);

        // ================= TOOLBAR =================
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        // ================= SESSION / API =================
        session = new SessionManager(this);
        groupApi = new GroupApiManager();

        if (!session.isLoggedIn()) {
            finish();
            return;
        }

        // ================= VIEWS =================
        imgCard = findViewById(R.id.imgCard);
        txtName = findViewById(R.id.txtName);
        txtType = findViewById(R.id.txtType);
        txtMana = findViewById(R.id.txtMana);
        txtText = findViewById(R.id.txtText);
        txtStats = findViewById(R.id.txtStats);
        txtPrice = findViewById(R.id.txtPrice);
        progress = findViewById(R.id.progress);
        btnFlip = findViewById(R.id.btnFlip);

        // ================= INTENT =================
        String cardId = getIntent().getStringExtra("CARD_ID");
        if (cardId == null) {
            finish();
            return;
        }

        loadCard(cardId);
        btnFlip.setOnClickListener(v -> flipCard());
    }

    // ================= MENU =================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_card_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            showAddToCollectionDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ================= CARD API =================
    private void loadCard(String id) {
        progress.setVisibility(View.VISIBLE);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getCardDetail(id).enqueue(new Callback<Card>() {
            @Override
            public void onResponse(Call<Card> call, Response<Card> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    card = response.body();
                    showCard();
                }
            }

            @Override
            public void onFailure(Call<Card> call, Throwable t) {
                progress.setVisibility(View.GONE);
            }
        });
    }

    private void showCard() {
        toolbar.setTitle(card.getName());

        txtName.setText(card.getName());
        txtType.setText(card.getType());
        txtMana.setText(card.getManaCost());
        txtText.setText(card.getText());

        if (card.getPower() != null && card.getToughness() != null) {
            txtStats.setText(card.getPower() + " / " + card.getToughness());
        } else {
            txtStats.setText("-");
        }

        if (card.getCurrentPrice() != null && card.getCurrentPrice().getUsd() != null) {
            txtPrice.setText("$" + card.getCurrentPrice().getUsd());
        } else {
            txtPrice.setText("-");
        }

        if (card.getImageUrl() != null) {
            Glide.with(this).load(card.getImageUrl()).into(imgCard);
        }

        btnFlip.setVisibility(
                card.getFaces() != null && card.getFaces().size() == 2
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    // ================= FLIP =================
    private void flipCard() {
        if (card.getFaces() == null || card.getFaces().size() != 2) return;

        faceIndex = faceIndex == 0 ? 1 : 0;
        CardFace face = card.getFaces().get(faceIndex);

        txtName.setText(face.getName());
        txtType.setText(face.getType());
        txtMana.setText(face.getManaCost());
        txtText.setText(face.getText());

        if (face.getPower() != null && face.getToughness() != null) {
            txtStats.setText(face.getPower() + " / " + face.getToughness());
        } else {
            txtStats.setText("-");
        }

        if (face.getImageUrl() != null) {
            Glide.with(this).load(face.getImageUrl()).into(imgCard);
        }
    }

    // ================= COLLECTION (REAL) =================
    private void showAddToCollectionDialog() {
        AlertDialog loading = new AlertDialog.Builder(this)
                .setTitle("Add to Collection")
                .setMessage("Loading...")
                .setCancelable(false)
                .create();

        loading.show();

        groupApi.getGroups(session, new GroupApiManager.ListCallback() {
            @Override
            public void onSuccess(List<Group> groups) {
                loading.dismiss();

                if (groups.isEmpty()) {
                    showCreateCollectionDialog();
                    return;
                }

                String[] items = new String[groups.size() + 1];
                for (int i = 0; i < groups.size(); i++) {
                    items[i] = groups.get(i).getName();
                }
                items[groups.size()] = "➕ Create new collection";

                new AlertDialog.Builder(CardDetailActivity.this)
                        .setTitle("Select Collection")
                        .setItems(items, (d, which) -> {
                            if (which == groups.size()) {
                                showCreateCollectionDialog();
                            } else {
                                addCardToGroup(groups.get(which));
                            }
                        })
                        .show();
            }

            @Override
            public void onError(String msg) {
                loading.dismiss();
                Toast.makeText(CardDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateCollectionDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        new AlertDialog.Builder(this)
                .setTitle("New Collection")
                .setView(input)
                .setPositiveButton("Create", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        createGroupAndAddCard(name);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createGroupAndAddCard(String name) {
        groupApi.createGroup(session, name, new GroupApiManager.ObjectCallback() {
            @Override
            public void onSuccess(Group g) {
                Toast.makeText(CardDetailActivity.this, "Collection created", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(CardDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addCardToGroup(Group group) {
        groupApi.addCard(
                session,
                group.getId(),
                card.getUniversalId(),
                new GroupApiManager.SimpleCallback() {
                    @Override
                    public void onDone() {
                        Toast.makeText(
                                CardDetailActivity.this,
                                "Added to " + group.getName(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onError(String msg) {
                        Toast.makeText(CardDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }


}
