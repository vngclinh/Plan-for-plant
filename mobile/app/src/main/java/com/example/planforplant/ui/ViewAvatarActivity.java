package com.example.planforplant.ui;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.planforplant.R;

public class ViewAvatarActivity extends AppCompatActivity {
    private ImageView imgFullAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_avatar);

        imgFullAvatar = findViewById(R.id.imgFullAvatar);

        String avatarUrl = getIntent().getStringExtra("avatarUrl");

        Glide.with(this)
                .load(avatarUrl)
                .into(imgFullAvatar);

        imgFullAvatar.setOnClickListener(v -> finish());
    }
}
