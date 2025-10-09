package com.example.planforplant.ui;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.planforplant.R;
import com.example.planforplant.DTO.GardenScheduleResponse;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.GardenScheduleApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CareDetailActivity extends AppCompatActivity {

    private LinearLayout layoutScheduleContainer;
    private GardenScheduleApi scheduleApi;

    private final Long gardenId = 1L;
    private final String selectedDate = "2025-10-08";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.care_detail);

        layoutScheduleContainer = findViewById(R.id.layoutScheduleContainer);
        scheduleApi = ApiClient.getLocalClient(this).create(GardenScheduleApi.class);

        loadSchedules();
    }

    private void loadSchedules() {
        scheduleApi.getSchedulesByGarden(gardenId).enqueue(new Callback<List<GardenScheduleResponse>>() {
            @Override
            public void onResponse(Call<List<GardenScheduleResponse>> call, Response<List<GardenScheduleResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showSchedules(response.body());
                } else {
                    Toast.makeText(CareDetailActivity.this, "Không tải được kế hoạch", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<GardenScheduleResponse>> call, Throwable t) {
                Toast.makeText(CareDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showSchedules(List<GardenScheduleResponse> schedules) {
        layoutScheduleContainer.removeAllViews();

        boolean hasWork = false;
        for (GardenScheduleResponse s : schedules) {
            if (s.scheduledTime != null && s.scheduledTime.startsWith(selectedDate)) {
                hasWork = true;

                CardView card = new CardView(this);
                card.setRadius(12);
                card.setCardElevation(6);
                card.setUseCompatPadding(true);

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(24, 24, 24, 24);

                TextView emoji = new TextView(this);
                emoji.setTextSize(20);

                TextView text = new TextView(this);
                text.setTextSize(16);
                text.setPadding(16, 0, 0, 0);

                switch (s.type) {
                    case "WATERING":
                        emoji.setText("💧");
                        text.setText("Tưới nước: " + (s.waterAmount != null ? s.waterAmount + " ml" : "Không rõ"));
                        break;
                    case "FERTILIZER":
                        emoji.setText("🌱");
                        text.setText("Bón phân: " + (s.note != null ? s.note : "Không rõ"));
                        break;
                    case "PRUNING":
                        emoji.setText("✂️");
                        text.setText("Tỉa lá: " + (s.note != null ? s.note : "Không có ghi chú"));
                        break;
                    default:
                        emoji.setText("🪴");
                        text.setText("Công việc khác: " + (s.note != null ? s.note : ""));
                        break;
                }

                row.addView(emoji);
                row.addView(text);
                card.addView(row);
                layoutScheduleContainer.addView(card);
            }
        }

        if (!hasWork) {
            TextView noWork = new TextView(this);
            noWork.setText("Không có công việc nào trong ngày 🌤️");
            noWork.setPadding(24, 24, 24, 24);
            noWork.setTextSize(16);
            layoutScheduleContainer.addView(noWork);
        }
    }
}
