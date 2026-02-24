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

    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ check session FIRST
        SessionManager sessionManager = new SessionManager(this);
        //if (sessionManager.isLoggedIn()) {
          //  startActivity(new Intent(LoginActivity.this, MainActivity.class));
            //finish();
            //return;
        //}

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoSignup = findViewById(R.id.btnGoSignup);
        progressBar = findViewById(R.id.progressBar);

        authManager = new AuthManager(this);

        btnLogin.setOnClickListener(v -> handleLogin());

        btnGoSignup.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class))
        );
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

            // ✅ FIXED: correct method signature
            @Override
            public void onSuccess(String token, String userId, String username, String email, String finalProfileImage){
                progressBar.setVisibility(View.GONE);

                // Session already saved in AuthManager,
                // but keeping this is safe
                SessionManager sessionManager = new SessionManager(LoginActivity.this);

                sessionManager.setLoggedIn(true);
                sessionManager.saveUserId(userId);
                sessionManager.saveToken(token);
                sessionManager.saveUser(username, email);

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
