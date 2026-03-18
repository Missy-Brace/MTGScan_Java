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
import com.example.mtg_java.model.CardResponse;

import java.util.List;

public class CardItemAdapter extends RecyclerView.Adapter<CardItemAdapter.ViewHolder> {

    private final Context context;
    private final List<CardResponse.CardItem> list;

    public CardItemAdapter(Context context, List<CardResponse.CardItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int i) {
        CardResponse.CardItem c = list.get(i);
        h.txtName.setText(c.name != null ? c.name : "-");


        if (c.imageUrl != null && !c.imageUrl.isEmpty()) {
            Glide.with(context).load(c.imageUrl).into(h.imgCard);
        }

        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CardDetailActivity.class);
            intent.putExtra("CARD_ID", c.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        ImageView imgCard;

        ViewHolder(View v) {
            super(v);
            txtName = v.findViewById(R.id.txtName);
            imgCard = v.findViewById(R.id.imgCard);
        }
    }
}
