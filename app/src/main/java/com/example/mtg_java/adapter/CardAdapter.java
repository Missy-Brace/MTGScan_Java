package com.example.mtg_java.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mtg_java.CardDetailActivity;
import com.example.mtg_java.R;
import com.example.mtg_java.api.ApiClient;
import com.example.mtg_java.api.ApiService;
import com.example.mtg_java.model.Card;
import com.example.mtg_java.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private Context context;
    private List<Card> cardList;
    boolean isCollectionMode = false;

    // 🔹 ADDED
    private OnCardClickListener listener;

    // 🔹 ADDED

    private String groupId;
    private SessionManager session;

    // 🔹 ADDED interface
    public interface OnCardClickListener {
        void onCardClick(Card card);
    }

    // 🔹 EXISTING constructor (unchanged)
    public CardAdapter(Context context, List<Card> cardList) {
        this.context = context;
        this.cardList = cardList;
    }

    // 🔹 ADDED new constructor (do NOT remove old one)
    public CardAdapter(Context context, List<Card> cardList, OnCardClickListener listener) {
        this.context = context;
        this.cardList = cardList;
        this.listener = listener;
    }

    // 🔹 ADDED new constructor for collection mode
    public CardAdapter(Context context, List<Card> cardList, String groupId) {
        this.context = context;
        this.cardList = cardList;
        this.groupId = groupId;
        this.isCollectionMode = true;
        this.session = new SessionManager(context);
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cardList.get(position);
        holder.txtName.setText(card.getName() != null ? card.getName() : "-");

        if (card.getImageUrl() != null && !card.getImageUrl().isEmpty()) {
            Glide.with(context).load(card.getImageUrl()).into(holder.imgCard);
        }

        // 🔹 KEEP original behavior
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCardClick(card);
            } else {
                Intent intent = new Intent(context, CardDetailActivity.class);
                intent.putExtra("CARD_ID", card.getUniversalId());
                context.startActivity(intent);
            }
        });

        // 🔹 ADDED: show ✕ only in collection
        if (isCollectionMode) {
            holder.btnRemove.setVisibility(View.VISIBLE);
            holder.btnRemove.setOnClickListener(v -> {
                showRemoveDialog(card, position);
            });
        } else {
            holder.btnRemove.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    // 🔹 ADDED dialog
    private void showRemoveDialog(Card card, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Remove card?")
                .setMessage(card.getName())
                .setPositiveButton("Remove", (d, w) -> {
                    removeCardFromGroup(card, position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // 🔹 ADDED API call
    private void removeCardFromGroup(Card card, int position) {
        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.removeCardFromGroup(
                groupId,
                card.getUniversalId(),
                "Bearer " + session.getToken()
        ).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    cardList.remove(position);
                    notifyItemRemoved(position);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Failed to remove", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class CardViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        ImageView imgCard;

        // 🔹 ADDED
        ImageButton btnRemove;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            imgCard = itemView.findViewById(R.id.imgCard);

            // 🔹 ADDED
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
