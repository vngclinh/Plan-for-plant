package com.example.planforplant.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.planforplant.R;

public class NotFoundActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notfound);

        TextView tvMessage = findViewById(com.example.planforplant.R.id.tvMessage);
        Button btnRetry = findViewById(R.id.btnRetry);

        // Optionally get message from previous activity
        String message = getIntent().getStringExtra("message");
        if (message != null) {
            tvMessage.setText(message);
        }

        // Retry = go back to search
        btnRetry.setOnClickListener(v -> {
            Intent intent = new Intent(NotFoundActivity.this, SearchActivity.class);
            startActivity(intent);
            finish(); // close "Not Found" screen
        });
    }
}
