package com.example.mtg_java.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mtg_java.R;
import com.example.mtg_java.model.News;
import com.example.mtg_java.utils.TimeUtils;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsVH> {

    private List<News> newsList;
    private int layoutId;   // 🔥 layout variable

    public NewsAdapter(List<News> newsList, @LayoutRes int layoutId) {
        this.newsList = newsList;
        this.layoutId = layoutId;
    }

    @NonNull
    @Override
    public NewsVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutId, parent, false);

        return new NewsVH(view);
    }

    @Override

    public void onBindViewHolder(@NonNull NewsVH holder, int position) {

        News news = newsList.get(position);
        holder.itemView.setOnClickListener(v -> {
            String url = news.getLink();

            if (url != null && !url.isEmpty()) {
                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        android.net.Uri.parse(url)
                );
                v.getContext().startActivity(intent);
            }
        });

        if (holder.tvTitle != null) {
            holder.tvTitle.setText(news.getTitle());
        }

        String source = news.getSource();
        String date = news.getIsoDate();
        String timeAgo = TimeUtils.getTimeAgo(date);

        if (holder.tvSourceDate != null) {
            holder.tvSourceDate.setText(source + " • " + timeAgo);
        }

        if (holder.imgNews != null) {
            Glide.with(holder.itemView.getContext())
                    .load(news.getImage())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.imgNews);
        }
    }

    @Override
    public int getItemCount() {
        return newsList != null ? newsList.size() : 0;
    }

    static class NewsVH extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvSourceDate;
        ImageView imgNews;

        public NewsVH(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSourceDate = itemView.findViewById(R.id.tvSourceDate);
            imgNews = itemView.findViewById(R.id.imgNews);
        }
    }
}