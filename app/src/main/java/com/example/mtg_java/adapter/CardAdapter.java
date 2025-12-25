package com.example.mtg_java.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mtg_java.R;
import com.example.mtg_java.model.Card;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private List<Card> cards;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Card card);
    }

    public CardAdapter(List<Card> cards, OnItemClickListener listener) {
        this.cards = cards;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cards.get(position);
        holder.name.setText(card.getName());
        holder.type.setText(card.getType());
        Picasso.get().load(card.getImageUrl()).placeholder(R.color.black).into(holder.image);

    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, type;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.cardImage);
            name = itemView.findViewById(R.id.cardName);
            type = itemView.findViewById(R.id.cardType);
        }
    }
}
