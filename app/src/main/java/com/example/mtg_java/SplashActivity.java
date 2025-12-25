package com.example.mtg_java;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mtg_java.utils.SessionManager; // we'll create this to manage login state

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is logged in
        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn()) {
            // go to MainActivity
            startActivity(new Intent(this, MainActivity.class));
        } else {
            // go to LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish(); // so user cannot go back to Splash
    }
}
