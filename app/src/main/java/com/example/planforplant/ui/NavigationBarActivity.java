package com.example.tenproject; // đổi thành tên package của bạn

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.planforplant.ui.DiaryActivity;
import com.example.planforplant.ui.HomeActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnTrangChu = findViewById(R.id.btnTrangChu);
        Button btnNhatKy = findViewById(R.id.btnNhatKy);

        btnTrangChu.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        btnNhatKy.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DiaryActivity.class);
            startActivity(intent);
        });
    }
}