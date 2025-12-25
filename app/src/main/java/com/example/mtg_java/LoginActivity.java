package com.example.mtg_java;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mtg_java.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoSignup;
    private ProgressBar progressBar;

    private AuthManager authManager; // a class to handle login/register logic

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn()) {
            // user is already logged in, go straight to main screen
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish(); // prevent going back to login
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoSignup = findViewById(R.id.btnGoSignup);
        progressBar = findViewById(R.id.progressBar);

        authManager = new AuthManager(this);

        btnLogin.setOnClickListener(v -> handleLogin());
        btnGoSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Email and password required", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        authManager.login(email, password, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                // Go to main app screen
                SessionManager session = new SessionManager(LoginActivity.this);
                session.setLoggedIn(true);

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();

            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
