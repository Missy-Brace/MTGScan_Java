package com.example.mtg_java.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mtg_java.CardDetailActivity;
import com.example.mtg_java.R;
import com.example.mtg_java.model.Card;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private Context context;
    private List<Card> cardList;

    public CardAdapter(Context context, List<Card> cardList) {
        this.context = context;
        this.cardList = cardList;
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

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CardDetailActivity.class);
            intent.putExtra("CARD_ID", card.getUniversalId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    public class CardViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        ImageView imgCard;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            imgCard = itemView.findViewById(R.id.imgCard);
        }
    }
}
