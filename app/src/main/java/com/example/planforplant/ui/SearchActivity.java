package com.example.planforplant.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;
import com.example.planforplant.R;
import com.example.planforplant.model.Plant;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private EditText searchBox;
    private ImageView btnSearchIcon;
    private LinearLayout layoutSearchResult;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        searchBox = findViewById(R.id.search_box);
        btnSearchIcon = findViewById(R.id.btnSearchIcon);
        layoutSearchResult = findViewById(R.id.layoutSearchResult);
        apiService = ApiClient.getLocalClient().create(ApiService.class);

        // Nếu MainActivity gửi từ khóa
        String keyword = getIntent().getStringExtra("keyword");
        if (keyword != null && !keyword.isEmpty()) {
            searchBox.setText(keyword);
            triggerSearch(keyword);
        }

        // Khi nhấn nút search trên bàn phím
        searchBox.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                triggerSearch(searchBox.getText().toString());
                return true;
            }
            return false;
        });

        // Khi click vào icon tìm kiếm
        btnSearchIcon.setOnClickListener(v -> triggerSearch(searchBox.getText().toString()));
    }

    private void triggerSearch(String keyword) {
        if (keyword.isEmpty()) {
            Toast.makeText(this, "Nhập từ khóa để tìm kiếm", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.searchPlants(keyword.trim()).enqueue(new Callback<List<Plant>>() {
            @Override
            public void onResponse(Call<List<Plant>> call, Response<List<Plant>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Plant> plants = response.body();
                    if (plants.isEmpty()) {
                        Intent intent = new Intent(SearchActivity.this, NotFoundActivity.class);
                        intent.putExtra("message", "Không tìm thấy cây phù hợp với \"" + keyword + "\"");
                        startActivity(intent);
                        finish();
                    } else {
                        displayResults(plants);
                    }
                } else {
                    Toast.makeText(SearchActivity.this, "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Plant>> call, Throwable t) {
                Toast.makeText(SearchActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayResults(List<Plant> plants) {
        layoutSearchResult.removeAllViews();
        for (Plant plant : plants) {
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setPadding(8, 8, 8, 8);
            itemLayout.setBackgroundResource(R.drawable.bg_card);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 8);
            itemLayout.setLayoutParams(params);

            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(48, 48);
            imageView.setLayoutParams(imgParams);
            if (plant.getImageUrl() != null && !plant.getImageUrl().isEmpty()) {
                Picasso.get().load(plant.getImageUrl()).into(imageView);
            } else {
                imageView.setImageResource(R.drawable.ic_local_florist);
            }

            TextView textView = new TextView(this);
            textView.setText(plant.getCommonName());
            textView.setTextSize(16);
            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setPadding(12, 0, 0, 0);
            textView.setGravity(Gravity.CENTER_VERTICAL);

            itemLayout.addView(imageView);
            itemLayout.addView(textView);
            layoutSearchResult.addView(itemLayout);
        }
    }
}