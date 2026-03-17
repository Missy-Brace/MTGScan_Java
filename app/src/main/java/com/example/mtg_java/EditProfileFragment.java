package com.example.mtg_java;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.mtg_java.utils.LocalCache;
import com.example.mtg_java.utils.SessionManager;

public class EditProfileFragment extends Fragment {

    private ImageView imgProfile;
    private EditText edtUsername, edtEmail;
    private Button btnUpload, btnSave;
    private AuthManager authManager;
    private LocalCache cache;
    private SessionManager session;

    private Uri selectedImageUri;
    private String currentProfileImageUrl = "";

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public EditProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        authManager = new AuthManager(requireContext());
        cache       = LocalCache.getInstance(requireContext());
        session     = SessionManager.getInstance(requireContext());

        edtUsername = view.findViewById(R.id.edtUsername);
        edtEmail    = view.findViewById(R.id.edtEmail);
        imgProfile  = view.findViewById(R.id.imgProfile);
        btnUpload   = view.findViewById(R.id.btnUpload);
        btnSave     = view.findViewById(R.id.btnSave);

        // ── 1. Show cached data immediately ──────────────────────
        edtUsername.setText(session.getUsername());
        edtEmail.setText(session.getEmail());

        String cachedAvatar = cache.getProfileImageUrl();
        if (cachedAvatar != null && !cachedAvatar.isEmpty()) {
            currentProfileImageUrl = cachedAvatar;
            Glide.with(requireContext()).load(cachedAvatar).into(imgProfile);
        }

        // ── 2. Connectivity gate ──────────────────────────────────
        boolean online = isNetworkAvailable();
        setEditingEnabled(online);

        if (online) {
            // Refresh fields from server — may differ if changed on another device
            authManager.getCurrentUser(new AuthManager.AuthCallback() {
                @Override
                public void onSuccess(String t, String id, String username,
                                      String email, String profileImage) {
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> {
                        edtUsername.setText(username);
                        edtEmail.setText(email);
                        if (profileImage != null && !profileImage.isEmpty()) {
                            currentProfileImageUrl = profileImage;
                            Glide.with(requireContext()).load(profileImage).into(imgProfile);
                        }
                    });
                }

                @Override
                public void onError(String message) {
                    // Suppress — cached values are already shown, editing is
                    // still allowed because we confirmed online above.
                }
            });
        }

        // ── Image picker ──────────────────────────────────────────
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK
                            && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        Glide.with(requireContext()).load(selectedImageUri).into(imgProfile);
                    }
                }
        );

        btnUpload.setOnClickListener(v -> openGallery());

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        btnSave.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String email    = edtEmail.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty()) {
                Toast.makeText(getContext(), "All fields required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedImageUri != null) {
                authManager.uploadAvatar(selectedImageUri, new AuthManager.ImageUploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            currentProfileImageUrl = imageUrl;
                            cache.saveProfileImageUrl(imageUrl); // update cache on upload
                            Glide.with(requireContext()).load(imageUrl).into(imgProfile);
                            updateProfileNow(username, email, currentProfileImageUrl);
                        });
                    }

                    @Override
                    public void onError(String message) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show()
                        );
                    }
                });
            } else {
                updateProfileNow(username, email, currentProfileImageUrl);
            }
        });

        return view;
    }

    private void updateProfileNow(String username, String email, String imageUrl) {
        authManager.updateProfile(username, email, imageUrl, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(String t, String id, String u, String e, String profileImage) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    // Persist updated values to both caches
                    session.saveUser(u, e);
                    if (profileImage != null && !profileImage.isEmpty()) {
                        cache.saveProfileImageUrl(profileImage);
                    }
                    Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                });
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void setEditingEnabled(boolean enabled) {
        edtUsername.setEnabled(enabled);
        edtEmail.setEnabled(enabled);
        btnUpload.setEnabled(enabled);
        btnSave.setEnabled(enabled);

        float alpha = enabled ? 1.0f : 0.5f;
        btnUpload.setAlpha(alpha);
        btnSave.setAlpha(alpha);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }
}
