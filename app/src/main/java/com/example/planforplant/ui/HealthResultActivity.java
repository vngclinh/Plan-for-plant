package com.example.planforplant.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.planforplant.R;
import com.example.planforplant.model.HealthResponse;
import com.google.gson.Gson;

public class HealthResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_result);

        LinearLayout container = findViewById(R.id.resultContainer);
        String json = getIntent().getStringExtra("result");
        HealthResponse response = new Gson().fromJson(json, HealthResponse.class);

        if (response != null && response.getResult() != null
                && response.getResult().getDisease() != null) {
            for (HealthResponse.Suggestion s : response.getResult().getDisease().suggestions) {
                View card = getLayoutInflater().inflate(R.layout.item_health_disease, container, false);

                TextView tvName = card.findViewById(R.id.tvDiseaseName);
                TextView tvProb = card.findViewById(R.id.tvDiseaseProb);

                tvName.setText("🍂 " + s.name);
                tvProb.setText(String.format("Xác suất: %.1f%%", s.probability * 100));

                container.addView(card);
            }
        }
    }
}
