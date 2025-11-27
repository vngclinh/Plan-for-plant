package com.example.planforplant.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.AndroidViewModel;

import java.util.List;

public class StatisticsViewModel extends AndroidViewModel {

    private final StatisticsRepository repository;

    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        repository = new StatisticsRepository(application);
    }

    public LiveData<List<StatsPoint>> getLast7DaysCounts() {
        return repository.getLast7DaysCounts();
    }

    public LiveData<List<StatsPoint>> getCounts(Granularity granularity) {
        return repository.getCounts(granularity);
    }
}
