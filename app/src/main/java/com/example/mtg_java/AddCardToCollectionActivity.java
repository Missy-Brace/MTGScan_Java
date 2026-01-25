package com.example.mtg_java;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class AddCardToCollectionActivity extends AppCompatActivity {

    String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        groupId = getIntent().getStringExtra("group_id");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        AddCardToCollectionFragment f = new AddCardToCollectionFragment();
        Bundle b = new Bundle();
        b.putString("group_id", groupId);
        f.setArguments(b);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, f)
                .commit();
    }
}
