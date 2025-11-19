package com.example.planforplant.ui;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.planforplant.R;

public class NotificationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);

        // You can add logic here to handle the back button, etc.
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
