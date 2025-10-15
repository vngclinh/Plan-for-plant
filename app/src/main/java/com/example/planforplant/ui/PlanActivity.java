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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanActivity extends AppCompatActivity {

    private Spinner spinnerGarden, spinnerCompletion;
    private CalendarView calendarView;
    private TextView timePickerAction;
    private EditText etWaterDuration, etFertilizerType, etFertilizerAmount, etPruningNote, etNote;
    private CheckBox cbWatering, cbFertilizing, cbPruning;
    private Button btnCreatePlan, btnBackHome;

    private List<GardenResponse> myGardens = new ArrayList<>();
    private Long selectedGardenId = null;
    private int selectedHour = 8;
    private int selectedMinute = 0;
    private long selectedDateMillis;

    private boolean isEditMode = false;
    private List<GardenScheduleResponse> pendingSchedulesToEdit = null;

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
        etPruningNote = findViewById(R.id.etPruningNote);
        etNote = findViewById(R.id.etNote);
        cbWatering = findViewById(R.id.cbWatering);
        cbFertilizing = findViewById(R.id.cbFertilizing);
        cbPruning = findViewById(R.id.cbPruning);
        btnCreatePlan = findViewById(R.id.btnCreatePlan);
        btnBackHome = findViewById(R.id.btnBackHome);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        sessionManager = new SessionManager(this);

        // 🔹 Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("editMode", false);
        pendingSchedulesToEdit = (List<GardenScheduleResponse>) intent.getSerializableExtra("schedulesToEdit");

        if (isEditMode) {
            btnCreatePlan.setText("Cập nhật kế hoạch");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, displayOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCompletion.setAdapter(adapter);

        selectedDateMillis = calendarView.getDate();
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            selectedDateMillis = cal.getTimeInMillis();
        });

        timePickerAction.setOnClickListener(v -> openTimePickerDialog());
        loadMyGardens();

        btnCreatePlan.setOnClickListener(v -> createOrUpdateSchedules());
        btnBackHome.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }

    /** tải danh sách cây */
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

                    // 🔹 Nếu là chế độ chỉnh sửa → điền dữ liệu sau khi load danh sách cây
                    if (isEditMode && pendingSchedulesToEdit != null && !pendingSchedulesToEdit.isEmpty()) {
                        populateFieldsFromExistingSchedules(pendingSchedulesToEdit);
                    }

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

    /** mở chọn giờ */
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

    /** điền dữ liệu khi chỉnh sửa kế hoạch */
    private void populateFieldsFromExistingSchedules(List<GardenScheduleResponse> schedules) {
        if (schedules == null || schedules.isEmpty()) return;

        GardenScheduleResponse first = schedules.get(0);
        selectedGardenId = first.getGardenId();

        // chọn garden tương ứng
        for (int i = 0; i < myGardens.size(); i++) {
            if (myGardens.get(i).getId().equals(selectedGardenId)) {
                spinnerGarden.setSelection(i);
                break;
            }
        }

        // ngày giờ
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date d = sdf.parse(first.getScheduledTime());
            if (d != null) {
                calendarView.setDate(d.getTime());
                Calendar c = Calendar.getInstance();
                c.setTime(d);
                selectedHour = c.get(Calendar.HOUR_OF_DAY);
                selectedMinute = c.get(Calendar.MINUTE);
                timePickerAction.setText(String.format("Giờ thực hiện: %02d:%02d", selectedHour, selectedMinute));
            }
        } catch (ParseException ignored) {}

        // trạng thái
        for (int i = 0; i < apiValues.length; i++) {
            if (apiValues[i].equalsIgnoreCase(first.getCompletion())) {
                spinnerCompletion.setSelection(i);
                break;
            }
        }

        // điền dữ liệu theo từng loại
        for (GardenScheduleResponse s : schedules) {
            switch (s.getType().toUpperCase(Locale.ROOT)) {
                case "WATERING":
                    cbWatering.setChecked(true);
                    if (s.getWaterAmount() != null)
                        etWaterDuration.setText(String.valueOf(s.getWaterAmount()));
                    break;
                case "FERTILIZING":
                    cbFertilizing.setChecked(true);
                    if (s.getFertilityType() != null)
                        etFertilizerType.setText(s.getFertilityType());
                    if (s.getFertilityAmount() != null)
                        etFertilizerAmount.setText(String.valueOf(s.getFertilityAmount()));
                    break;
                case "PRUNING":
                    cbPruning.setChecked(true);
                    if (s.getNote() != null)
                        etPruningNote.setText(s.getNote());
                    break;
                case "NOTE":
                    if (s.getNote() != null)
                        etNote.setText(s.getNote());
                    break;
            }
        }
    }

    /** tạo hoặc ghi đè kế hoạch */
    private void createOrUpdateSchedules() {
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
        if (cbPruning.isChecked()) requests.add(makeReq("PRUNING", scheduledTime, completion));
        if (!cbWatering.isChecked() && !cbFertilizing.isChecked() && !cbPruning.isChecked() &&
                !etNote.getText().toString().trim().isEmpty()) {
            requests.add(makeReq("NOTE", scheduledTime, completion));
        }

        if (requests.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn hoạt động hoặc nhập ghi chú 🌿", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Đang xử lý kế hoạch...");
        progressDialog.show();

        ApiService api = ApiClient.getLocalClient(this).create(ApiService.class);
        final int[] done = {0};

        for (GardenScheduleRequest req : requests) {
            api.getSchedulesByDate(datePart).enqueue(new Callback<List<GardenScheduleResponse>>() {
                @Override
                public void onResponse(@NonNull Call<List<GardenScheduleResponse>> call,
                                       @NonNull Response<List<GardenScheduleResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<GardenScheduleResponse> list = response.body();
                        GardenScheduleResponse existing = null;
                        for (GardenScheduleResponse s : list) {
                            if (s.getType().equalsIgnoreCase(req.getType()) &&
                                    s.getGardenId().equals(req.getGardenId()) &&
                                    s.getScheduledTime().startsWith(datePart)) {
                                existing = s;
                                break;
                            }
                        }

                        if (existing != null) {
                            api.updateSchedule(existing.getId(), req).enqueue(new Callback<GardenScheduleResponse>() {
                                @Override
                                public void onResponse(@NonNull Call<GardenScheduleResponse> call,
                                                       @NonNull Response<GardenScheduleResponse> res2) {
                                    done[0]++;
                                    checkDone(requests.size(), done[0]);
                                }

                                @Override
                                public void onFailure(@NonNull Call<GardenScheduleResponse> call, @NonNull Throwable t) {
                                    Toast.makeText(PlanActivity.this, "Lỗi cập nhật: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            api.createSchedule(req).enqueue(new Callback<GardenScheduleResponse>() {
                                @Override
                                public void onResponse(@NonNull Call<GardenScheduleResponse> call,
                                                       @NonNull Response<GardenScheduleResponse> res3) {
                                    done[0]++;
                                    checkDone(requests.size(), done[0]);
                                }

                                @Override
                                public void onFailure(@NonNull Call<GardenScheduleResponse> call, @NonNull Throwable t) {
                                    Toast.makeText(PlanActivity.this, "Lỗi tạo mới: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<GardenScheduleResponse>> call, @NonNull Throwable t) {
                    Toast.makeText(PlanActivity.this, "Lỗi kiểm tra: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void checkDone(int total, int success) {
        if (success >= total) {
            progressDialog.dismiss();
            Toast.makeText(this, "Đã xử lý " + success + "/" + total + " kế hoạch 🌿", Toast.LENGTH_LONG).show();
            finish();
        }
    }

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
        } else if (type.equals("PRUNING")) {
            String note = etPruningNote.getText().toString().trim();
            if (!note.isEmpty()) req.setNote(note);
        }

        return req;
    }
}
