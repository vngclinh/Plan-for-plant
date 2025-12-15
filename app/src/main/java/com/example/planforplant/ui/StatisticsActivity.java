package com.example.planforplant.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.planforplant.R;

import java.util.List;

public class StatisticsActivity extends NavigationBarActivity {

    private SimpleBarChartView barChart;
    private TextView tvPlaceholder;
    private LiveData<List<StatsPoint>> currentLiveData;
    private TextView tvToday, tvWeek, tvMonth, tvInsight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        barChart = findViewById(R.id.barChart);
        tvPlaceholder = findViewById(R.id.tvStatisticsPlaceholder);
        tvToday = findViewById(R.id.tvToday);
        tvWeek = findViewById(R.id.tvWeek);
        tvMonth = findViewById(R.id.tvMonth);
        tvInsight = findViewById(R.id.tvInsight);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        Spinner spinnerGranularity = findViewById(R.id.spinnerGranularity);

        // create ViewModel with AndroidViewModelFactory so it receives Application
        StatisticsViewModel viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(StatisticsViewModel.class);

        // configure spinner listener
        spinnerGranularity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Granularity gran;
                if (position == 0) gran = Granularity.DAY;
                else if (position == 1) gran = Granularity.WEEK;
                else gran = Granularity.MONTH;

                // remove previous observers
                if (currentLiveData != null) currentLiveData.removeObservers(StatisticsActivity.this);

                // show loading
                progressBar.setVisibility(View.VISIBLE);
                barChart.setVisibility(View.GONE);
                tvPlaceholder.setVisibility(View.GONE);

                currentLiveData = viewModel.getCounts(gran);
                currentLiveData.observe(StatisticsActivity.this, statsPoints -> {
                    progressBar.setVisibility(View.GONE);

                    if (statsPoints == null || statsPoints.isEmpty()) {
                        tvPlaceholder.setVisibility(View.VISIBLE);
                        barChart.setVisibility(View.GONE);
                        return;
                    }

                    // ===== 1. Hiện biểu đồ =====
                    tvPlaceholder.setVisibility(View.GONE);
                    barChart.setVisibility(View.VISIBLE);
                    barChart.setData(statsPoints);

                    // ===== 2. TÍNH SUMMARY =====
                    int total = 0;
                    for (StatsPoint p : statsPoints) {
                        total += p.getValue();
                    }

                    // ===== 3. SET TEXT CHO SUMMARY CARD =====
                    tvToday.setText(
                            String.valueOf(statsPoints.get(statsPoints.size() - 1).getValue())
                    );
                    tvWeek.setText(String.valueOf(total));
                    tvMonth.setText(String.valueOf(total));

                    // ===== 4. INSIGHT TEXT (nếu có) =====
                    tvInsight.setText(
                            "Bạn đã chăm cây " + total + " lần trong khoảng thời gian này 🌱"
                    );
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // no-op
            }
        });

        // Trigger initial load for default selection
        spinnerGranularity.setSelection(0);
    }
}
