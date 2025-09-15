package com.example.planforplant.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.planforplant.R;
import com.example.planforplant.ui.SearchActivity;

public class MainActivity extends AppCompatActivity {

    private EditText searchBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        searchBox = findViewById(R.id.search_box);

        // 🔎 Search box action
        searchBox.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                String keyword = searchBox.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                    intent.putExtra("keyword", keyword);
                    startActivity(intent);
                }
                return true;
            }
            return false;
        });


        LinearLayout plantIdentifier = findViewById(R.id.plant_identifier);
        plantIdentifier.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, IdentifyActivity.class);
            startActivity(intent);
        });
    }
}