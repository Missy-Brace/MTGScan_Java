package com.example.mtg_java;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mtg_java.model.Group;

import java.util.ArrayList;
import java.util.List;

public class CollectionFragment extends Fragment {

    private RecyclerView recycler;
    private TextView txtEmpty;
    private GroupAdapter adapter;
    private List<Group> groups = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collection, container, false);

        recycler = view.findViewById(R.id.recyclerGroups);
        txtEmpty = view.findViewById(R.id.txtEmpty);

        adapter = new GroupAdapter(groups, new GroupAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Group group) {
                // what happens when a group is clicked
            }

            @Override
            public void onMoreClick(Group group, View view) {
                // what happens when the "more" button is clicked
            }
        });


        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);

        view.findViewById(R.id.btnAdd).setOnClickListener(v -> showCreateDialog());

        loadGroups();

        return view;
    }

    private void loadGroups() {
        // TODO: load from storage or API
        // For demo, empty list
        updateUI();
    }

    private void updateUI() {
        adapter.setGroups(groups);
        txtEmpty.setVisibility(groups.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showCreateDialog() {
        EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        new AlertDialog.Builder(getContext())
                .setTitle("New Collection")
                .setView(input)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        Group g = new Group(name);
                        groups.add(g);
                        updateUI();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showGroupOptions(Group group) {
        String[] options = {"Rename", "Delete"};
        new AlertDialog.Builder(getContext())
                .setTitle(group.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showRenameDialog(group);
                    else if (which == 1) {
                        groups.remove(group);
                        updateUI();
                    }
                })
                .show();
    }

    private void showRenameDialog(Group group) {
        EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(group.getName());
        new AlertDialog.Builder(getContext())
                .setTitle("Rename Collection")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        group.setName(name);
                        updateUI();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
