package com.example.mtg_java;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mtg_java.api.ApiService;
import com.example.mtg_java.model.Card;
import com.example.mtg_java.model.CardFace;
import com.example.mtg_java.api.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CardDetailActivity extends AppCompatActivity {

    ImageView imgCard;
    TextView txtName, txtType, txtMana, txtText, txtStats, txtPrice;
    ProgressBar progress;
    ImageButton btnFlip;

    Card card;
    int faceIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_detail);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(""); // Optional: remove title
        }

        imgCard = findViewById(R.id.imgCard);
        txtName = findViewById(R.id.txtName);
        txtType = findViewById(R.id.txtType);
        txtMana = findViewById(R.id.txtMana);
        txtText = findViewById(R.id.txtText);
        txtStats = findViewById(R.id.txtStats);
        txtPrice = findViewById(R.id.txtPrice);
        progress = findViewById(R.id.progress);
        btnFlip = findViewById(R.id.btnFlip);

        String cardId = getIntent().getStringExtra("CARD_ID");

        if (cardId == null) {
            finish(); // close activity safely if no ID
            return;
        }

        loadCard(cardId);

        btnFlip.setOnClickListener(v -> flipCard());
    }

    private void loadCard(String id) {
        progress.setVisibility(View.VISIBLE);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getCardDetail(id).enqueue(new Callback<Card>() {
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
                t.printStackTrace();
            }
        });
    }

    private void showCard() {
        // Safe null checks for every field
        txtName.setText(card.getName() != null ? card.getName() : "-");
        txtType.setText(card.getType() != null ? card.getType() : "-");
        txtMana.setText(card.getManaCost() != null ? card.getManaCost() : "-");
        txtText.setText(card.getText() != null ? card.getText() : "-");

        String stats = "";
        if (card.getPower() != null && card.getToughness() != null) {
            stats = card.getPower() + " / " + card.getToughness();
        }
        txtStats.setText(stats.isEmpty() ? "-" : stats);

        if (card.getCurrentPrice() != null && card.getCurrentPrice().getUsd() != null) {
            txtPrice.setText("$" + card.getCurrentPrice().getUsd());
        } else {
            txtPrice.setText("-");
        }

        if (card.getImageUrl() != null && !card.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(card.getImageUrl())
                    .into(imgCard);
        }

        btnFlip.setVisibility(card.getFaces() != null && card.getFaces().size() == 2 ? View.VISIBLE : View.GONE);
    }

    private void flipCard() {
        if (card.getFaces() == null || card.getFaces().size() != 2) return;

        faceIndex = faceIndex == 0 ? 1 : 0;
        CardFace face = card.getFaces().get(faceIndex);

        txtName.setText(face.getName() != null ? face.getName() : "-");
        txtType.setText(face.getType() != null ? face.getType() : "-");
        txtMana.setText(face.getManaCost() != null ? face.getManaCost() : "-");
        txtText.setText(face.getText() != null ? face.getText() : "-");

        String stats = "";
        if (face.getPower() != null && face.getToughness() != null) {
            stats = face.getPower() + " / " + face.getToughness();
        }
        txtStats.setText(stats.isEmpty() ? "-" : stats);

        if (face.getImageUrl() != null && !face.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(face.getImageUrl())
                    .into(imgCard);
        }
    }
}
