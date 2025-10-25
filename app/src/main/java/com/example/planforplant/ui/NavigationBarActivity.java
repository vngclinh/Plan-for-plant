package com.example.planforplant.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import com.example.planforplant.R;


public abstract class NavigationBarActivity extends AppCompatActivity {

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        setupBottomNavigation();
    }

    protected void setupBottomNavigation() {
        View navHome = findViewById(R.id.nav_home);
        View navDiary = findViewById(R.id.nav_diary);
        View navCamera = findViewById(R.id.nav_camera);
        View navChatbot = findViewById(R.id.nav_chatbot);
        View navProfile = findViewById(R.id.nav_profile);

        // Nút Trang chủ -> Mở MainActivity (hiển thị menu.xml)
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                if (!(this instanceof MainActivity)) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            });
        }

        // Nút Nhật ký -> Mở DiaryActivity (hiển thị care_calendar.xml)
        if (navDiary != null) {
            navDiary.setOnClickListener(v -> {
                if (!(this instanceof ScheduleListActivity)) {
                    Intent intent = new Intent(this, ScheduleListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            });
        }

        // Nút Camera -> Mở CaptureActivity
        if (navCamera != null) {
            navCamera.setOnClickListener(v -> {
                if (!(this instanceof CaptureActivity)) {
                    Intent intent = new Intent(this, CaptureActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            });
        }

        // Nút Chatbot -> Mở ChatbotActivity
        if (navChatbot != null) {
            navChatbot.setOnClickListener(v -> {
                if (!(this instanceof ChatbotActivity)) {
                    Intent intent = new Intent(this, ChatbotActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            });
        }

        // Nút Hồ sơ (tạm thời không làm gì)
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                if (!(this instanceof SettingActivity)) {
                    Intent intent = new Intent(this, SettingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            });
        }
    }
}
