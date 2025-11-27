package com.example.planforplant.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;
import com.example.planforplant.model.Disease;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchDiseaseActivity extends AppCompatActivity {

    private RecyclerView recyclerDiseases;
    private EditText etSearch;
    private TextView tvEmpty;
    private DiseaseListAdapter adapter;
    private List<Disease> diseaseList = new ArrayList<>();
    private List<Disease> filteredList = new ArrayList<>();
    private long gardenId;
    private String nickname;

    private String status;
    private String dateAdded;
    private Long plantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_disease);

        recyclerDiseases = findViewById(R.id.recyclerSearchResults);
        etSearch = findViewById(R.id.etSearchDisease);
        tvEmpty = findViewById(R.id.tvNoResult);
        ImageView btnBack = findViewById(R.id.btn_back);

        // Lấy thông tin garden từ intent
        gardenId = getIntent().getLongExtra("gardenId", -1);
        nickname = getIntent().getStringExtra("nickname");

        status = getIntent().getStringExtra("status");
        dateAdded = getIntent().getStringExtra("dateAdded");
        plantId = getIntent().getLongExtra("plantId", -1);

        btnBack.setOnClickListener(v -> finish());


        recyclerDiseases.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DiseaseListAdapter(this, filteredList, disease -> {



            Intent intent = new Intent(this, DiseaseDetailActivity.class);
            intent.putExtra("diseaseId", disease.getId());
            intent.putExtra("gardenId",getIntent().getLongExtra("gardenId", -1));
            intent.putExtra("nickname",getIntent().getStringExtra("nickname"));
            intent.putExtra("status", getIntent().getStringExtra("status"));
            intent.putExtra("dateAdded", getIntent().getStringExtra("dateAdded"));
            intent.putExtra("plantId", getIntent().getLongExtra("plantId", -1));
            startActivity(intent);
        });
        recyclerDiseases.setAdapter(adapter);

        // Load danh sách bệnh từ API
        loadDiseases();

        // Filter theo từ khóa khi người dùng nhập
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadDiseases() {
        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);
        api.getAllDiseases().enqueue(new Callback<List<Disease>>() {
            @Override
            public void onResponse(Call<List<Disease>> call, Response<List<Disease>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    tvEmpty.setText("Không thể tải danh sách bệnh.\nMã lỗi: " + response.code());
                    tvEmpty.setVisibility(View.VISIBLE);
                    return;
                }
                diseaseList.clear();
                diseaseList.addAll(response.body());
                filter(etSearch.getText().toString());
            }

            @Override
            public void onFailure(Call<List<Disease>> call, Throwable t) {
                tvEmpty.setText("Không thể kết nối tới server.\n" + t.getMessage());
                tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(SearchDiseaseActivity.this, "Cannot load diseases", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void filter(String keyword) {
        filteredList.clear();
        String keywordNormalized = removeDiacritics(keyword.toLowerCase().trim());

        if (keywordNormalized.isEmpty()) {
            filteredList.addAll(diseaseList);
        } else {
            for (Disease d : diseaseList) {
                String diseaseNameNormalized = removeDiacritics(d.getName().toLowerCase());
                if (diseaseNameNormalized.contains(keywordNormalized)) {
                    filteredList.add(d);
                }
            }
        }
        tvEmpty.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.notifyDataSetChanged();
    }
    public static String removeDiacritics(String str) {
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }
}