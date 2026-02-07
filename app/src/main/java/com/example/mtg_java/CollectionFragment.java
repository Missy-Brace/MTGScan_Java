package com.example.mtg_java;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mtg_java.model.Group;
import com.example.mtg_java.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class CollectionFragment extends Fragment {

    private RecyclerView recycler;
    private TextView txtEmpty;
    private GroupAdapter adapter;
    private final List<Group> groups = new ArrayList<>();

    private SessionManager session;
    private GroupApiManager api;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        session = new SessionManager(requireContext());
        api = new GroupApiManager();

        // 🔐 Not logged in → go to login
        if (!session.isLoggedIn()) {
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
            return null;
        }

        View view = inflater.inflate(R.layout.fragment_collection, container, false);

        recycler = view.findViewById(R.id.recyclerGroups);
        txtEmpty = view.findViewById(R.id.txtEmpty);

        adapter = new GroupAdapter(groups, new GroupAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Group group) {
                Intent i = new Intent(requireContext(), CollectionDetailActivity.class);
                i.putExtra("group_id", group.getId());
                i.putExtra("group_name", group.getName());
                startActivity(i);
            }

            @Override
            public void onMoreClick(Group group, View anchor) {
                showGroupOptions(group);
            }
        });

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);

        view.findViewById(R.id.btnAdd).setOnClickListener(v -> showCreateDialog());

        loadGroups();
        return view;
    }

    // ===================== API =====================

    private void loadGroups() {
        api.getGroups(session, new GroupApiManager.ListCallback() {
            @Override
            public void onSuccess(List<Group> result) {
                groups.clear();
                groups.addAll(result);
                updateUI();
            }

            @Override
            public void onError(String msg) {
                txtEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createGroup(String name) {
        api.createGroup(session, name, new GroupApiManager.ObjectCallback() {
            @Override
            public void onSuccess(Group g) {
                loadGroups();   // refresh list
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void renameGroup(Group group, String newName) {
        api.renameGroup(session, group.getId(), newName, new GroupApiManager.SimpleCallback() {
            @Override
            public void onDone() {
                loadGroups();
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteGroup(Group group) {
        api.deleteGroup(session, group.getId(), new GroupApiManager.SimpleCallback() {
            @Override
            public void onDone() {
                loadGroups();
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ===================== UI =====================

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
                .setPositiveButton("Create", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        createGroup(name);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showGroupOptions(Group group) {
        String[] options = {"Rename", "Delete"};

        new AlertDialog.Builder(getContext())
                .setTitle(group.getName())
                .setItems(options, (d, which) -> {
                    if (which == 0) showRenameDialog(group);
                    else deleteGroup(group);
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
                .setPositiveButton("Save", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        renameGroup(group, name);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    @Override
    public void onResume() {
        super.onResume();
        loadGroups();   // 🔥 refresh when coming back
    }
}
