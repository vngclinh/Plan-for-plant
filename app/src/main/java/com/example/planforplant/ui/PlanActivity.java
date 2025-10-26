package com.example.planforplant.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.planforplant.DTO.GardenResponse;
import com.example.planforplant.DTO.GardenScheduleRequest;
import com.example.planforplant.DTO.GardenScheduleResponse;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;
import com.example.planforplant.session.SessionManager;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanActivity extends AppCompatActivity {

    private Spinner spinnerGarden, spinnerCompletion;
    private CalendarView calendarView;
    private TextView timePickerAction;
    private EditText etWaterDuration, etFertilizerType, etFertilizerAmount, etNote;
    private CheckBox cbWatering, cbFertilizing;
    private Button btnCreatePlan, btnBackHome;

    private List<GardenResponse> myGardens = new ArrayList<>();
    private Long selectedGardenId = null;
    private int selectedHour = 8;
    private int selectedMinute = 0;
    private long selectedDateMillis;

    private final String[] displayOptions = {"🌱 Chưa thực hiện", "🌿 Đã hoàn thành", "🍂 Bỏ qua"};
    private final String[] apiValues = {"NotDone", "Complete", "Skipped"};

    private ProgressDialog progressDialog;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.care_plant);

        spinnerGarden = findViewById(R.id.spinnerGarden);
        spinnerCompletion = findViewById(R.id.spinnerCompletion);
        calendarView = findViewById(R.id.calendarView);
        timePickerAction = findViewById(R.id.timePickerAction);
        etWaterDuration = findViewById(R.id.etWaterDuration);
        etFertilizerType = findViewById(R.id.etFertilizerType);
        etFertilizerAmount = findViewById(R.id.etFertilizerAmount);
        etNote = findViewById(R.id.etNote);
        cbWatering = findViewById(R.id.cbWatering);
        cbFertilizing = findViewById(R.id.cbFertilizing);
        btnCreatePlan = findViewById(R.id.btnCreatePlan);
        btnBackHome = findViewById(R.id.btnBackHome);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        sessionManager = new SessionManager(this);

        // Không cho chọn ngày quá khứ
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        calendarView.setMinDate(today.getTimeInMillis());

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            if (cal.before(today)) {
                Toast.makeText(this, "Không thể lập kế hoạch cho ngày đã qua 🌿", Toast.LENGTH_SHORT).show();
                calendarView.setDate(today.getTimeInMillis(), true, true);
            } else {
                selectedDateMillis = cal.getTimeInMillis();
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Toast.makeText(this, "Đã chọn ngày: " + fmt.format(cal.getTime()), Toast.LENGTH_SHORT).show();
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, displayOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCompletion.setAdapter(adapter);

        selectedDateMillis = calendarView.getDate();
        timePickerAction.setOnClickListener(v -> openTimePickerDialog());
        loadMyGardens();

        btnCreatePlan.setOnClickListener(v -> createSchedules());
        btnBackHome.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }

    /*Tải danh sách cây */
    private void loadMyGardens() {
        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);
        progressDialog.setMessage("Đang tải danh sách cây 🌿...");
        progressDialog.show();

        api.getMyGarden().enqueue(new Callback<List<GardenResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<GardenResponse>> call,
                                   @NonNull Response<List<GardenResponse>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    myGardens = response.body();
                    if (myGardens.isEmpty()) {
                        Toast.makeText(PlanActivity.this, "Bạn chưa có cây nào 🌱", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<String> names = new ArrayList<>();
                    for (GardenResponse g : myGardens) {
                        if (g.getPlant() != null && g.getPlant().getCommonName() != null)
                            names.add("🌿 " + g.getPlant().getCommonName());
                        else if (g.getNickname() != null && !g.getNickname().isEmpty())
                            names.add("🌱 " + g.getNickname());
                        else names.add("Cây không xác định");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(PlanActivity.this,
                            android.R.layout.simple_spinner_item, names);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerGarden.setAdapter(adapter);

                    spinnerGarden.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                            selectedGardenId = myGardens.get(position).getId();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedGardenId = null;
                        }
                    });

                } else {
                    Toast.makeText(PlanActivity.this, "Không thể tải danh sách cây", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GardenResponse>> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(PlanActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*Mở chọn giờ */
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
        picker.show(getSupportFragmentManager(), "time_picker");
    }

    /*Tạo kế hoạch mới */
    private void createSchedules() {
        if (selectedGardenId == null) {
            Toast.makeText(this, "Vui lòng chọn cây 🌱", Toast.LENGTH_SHORT).show();
            return;
        }

        List<GardenScheduleRequest> requests = new ArrayList<>();
        int selectedIndex = spinnerCompletion.getSelectedItemPosition();
        String completion = apiValues[selectedIndex];

        Date date = new Date(selectedDateMillis);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String datePart = df.format(date);
        String timePart = String.format(Locale.getDefault(), "%02d:%02d:00", selectedHour, selectedMinute);
        String scheduledTime = datePart + "T" + timePart;

        if (cbWatering.isChecked()) requests.add(makeReq("WATERING", scheduledTime, completion));
        if (cbFertilizing.isChecked()) requests.add(makeReq("FERTILIZING", scheduledTime, completion));
        if (!cbWatering.isChecked() && !cbFertilizing.isChecked() &&
                !etNote.getText().toString().trim().isEmpty()) {
            requests.add(makeReq("NOTE", scheduledTime, completion));
        }

        if (requests.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn hoạt động hoặc nhập ghi chú 🌿", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Đang lưu kế hoạch...");
        progressDialog.show();

        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);
        final int[] done = {0};

        for (GardenScheduleRequest req : requests) {
            api.createSchedule(req).enqueue(new Callback<GardenScheduleResponse>() {
                @Override
                public void onResponse(@NonNull Call<GardenScheduleResponse> call,
                                       @NonNull Response<GardenScheduleResponse> response) {
                    done[0]++;
                    checkDone(requests.size(), done[0]);
                }

                @Override
                public void onFailure(@NonNull Call<GardenScheduleResponse> call, @NonNull Throwable t) {
                    Toast.makeText(PlanActivity.this, "Lỗi khi lưu: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /*Kiểm tra khi hoàn tất lưu */
    private void checkDone(int total, int success) {
        if (success >= total) {
            progressDialog.dismiss();
            Toast.makeText(this, "Đã lưu kế hoạch thành công 🌿", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
        }
    }

    /*Tạo request gửi lên BE */
    private GardenScheduleRequest makeReq(String type, String scheduledTime, String completion) {
        GardenScheduleRequest req = new GardenScheduleRequest();
        req.setGardenId(selectedGardenId);
        req.setType(type);
        req.setScheduledTime(scheduledTime);
        req.setCompletion(completion);
        req.setNote(etNote.getText().toString().trim());

        if (type.equals("WATERING")) {
            String waterStr = etWaterDuration.getText().toString().trim();
            if (!waterStr.isEmpty()) req.setWaterAmount(Double.valueOf(waterStr));
        } else if (type.equals("FERTILIZING")) {
            req.setFertilityType(etFertilizerType.getText().toString().trim());
            String amt = etFertilizerAmount.getText().toString().trim();
            if (!amt.isEmpty()) req.setFertilityAmount(Double.valueOf(amt));
        }
        return req;
    }
}
