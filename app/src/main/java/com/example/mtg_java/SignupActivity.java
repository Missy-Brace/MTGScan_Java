package com.example.mtg_java;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword;
    private Button btnSignup;
    private TextView btnGoLogin;
    private ProgressBar progressBar;

    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignup = findViewById(R.id.btnSignup);
        btnGoLogin = findViewById(R.id.btnGoLogin);

        String text = "Already have an account? Login";
        SpannableString spannable = new SpannableString(text);

        spannable.setSpan(
                new ForegroundColorSpan(Color.parseColor("#FF5A00")),
                text.indexOf("Login"),
                text.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        btnGoLogin.setText(spannable);
        progressBar = findViewById(R.id.progressBar);

        authManager = new AuthManager(this);

        btnSignup.setOnClickListener(v -> handleSignup());
        btnGoLogin.setOnClickListener(v -> finish());
    }
    private void handleSignup() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();


        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }

        if (username.length() < 3) {
            etUsername.setError("Username must be at least 3 characters");
            etUsername.requestFocus();
            return;
        }


        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter valid email");
            etEmail.requestFocus();
            return;
        }


        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        authManager.register(username, email, password, new AuthManager.AuthCallback() {

            @Override
            public void onSuccess(String token, String userId, String username, String email, String finalProfileImage) {
                progressBar.setVisibility(View.GONE);

                Toast.makeText(SignupActivity.this,
                        "Signup successful",
                        Toast.LENGTH_SHORT).show();

                startActivity(new Intent(SignupActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);


                if (message.toLowerCase().contains("exists") ||
                        message.toLowerCase().contains("duplicate")) {

                    etEmail.setError("Account already exists");
                    etEmail.requestFocus();
                } else {
                    Toast.makeText(SignupActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
