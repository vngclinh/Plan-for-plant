package com.example.planforplant.ui;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.example.planforplant.DTO.GardenScheduleRequest;
import com.example.planforplant.DTO.GardenScheduleResponse;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;
import com.example.planforplant.session.SessionManager;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView timePickerAction;
    private EditText etWaterDuration, etFertilizerType, etFertilizerAmount, etPruningNote, etNote;
    private Spinner spinnerCompletion;
    private Button btnCreatePlan;

    private int selectedHour = 8;
    private int selectedMinute = 0;
    private long selectedDateMillis;
    private ProgressDialog progressDialog;
    private SessionManager sessionManager;
    private Long gardenId;  // lấy từ Intent (khi tạo kế hoạch cho cây cụ thể)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.care_plant);

        // === Gán view ===
        calendarView = findViewById(R.id.calendarView);
        timePickerAction = findViewById(R.id.timePickerAction);
        etWaterDuration = findViewById(R.id.etWaterDuration);
        etFertilizerType = findViewById(R.id.etFertilizerType);
        etFertilizerAmount = findViewById(R.id.etFertilizerAmount);
        etPruningNote = findViewById(R.id.etPruningNote);
        spinnerCompletion = findViewById(R.id.spinnerCompletion);
        etNote = findViewById(R.id.etNote);
        btnCreatePlan = findViewById(R.id.btnCreatePlan);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        sessionManager = new SessionManager(this);

        // === Lấy gardenId từ Intent (nếu có) ===
        gardenId = getIntent().getLongExtra("gardenId", -1);
        if (gardenId == -1) {
            Toast.makeText(this, "Thiếu thông tin vườn 🌱", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        // === Ngày mặc định ===
        selectedDateMillis = calendarView.getDate();
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            selectedDateMillis = cal.getTimeInMillis();
        });

        // === Chọn giờ ===
        timePickerAction.setOnClickListener(v -> openTimePickerDialog());

        // === Chọn trạng thái ===
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"NotDone", "Done", "Skipped"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCompletion.setAdapter(adapter);

        // === Nút tạo kế hoạch ===
        btnCreatePlan.setOnClickListener(v -> createSchedule());
    }

    private void openTimePickerDialog() {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTitleText("🕒 Chọn giờ thực hiện")
                .setHour(selectedHour)
                .setMinute(selectedMinute)
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            selectedHour = picker.getHour();
            selectedMinute = picker.getMinute();
            timePickerAction.setText(String.format(Locale.getDefault(),
                    "Giờ thực hiện: %02d:%02d", selectedHour, selectedMinute));
        });

        picker.show(getSupportFragmentManager(), "material_time_picker");
    }

    private void createSchedule() {
        // === Kiểm tra gardenId ===
        if (gardenId == null || gardenId <= 0) {
            Toast.makeText(this, "Không xác định được vườn!", Toast.LENGTH_SHORT).show();
            return;
        }

        // === Tạo request ===
        GardenScheduleRequest req = new GardenScheduleRequest();
        req.setGardenId(gardenId);
        req.setCompletion(spinnerCompletion.getSelectedItem().toString());
        req.setNote(etNote.getText().toString());
        req.setType("WATERING");

        String waterStr = etWaterDuration.getText().toString();
        if (!waterStr.isEmpty()) req.setWaterAmount(Double.valueOf(waterStr));

        String fertType = etFertilizerType.getText().toString();
        if (!fertType.isEmpty()) req.setFertilityType(fertType);

        String fertAmt = etFertilizerAmount.getText().toString();
        if (!fertAmt.isEmpty()) req.setFertilityAmount(Double.valueOf(fertAmt));

        // === Gộp ngày + giờ ===
        Date date = new Date(selectedDateMillis);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String datePart = df.format(date);
        String timePart = String.format(Locale.getDefault(), "%02d:%02d:00", selectedHour, selectedMinute);
        String isoDateTime = datePart + "T" + timePart;
        req.setScheduledTime(isoDateTime);

        // === Gọi API ===
        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);

        progressDialog.setMessage("Đang tạo lịch chăm cây...");
        progressDialog.show();

        api.createSchedule(req).enqueue(new Callback<GardenScheduleResponse>() {
            @Override
            public void onResponse(@NonNull Call<GardenScheduleResponse> call, @NonNull Response<GardenScheduleResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(PlanActivity.this, "Tạo kế hoạch chăm cây thành công 🌿", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(PlanActivity.this, "Không thể tạo kế hoạch (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GardenScheduleResponse> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(PlanActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
