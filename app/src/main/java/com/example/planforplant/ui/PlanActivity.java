package com.example.planforplant.ui;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.GardenScheduleApi;
import com.example.planforplant.DTO.GardenScheduleRequest;
import com.example.planforplant.DTO.GardenScheduleResponse;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanActivity extends AppCompatActivity {

    private CheckBox cbWatering, cbFertilizer, cbPruning, cbOther;
    private EditText etWaterAmount, etWaterTime, etFertilizerType, etFertilizerAmount, etPruningNote, etOtherTask;
    private Button btnCreatePlan;
    private GardenScheduleApi scheduleApi;

    // Giả sử test cho gardenId = 1 (sau này truyền thật qua Intent)
    private final Long gardenId = 1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.care_plant);

        // Ánh xạ View
        cbWatering = findViewById(R.id.cbWatering);
        cbFertilizer = findViewById(R.id.cbFertilizer);
        cbPruning = findViewById(R.id.cbPruning);
        cbOther = findViewById(R.id.cbOther);

        etWaterAmount = findViewById(R.id.etWaterAmount);
        etWaterTime = findViewById(R.id.etWaterTime);
        etFertilizerType = findViewById(R.id.etFertilizerType);
        etFertilizerAmount = findViewById(R.id.etFertilizerAmount);
        etPruningNote = findViewById(R.id.etPruningNote);
        etOtherTask = findViewById(R.id.etOtherTask);
        btnCreatePlan = findViewById(R.id.btnCreatePlan);

        // Khởi tạo Retrofit client
        scheduleApi = ApiClient.getLocalClient(this).create(GardenScheduleApi.class);

        // Hiển thị TimePicker khi click vào ô giờ
        etWaterTime.setFocusable(false);
        etWaterTime.setOnClickListener(v -> showTimePickerDialog());

        // Nút lưu kế hoạch
        btnCreatePlan.setOnClickListener(v -> submitPlan());
    }

    // ----------------- TIME PICKER -----------------
    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(
                PlanActivity.this,
                (view, selectedHour, selectedMinute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    etWaterTime.setText(time);
                },
                hour, minute, true
        );

        dialog.setTitle("Chọn giờ tưới cây");
        dialog.show();
    }

    // ----------------- GỬI KẾ HOẠCH -----------------
    private void submitPlan() {
        if (!cbWatering.isChecked() && !cbFertilizer.isChecked() &&
                !cbPruning.isChecked() && !cbOther.isChecked()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 công việc!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cbWatering.isChecked()) {
            sendSchedule("WATERING", etWaterAmount.getText().toString(), "Tưới cây");
        }
        if (cbFertilizer.isChecked()) {
            sendSchedule("FERTILIZER", etFertilizerAmount.getText().toString(),
                    "Bón phân: " + etFertilizerType.getText().toString());
        }
        if (cbPruning.isChecked()) {
            sendSchedule("PRUNING", null, etPruningNote.getText().toString());
        }
        if (cbOther.isChecked()) {
            sendSchedule("OTHER", null, etOtherTask.getText().toString());
        }
    }

    // ----------------- GỬI REQUEST API -----------------
    private void sendSchedule(String type, String amount, String note) {
        GardenScheduleRequest request = new GardenScheduleRequest();
        request.gardenId = gardenId;
        request.type = type;
        request.note = note;

        // Thời gian hiện tại (hoặc có thể dùng giờ người chọn)
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        request.scheduledTime = sdf.format(calendar.getTime());

        Log.d("PlanActivity", "➡️ Gửi kế hoạch: " + type + " | time=" + request.scheduledTime);

        scheduleApi.createSchedule(request).enqueue(new Callback<GardenScheduleResponse>() {
            @Override
            public void onResponse(Call<GardenScheduleResponse> call, Response<GardenScheduleResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PlanActivity.this,
                            "✅ Đã lưu kế hoạch " + type + " thành công!", Toast.LENGTH_SHORT).show();
                    Log.d("PlanActivity", "✅ API Response OK: " + response.body());
                } else {
                    Toast.makeText(PlanActivity.this,
                            "✅ Đã lưu kế hoạch " + type + " thành công!", Toast.LENGTH_SHORT).show();
                    Log.e("PlanActivity", "❌ Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<GardenScheduleResponse> call, Throwable t) {
                Toast.makeText(PlanActivity.this,
                        "⚠️ Không thể kết nối tới server!", Toast.LENGTH_LONG).show();
                Log.e("PlanActivity", "API Error: " + t.getMessage(), t);
            }
        });
    }
}
