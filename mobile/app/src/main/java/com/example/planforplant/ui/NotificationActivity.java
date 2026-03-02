package com.example.planforplant.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planforplant.R;
import com.example.planforplant.DTO.NotificationResponse;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends NavigationBarActivity
        implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView rvNotifications;
    private TextView tvEmpty;
    private NotificationAdapter adapter;

    private ApiService api;

    // cache list hiện tại để update UI nhanh
    private List<NotificationResponse> rawList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        rvNotifications = findViewById(R.id.rvNotifications);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter();
        adapter.setOnNotificationClickListener(this);
        rvNotifications.setAdapter(adapter);

        api = ApiClient.getLocalClient(this).create(ApiService.class);

        Log.d("NOTI_UI", "📢 NotificationActivity OPENED");

        loadNotifications();
    }

    // “Reload như web” khi quay lại màn hình
    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }

    private void loadNotifications() {
        api.getMyNotifications().enqueue(new Callback<List<NotificationResponse>>() {
            @Override
            public void onResponse(Call<List<NotificationResponse>> call,
                                   Response<List<NotificationResponse>> response) {

                Log.d("NOTI_API", "GET /me code=" + response.code());

                if (!response.isSuccessful()) {
                    showEmpty();
                    return;
                }

                List<NotificationResponse> data = response.body();
                if (data == null || data.isEmpty()) {
                    rawList = new ArrayList<>();
                    showEmpty();
                    return;
                }

                rawList = data;
                showGroupedList(data);
            }

            @Override
            public void onFailure(Call<List<NotificationResponse>> call, Throwable t) {
                Toast.makeText(NotificationActivity.this,
                        "Không tải được thông báo", Toast.LENGTH_SHORT).show();
                showEmpty();
            }
        });
    }

    private void showEmpty() {
        tvEmpty.setVisibility(View.VISIBLE);
        rvNotifications.setVisibility(View.GONE);
    }

    private void showGroupedList(List<NotificationResponse> data) {
        tvEmpty.setVisibility(View.GONE);
        rvNotifications.setVisibility(View.VISIBLE);

        List<NotificationListItem> items = buildGroupedItems(data);
        adapter.setItems(items);
    }

    // Group theo ngày: Hôm nay / Hôm qua / dd/MM/yyyy
    private List<NotificationListItem> buildGroupedItems(List<NotificationResponse> data) {
        List<NotificationListItem> out = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        String lastHeader = null;

        for (NotificationResponse n : data) {
            LocalDate d = parseDate(n.createdAt);
            String header = formatHeader(d, today, yesterday);

            if (!Objects.equals(lastHeader, header)) {
                out.add(NotificationListItem.header(header));
                lastHeader = header;
            }

            out.add(NotificationListItem.item(n));
        }

        return out;
    }

    private LocalDate parseDate(String iso) {
        try {
            if (iso != null && iso.length() >= 10) {
                return LocalDate.parse(iso.substring(0, 10));
            }
        } catch (Exception ignored) {}
        return LocalDate.now();
    }

    private String formatHeader(LocalDate d, LocalDate today, LocalDate yesterday) {
        if (d.equals(today)) return "Hôm nay";
        if (d.equals(yesterday)) return "Hôm qua";
        return d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    // Click 1 noti -> mark read (update UI ngay + call BE)
    @Override
    public void onNotificationClicked(NotificationResponse n) {
        if (n == null || n.id == null) return;

        if (!n.read) {
            n.read = true;
            showGroupedList(rawList);
        }

        api.markAsRead(n.id).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                loadNotifications();
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(NotificationActivity.this,
                        "Không cập nhật trạng thái đã đọc", Toast.LENGTH_SHORT).show();
                loadNotifications();
            }
        });
    }

    @Override
    public void onNotificationLongClicked(NotificationResponse n) {
        if (n == null || n.id == null) return;

        api.deleteNotification(n.id).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                loadNotifications();
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(NotificationActivity.this,
                        "Không xoá được thông báo", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
