package com.example.planforplant.ui;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.planforplant.R;

public class DiseaseNotFoundActivity extends AppCompatActivity {

    private ImageView imgCaptured;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_not_found);

        imgCaptured = findViewById(R.id.imgCaptured);

        String capturedImagePath = getIntent().getStringExtra("capturedImage");
        if (capturedImagePath != null) {
            Glide.with(this).load(Uri.parse(capturedImagePath)).into(imgCaptured);
        }
        findViewById(R.id.btnRetry).setOnClickListener(v -> {
            finish();
        });
    }
}
