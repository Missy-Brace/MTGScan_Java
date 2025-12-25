package com.example.mtg_java;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mtg_java.model.Group;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<Group> groups;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Group group);
        void onMoreClick(Group group, View view);
    }

    public GroupAdapter(List<Group> groups, OnItemClickListener listener) {
        this.groups = groups;
        this.listener = listener;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groups.get(position);
        holder.txtName.setText(group.getName());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(group));
        holder.btnMore.setOnClickListener(v -> listener.onMoreClick(group, v));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        ImageButton btnMore;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtGroupName);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
