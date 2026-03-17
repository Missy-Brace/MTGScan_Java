package com.example.mtg_java;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mtg_java.utils.SessionManager;

public class SettingsFragment extends Fragment {

    private AuthManager authManager;
    private SessionManager session;

    private TextView txtUsername, txtEmail;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        authManager = new AuthManager(requireContext());
        session = SessionManager.getInstance(requireContext());

        txtUsername = view.findViewById(R.id.txtUsername);
        txtEmail = view.findViewById(R.id.txtEmail);

        loadUserInfo();

        // PROFILE CLICK
        view.findViewById(R.id.rowProfile).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, new EditProfileFragment())
                        .addToBackStack(null)
                        .commit()
        );

        // CHANGE PASSWORD
        view.findViewById(R.id.rowChangePassword).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, new ChangePasswordFragment())
                        .addToBackStack(null)
                        .commit()
        );

        // LOGOUT
        view.findViewById(R.id.rowLogout).setOnClickListener(v -> logout());

        // DELETE ACCOUNT
        view.findViewById(R.id.rowDeleteAccount).setOnClickListener(v -> confirmDelete());

        return view;
    }

    // 🔹 Load profile from server
    private void loadUserInfo() {
        authManager.getCurrentUser(new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(String token, String userId, String username, String email, String finalProfileImage) {
                if (!isAdded()) return;
                txtUsername.setText(username);
                txtEmail.setText(email);
                session.saveUser(username, email);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 Logout
    private void logout() {
        SessionManager session = SessionManager.getInstance(getContext());
        session.clearSession();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // 🔹 Confirm Delete
    private void confirmDelete() {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Account")
                .setMessage("This action is irreversible. Are you sure?")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        authManager.deleteAccount(new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(String token, String userId, String username, String email, String finalProfileImage) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Account deleted", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}