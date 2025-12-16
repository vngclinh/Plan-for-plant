package com.example.planforplant.ui;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.example.planforplant.R;

public class NotificationActivity extends NavigationBarActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }
}
