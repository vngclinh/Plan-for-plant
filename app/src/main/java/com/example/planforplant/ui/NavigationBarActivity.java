package com.example.planforplant.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import com.example.planforplant.R;
import com.example.planforplant.ui.DiaryActivity;
import com.example.planforplant.ui.HomeActivity;

public class NavigationBarActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sử dụng đúng layout
        setContentView(R.layout.view_bottom_nav);

        // Tìm các mục menu bằng ID của LinearLayout
        View btnTrangChu = findViewById(R.id.nav_home);
        View btnNhatKy = findViewById(R.id.nav_diary);

        btnTrangChu.setOnClickListener(v -> {
            Intent intent = new Intent(NavigationBarActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        btnNhatKy.setOnClickListener(v -> {
            Intent intent = new Intent(NavigationBarActivity.this, DiaryActivity.class);
            startActivity(intent);
        });
    }
}
