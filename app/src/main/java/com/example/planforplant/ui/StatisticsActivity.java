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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        barChart = findViewById(R.id.barChart);
        tvPlaceholder = findViewById(R.id.tvStatisticsPlaceholder);
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
                    } else {
                        tvPlaceholder.setVisibility(View.GONE);
                        barChart.setVisibility(View.VISIBLE);
                        barChart.setData(statsPoints);
                    }
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
