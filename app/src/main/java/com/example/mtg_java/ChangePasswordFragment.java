package com.example.mtg_java;

import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.*;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class ChangePasswordFragment extends Fragment {

    private EditText edtCurrent, edtNew;
    private AuthManager authManager;
    private EditText edtConfirm;
    private TextView txtSuccess;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_change_password, container, false);
        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        authManager = new AuthManager(requireContext());

        edtCurrent = view.findViewById(R.id.edtCurrentPassword);
        edtNew = view.findViewById(R.id.edtNewPassword);
        edtConfirm = view.findViewById(R.id.edtConfirmPassword);
        txtSuccess = view.findViewById(R.id.txtSuccess);
        setupPasswordToggle(edtCurrent);
        setupPasswordToggle(edtNew);
        setupPasswordToggle(edtConfirm);

        view.findViewById(R.id.btnChangePassword).setOnClickListener(v -> {

            String current = edtCurrent.getText().toString().trim();
            String newPass = edtNew.getText().toString().trim();
            String confirm = edtConfirm.getText().toString().trim();

            // 1️⃣ Empty validation
            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2️⃣ Password strength
            if (newPass.length() < 8) {
                Toast.makeText(getContext(), "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3️⃣ Confirm password match
            if (!newPass.equals(confirm)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            authManager.changePassword(
                    current,
                    newPass,
                    new AuthManager.AuthCallback() {
                        @Override
                        public void onSuccess(String t, String id, String u, String e, String finalProfileImage) {

                            txtSuccess.setVisibility(View.VISIBLE);

                            new Handler().postDelayed(() -> {
                                requireActivity().getSupportFragmentManager().popBackStack();
                            }, 1500);
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        return view;
    }
    private void setupPasswordToggle(EditText editText) {
        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width())) {

                    if (editText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    } else {
                        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }

                    editText.setSelection(editText.getText().length());
                    return true;
                }
            }
            return false;
        });
    }
}