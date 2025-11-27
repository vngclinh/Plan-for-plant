package com.example.planforplant.ui;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.planforplant.R;

public class DiseaseNotFoundActivity extends AppCompatActivity {

    private TextView tvName, tvDescription, tvSuggestion, tvProbability;
    private ImageView imgDisease;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_not_found);

        tvName = findViewById(R.id.tvName);
        tvProbability = findViewById(R.id.tvProbability);
        tvDescription = findViewById(R.id.tvDescription);
        tvSuggestion = findViewById(R.id.tvSuggestion);
        imgDisease = findViewById(R.id.imgDisease);

        String name = getIntent().getStringExtra("diseaseName");
        double prob = getIntent().getDoubleExtra("probability", 0);
        String description = getIntent().getStringExtra("description");
        String imageUrl = getIntent().getStringExtra("imageUrl");

        tvName.setText(name);
        tvProbability.setText(String.format("Xác suất: %.1f%%", prob * 100));

        if (description != null && !description.isEmpty())
            tvDescription.setText(description);
        else
            tvDescription.setText("Không có mô tả chi tiết từ Plant.id");

        tvSuggestion.setText("Bệnh không có trong hệ thống của bạn.");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(imgDisease);
        } else {
            imgDisease.setImageResource(R.drawable.img_not_found);
        }
    }
}
