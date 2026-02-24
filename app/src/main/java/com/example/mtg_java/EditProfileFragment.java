package com.example.mtg_java;

import android.app.Activity;
import android.content.Intent;
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

public class EditProfileFragment extends Fragment {

    private ImageView imgProfile;
    private EditText edtUsername, edtEmail;
    private AuthManager authManager;
    private Uri selectedImageUri;
    private String currentProfileImageUrl = "";   // ✅ store existing image
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public EditProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        authManager = new AuthManager(requireContext());

        edtUsername = view.findViewById(R.id.edtUsername);
        edtEmail = view.findViewById(R.id.edtEmail);
        imgProfile = view.findViewById(R.id.imgProfile);
        Button btnUpload = view.findViewById(R.id.btnUpload);
        Button btnSave = view.findViewById(R.id.btnSave);

        // ✅ GET USER ONCE (username, email, image)
        authManager.getCurrentUser(new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(String t, String id, String username, String email, String profileImage) {

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {

                        edtUsername.setText(username);
                        edtEmail.setText(email);

                        // ✅ Save existing image
                        if (profileImage != null && !profileImage.isEmpty()) {
                            currentProfileImageUrl = profileImage;

                            Glide.with(requireContext())
                                    .load(profileImage)
                                    .into(imgProfile);
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // ✅ Image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK &&
                            result.getData() != null) {

                        selectedImageUri = result.getData().getData();
                        Glide.with(requireContext())
                                .load(selectedImageUri)
                                .into(imgProfile);
                    }
                }
        );

        btnUpload.setOnClickListener(v -> openGallery());

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        btnSave.setOnClickListener(v -> {

            String username = edtUsername.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty()) {
                Toast.makeText(getContext(), "All fields required", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ If new image selected → upload first
            if (selectedImageUri != null) {

                authManager.uploadAvatar(selectedImageUri, new AuthManager.ImageUploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {

                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {

                                currentProfileImageUrl = imageUrl;

                                Glide.with(requireContext())
                                        .load(imageUrl)
                                        .into(imgProfile);

                                updateProfileNow(username, email, currentProfileImageUrl);
                            });
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                });

            } else {
                // ✅ No new image → keep old one
                updateProfileNow(username, email, currentProfileImageUrl);
            }
        });

        return view;
    }

    private void updateProfileNow(String username, String email, String imageUrl) {

        authManager.updateProfile(username, email, imageUrl, new AuthManager.AuthCallback() {

            @Override
            public void onSuccess(String t, String id, String u, String e, String profileImage) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    });
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }
}