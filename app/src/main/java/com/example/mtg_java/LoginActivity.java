package com.example.mtg_java;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
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
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mtg_java.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    TextView btnGoSignup;
    private ProgressBar progressBar;

    private AuthManager authManager;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.setVmPolicy(
                new StrictMode.VmPolicy.Builder()
                        .detectLeakedClosableObjects()
                        .penaltyLog()
                        .build()
        );
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        if (sessionManager.isSessionValid()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        findViewById(R.id.etEmail);
        findViewById(R.id.etPassword);
        findViewById(R.id.btnLogin);
        initUI();
    }

    private void initUI() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoSignup = findViewById(R.id.btnGoSignup);
        progressBar = findViewById(R.id.progressBar);

        String text = "Don't have an account? Signup";
        SpannableString spannable = new SpannableString(text);

        spannable.setSpan(
                new ForegroundColorSpan(android.graphics.Color.parseColor("#FF5A00")),
                text.indexOf("Signup"),
                text.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        btnGoSignup.setText(spannable);

        authManager = new AuthManager(getApplicationContext());

        btnLogin.setOnClickListener(v -> handleLogin());

        btnGoSignup.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class))
        );
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

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

        btnLogin.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        authManager.login(email, password, new AuthManager.AuthCallback() {

            @Override
            public void onSuccess(String token, String userId, String username, String email, String finalProfileImage) {
                btnLogin.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                sessionManager.setLoggedIn(true);
                sessionManager.saveUserId(userId);
                sessionManager.saveToken(token);
                sessionManager.saveUser(username, email);

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onError(String message) {
                btnLogin.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
