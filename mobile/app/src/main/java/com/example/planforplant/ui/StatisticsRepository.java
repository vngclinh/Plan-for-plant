package com.example.planforplant.ui;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.planforplant.DTO.GardenScheduleResponse;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsRepository {

    private final Context context;

    public StatisticsRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Backwards-compatible: previous method for last 7 days
     */
    public LiveData<List<StatsPoint>> getLast7DaysCounts() {
        return getCounts(Granularity.DAY);
    }

    /**
     * Public API to get aggregated counts based on granularity
     */
    public LiveData<List<StatsPoint>> getCounts(Granularity granularity) {
        MutableLiveData<List<StatsPoint>> liveData = new MutableLiveData<>();

        ApiService apiService = ApiClient.getLocalClient(context).create(ApiService.class);
        Call<List<GardenScheduleResponse>> call = apiService.getAllSchedules();

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<GardenScheduleResponse>> call, @NonNull Response<List<GardenScheduleResponse>> response) {
                LocalDate today = LocalDate.now();

                // Prepare buckets depending on granularity
                Map<Object, Integer> counts = new HashMap<>();
                List<Object> keysInOrder = new ArrayList<>();

                if (granularity == Granularity.DAY) {
                    for (int i = 6; i >= 0; i--) {
                        LocalDate d = today.minusDays(i);
                        counts.put(d, 0);
                        keysInOrder.add(d);
                    }
                } else if (granularity == Granularity.WEEK) {
                    // last 4 weeks (week starting Monday)
                    for (int i = 3; i >= 0; i--) {
                        LocalDate weekStart = today.minusWeeks(i).with(DayOfWeek.MONDAY);
                        counts.put(weekStart, 0);
                        keysInOrder.add(weekStart);
                    }
                } else { // MONTH
                    for (int i = 5; i >= 0; i--) {
                        YearMonth ym = YearMonth.from(today).minusMonths(i);
                        counts.put(ym, 0);
                        keysInOrder.add(ym);
                    }
                }

                if (response.isSuccessful() && response.body() != null) {
                    List<GardenScheduleResponse> list = response.body();

                    for (GardenScheduleResponse s : list) {
                        String scheduled = s.getScheduledTime();
                        String created = s.getCreatedAt();

                        LocalDate date = tryParseToLocalDate(scheduled);
                        if (date == null) date = tryParseToLocalDate(created);
                        if (date == null) continue;

                        if (granularity == Granularity.DAY) {
                            if (counts.containsKey(date)) {
                                Integer cur = counts.get(date);
                                counts.put(date, (cur == null ? 0 : cur) + 1);
                            }
                        } else if (granularity == Granularity.WEEK) {
                            LocalDate weekStart = date.with(DayOfWeek.MONDAY);
                            if (counts.containsKey(weekStart)) {
                                Integer cur = counts.get(weekStart);
                                counts.put(weekStart, (cur == null ? 0 : cur) + 1);
                            }
                        } else { // MONTH
                            YearMonth ym = YearMonth.from(date);
                            if (counts.containsKey(ym)) {
                                Integer cur = counts.get(ym);
                                counts.put(ym, (cur == null ? 0 : cur) + 1);
                            }
                        }
                    }

                    // Build result list with formatted labels
                    List<StatsPoint> points = new ArrayList<>();
                    DateTimeFormatter labelDayFmt = DateTimeFormatter.ofPattern("MM-dd", Locale.getDefault());
                    DateTimeFormatter labelMonthFmt = DateTimeFormatter.ofPattern("MM-yyyy", Locale.getDefault());

                    for (Object key : keysInOrder) {
                        if (granularity == Granularity.DAY) {
                            LocalDate d = (LocalDate) key;
                            Integer c = counts.get(d);
                            points.add(new StatsPoint(d.format(labelDayFmt), c == null ? 0 : c));
                        } else if (granularity == Granularity.WEEK) {
                            LocalDate wk = (LocalDate) key;
                            Integer c = counts.get(wk);
                            String label = wk.format(labelDayFmt); // show week start
                            points.add(new StatsPoint(label, c == null ? 0 : c));
                        } else {
                            YearMonth ym = (YearMonth) key;
                            Integer c = counts.get(ym);
                            points.add(new StatsPoint(ym.format(labelMonthFmt), c == null ? 0 : c));
                        }
                    }

                    liveData.postValue(points);
                } else {
                    // Return zeros for requested granularity
                    List<StatsPoint> points = new ArrayList<>();
                    DateTimeFormatter labelDayFmt = DateTimeFormatter.ofPattern("MM-dd", Locale.getDefault());
                    DateTimeFormatter labelMonthFmt = DateTimeFormatter.ofPattern("MM-yyyy", Locale.getDefault());

                    for (Object key : keysInOrder) {
                        if (granularity == Granularity.DAY) {
                            LocalDate d = (LocalDate) key;
                            points.add(new StatsPoint(d.format(labelDayFmt), 0));
                        } else if (granularity == Granularity.WEEK) {
                            LocalDate wk = (LocalDate) key;
                            points.add(new StatsPoint(wk.format(labelDayFmt), 0));
                        } else {
                            YearMonth ym = (YearMonth) key;
                            points.add(new StatsPoint(ym.format(labelMonthFmt), 0));
                        }
                    }
                    liveData.postValue(points);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GardenScheduleResponse>> call, @NonNull Throwable t) {
                Log.e("StatisticsRepo", "API failure: " + t.getMessage(), t);
                LocalDate today = LocalDate.now();
                Map<Object, Integer> counts = new HashMap<>();
                List<Object> keysInOrder = new ArrayList<>();

                if (granularity == Granularity.DAY) {
                    for (int i = 6; i >= 0; i--) {
                        LocalDate d = today.minusDays(i);
                        keysInOrder.add(d);
                    }
                } else if (granularity == Granularity.WEEK) {
                    for (int i = 3; i >= 0; i--) {
                        LocalDate weekStart = today.minusWeeks(i).with(DayOfWeek.MONDAY);
                        keysInOrder.add(weekStart);
                    }
                } else {
                    for (int i = 5; i >= 0; i--) {
                        YearMonth ym = YearMonth.from(today).minusMonths(i);
                        keysInOrder.add(ym);
                    }
                }

                List<StatsPoint> points = new ArrayList<>();
                DateTimeFormatter labelDayFmt = DateTimeFormatter.ofPattern("MM-dd", Locale.getDefault());
                DateTimeFormatter labelMonthFmt = DateTimeFormatter.ofPattern("MM-yyyy", Locale.getDefault());

                for (Object key : keysInOrder) {
                    if (granularity == Granularity.DAY) {
                        LocalDate d = (LocalDate) key;
                        points.add(new StatsPoint(d.format(labelDayFmt), 0));
                    } else if (granularity == Granularity.WEEK) {
                        LocalDate wk = (LocalDate) key;
                        points.add(new StatsPoint(wk.format(labelDayFmt), 0));
                    } else {
                        YearMonth ym = (YearMonth) key;
                        points.add(new StatsPoint(ym.format(labelMonthFmt), 0));
                    }
                }

                liveData.postValue(points);
            }
        });

        return liveData;
    }

    private LocalDate tryParseToLocalDate(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            // Try OffsetDateTime first (handles timezone offsets and Z)
            OffsetDateTime odt = OffsetDateTime.parse(s, DateTimeFormatter.ISO_DATE_TIME);
            return odt.toLocalDate();
        } catch (Exception ignored) {
        }

        try {
            // Try LocalDateTime
            LocalDateTime ldt = LocalDateTime.parse(s, DateTimeFormatter.ISO_DATE_TIME);
            return ldt.toLocalDate();
        } catch (Exception ignored) {
        }

        try {
            // Try plain LocalDate
            return LocalDate.parse(s, DateTimeFormatter.ISO_DATE);
        } catch (Exception ignored) {
        }

        return null;
    }
}
