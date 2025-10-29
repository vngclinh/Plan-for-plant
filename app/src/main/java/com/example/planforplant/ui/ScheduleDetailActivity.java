package com.example.planforplant.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.planforplant.DTO.GardenScheduleResponse;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;

import java.text.SimpleDateFormat;
import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleDetailActivity extends AppCompatActivity {

    private LinearLayout layoutContainer;
    private TextView tvHeader;
    private ImageView btnBack;
    private Button btnEdit, btnBackBottom;
    private ProgressDialog progressDialog;
    private String scheduledTime;

    // Lưu kế hoạch mới nhất mỗi loại
    private Map<String, GardenScheduleResponse> latestByType = new LinkedHashMap<>();

    // Launcher để chờ kết quả từ PlanActivity
    private ActivityResultLauncher<Intent> editLauncher;

    public static void start(Context context, String scheduledTime) {
        Intent intent = new Intent(context, ScheduleDetailActivity.class);
        intent.putExtra("scheduledTime", scheduledTime);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_detail);

        layoutContainer = findViewById(R.id.layoutContainer);
        tvHeader = findViewById(R.id.tvHeader);
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
        btnBackBottom = findViewById(R.id.btnBackBottom);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        scheduledTime = getIntent().getStringExtra("scheduledTime");
        if (scheduledTime == null || scheduledTime.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thời gian kế hoạch", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo launcher để reload khi PlanActivity trả về kết quả
        editLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        progressDialog.setMessage("Đang tải lại kế hoạch...");
                        progressDialog.show();
                        loadSchedulesByDate(scheduledTime);
                        Toast.makeText(this, "Đã cập nhật thành công 🌿", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        loadSchedulesByDate(scheduledTime);

        btnBack.setOnClickListener(v -> finish());
        btnBackBottom.setOnClickListener(v -> finish());

        // Khi bấm chỉnh sửa → mở PlanActivity (chờ kết quả)
        btnEdit.setOnClickListener(v -> {
            if (latestByType.isEmpty()) {
                Toast.makeText(this, "Không có kế hoạch nào để chỉnh sửa 🌿", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, PlanActivity.class);
            intent.putExtra("editMode", true);
            ArrayList<GardenScheduleResponse> editableList = new ArrayList<>(latestByType.values());
            intent.putExtra("schedulesToEdit", editableList);

            editLauncher.launch(intent);
        });
    }

    /** Lấy kế hoạch theo ngày và chỉ giữ lại bản mới nhất cho mỗi loại */
    private void loadSchedulesByDate(String scheduledTime) {
        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);

        String datePart = scheduledTime.contains("T")
                ? scheduledTime.split("T")[0]
                : scheduledTime.substring(0, 10);

        progressDialog.setMessage("Đang tải kế hoạch ngày " + datePart + "...");
        progressDialog.show();

        api.getSchedulesByDate(datePart).enqueue(new Callback<List<GardenScheduleResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<GardenScheduleResponse>> call,
                                   @NonNull Response<List<GardenScheduleResponse>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    List<GardenScheduleResponse> schedules = response.body();

                    // Lọc cùng ngày
                    List<GardenScheduleResponse> sameDay = new ArrayList<>();
                    for (GardenScheduleResponse s : schedules) {
                        if (s.getScheduledTime() != null && s.getScheduledTime().startsWith(datePart)) {
                            sameDay.add(s);
                        }
                    }

                    // Giữ bản mới nhất theo loại
                    latestByType.clear();
                    for (GardenScheduleResponse s : sameDay) {
                        String type = s.getType();
                        if (type == null) continue;

                        if (!latestByType.containsKey(type)) {
                            latestByType.put(type, s);
                        } else {
                            GardenScheduleResponse existing = latestByType.get(type);
                            if (s.getUpdatedAt() != null && existing.getUpdatedAt() != null) {
                                if (s.getUpdatedAt().compareTo(existing.getUpdatedAt()) > 0) {
                                    latestByType.put(type, s);
                                }
                            } else if (s.getId() > existing.getId()) {
                                latestByType.put(type, s);
                            }
                        }
                    }

                    // Hiển thị ra màn hình
                    layoutContainer.removeAllViews();
                    SimpleDateFormat inFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat outFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());

                    for (GardenScheduleResponse s : latestByType.values()) {
                        LinearLayout card = new LinearLayout(ScheduleDetailActivity.this);
                        card.setOrientation(LinearLayout.VERTICAL);
                        card.setPadding(24, 24, 24, 24);
                        card.setBackgroundResource(R.drawable.bg_card);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 0, 0, 24);
                        card.setLayoutParams(params);

                        // Tiêu đề
                        TextView title = new TextView(ScheduleDetailActivity.this);
                        title.setText("📋 " + translateType(s.getType()));
                        title.setTextSize(18f);
                        title.setTextColor(getResources().getColor(R.color.green_primary));
                        title.setTypeface(null, Typeface.BOLD);
                        card.addView(title);

                        // Thời gian
                        String formattedTime = s.getScheduledTime();
                        try {
                            Date d = inFmt.parse(s.getScheduledTime());
                            formattedTime = outFmt.format(d);
                        } catch (Exception ignored) {}

                        // Chi tiết
                        TextView detail = new TextView(ScheduleDetailActivity.this);
                        detail.setTextColor(getResources().getColor(R.color.black));
                        detail.setTextSize(15f);
                        detail.setText("🕒 " + formattedTime + "\n");

                        if (s.getNote() != null && !s.getNote().isEmpty())
                            detail.append("📝 " + s.getNote() + "\n");
                        if (s.getWaterAmount() != null)
                            detail.append("💧 " + s.getWaterAmount() + " ml\n");
                        if (s.getFertilityType() != null)
                            detail.append("🧪 " + s.getFertilityType() + "\n");
                        if (s.getFertilityAmount() != null)
                            detail.append("⚖️ " + s.getFertilityAmount() + " g\n");
                        detail.append("📌 Trạng thái: " + s.getCompletion());

                        card.addView(detail);
                        layoutContainer.addView(card);
                    }

                    if (layoutContainer.getChildCount() == 0)
                        Toast.makeText(ScheduleDetailActivity.this,
                                "Không có kế hoạch nào trong ngày này 🌿", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(ScheduleDetailActivity.this, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GardenScheduleResponse>> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(ScheduleDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Dịch loại hoạt động sang tiếng Việt */
    private String translateType(String type) {
        if (type == null) return "(Không rõ)";
        switch (type.toUpperCase(Locale.ROOT)) {
            case "WATERING": return "Tưới nước 💧";
            case "FERTILIZING": return "Bón phân 🌱";
            case "MIST" : return "Phun ẩm";
            case "PRUNING": return "Tỉa lá ✂️";
            case "OTHER": return "Hoạt động khác 📝";
            default: return type;
        }
    }
}
