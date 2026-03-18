package com.example.mtg_java;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;



import com.example.mtg_java.databinding.ActivityMainBinding;
import com.example.mtg_java.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        SessionManager sessionManager = new SessionManager(this);

        if (!sessionManager.isSessionValid()) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        //  Preload ML model
        ((MyApp) getApplication()).preloadModelIfNeeded();

        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
        }
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.home) {
                replaceFragment(new com.example.mtg_java.HomeFragment());
            } else if (id == R.id.scan) {
                replaceFragment(new ScanFragment());
            } else if (id == R.id.collection) {
                replaceFragment(new CollectionFragment());
            } else if (id == R.id.settings) {
                replaceFragment(new SettingsFragment());
            }

            return true;
        });


    }
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
}