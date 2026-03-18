package com.example.mtg_java;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mtg_java.utils.LocalCache;
import com.example.mtg_java.utils.SessionManager;

public class SettingsFragment extends Fragment {

    private AuthManager authManager;
    private SessionManager session;
    private LocalCache cache;

    private TextView txtUsername, txtEmail;


    private View rowProfile, rowChangePassword, rowDeleteAccount;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        authManager = new AuthManager(requireContext());
        session     = SessionManager.getInstance(requireContext());
        cache       = LocalCache.getInstance(requireContext());

        txtUsername      = view.findViewById(R.id.txtUsername);
        txtEmail         = view.findViewById(R.id.txtEmail);
        rowProfile       = view.findViewById(R.id.rowProfile);
        rowChangePassword= view.findViewById(R.id.rowChangePassword);
        rowDeleteAccount = view.findViewById(R.id.rowDeleteAccount);


        txtUsername.setText(session.getUsername());
        txtEmail.setText(session.getEmail());


        boolean online = isNetworkAvailable();
        setEditingEnabled(online);

        if (online) {

            loadUserInfo();
        }

        rowProfile.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, new EditProfileFragment())
                        .addToBackStack(null)
                        .commit()
        );


        rowChangePassword.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, new ChangePasswordFragment())
                        .addToBackStack(null)
                        .commit()
        );


        view.findViewById(R.id.rowLogout).setOnClickListener(v -> logout());


        rowDeleteAccount.setOnClickListener(v -> confirmDelete());

        return view;
    }



    private void loadUserInfo() {
        authManager.getCurrentUser(new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(String token, String userId, String username,
                                  String email, String finalProfileImage) {
                if (!isAdded()) return;

                txtUsername.setText(username);
                txtEmail.setText(email);

                session.saveUser(username, email);
                if (finalProfileImage != null && !finalProfileImage.isEmpty()) {
                    cache.saveProfileImageUrl(finalProfileImage);
                }
            }

            @Override
            public void onError(String message) {

            }
        });
    }



    /**
     * Enable or disable the three rows that require a live server connection.
     * Logout is intentionally excluded — it only clears local state.
     * A 0.4 alpha gives a visible-but-clearly-inactive appearance without
     * hiding the rows entirely, so the user knows the features exist.
     */
    private void setEditingEnabled(boolean enabled) {
        float alpha = enabled ? 1.0f : 0.4f;

        rowProfile.setEnabled(enabled);
        rowProfile.setAlpha(alpha);

        rowChangePassword.setEnabled(enabled);
        rowChangePassword.setAlpha(alpha);

        rowDeleteAccount.setEnabled(enabled);
        rowDeleteAccount.setAlpha(alpha);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }



    private void logout() {
        session.clearSession();
        cache.clear();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

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
            public void onSuccess(String token, String userId, String username,
                                  String email, String finalProfileImage) {
                if (!isAdded()) return;
                cache.clear();
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
