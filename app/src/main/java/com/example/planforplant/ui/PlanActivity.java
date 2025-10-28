package com.example.planforplant.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.planforplant.DTO.GardenResponse;
import com.example.planforplant.DTO.GardenScheduleRequest;
import com.example.planforplant.DTO.GardenScheduleResponse;
import com.example.planforplant.Notification.NotificationWorker;
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

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class PlanActivity extends AppCompatActivity {

    private Spinner spinnerGarden, spinnerCompletion;
    private CalendarView calendarView;
    private TextView timePickerAction;
    private EditText etWaterDuration, etFertilizerType, etFertilizerAmount, etNote;
    private CheckBox cbWatering, cbFertilizing, cbPruning, cbMisting, cbOther;
    private Button btnCreatePlan, btnBackHome;

    private List<GardenResponse> myGardens = new ArrayList<>();
    private Long selectedGardenId = null;
    private int selectedHour = 8;
    private int selectedMinute = 0;
    private long selectedDateMillis;

    private final String[] displayOptionsNotDone = {"🌱 Chưa thực hiện", "🌿 Đã hoàn thành"};
    private final String[] apiValuesNotDone = {"NotDone", "Complete"};

    private final String[] displayOptionsComplete = {"🌿 Đã hoàn thành"};
    private final String[] apiValuesComplete = {"Complete"};

    private String[] apiValuesCurrent = apiValuesNotDone;

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
        cbPruning = findViewById(R.id.cbPruning);
        cbMisting = findViewById(R.id.cbMist);
        cbOther = findViewById(R.id.cbOther);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        sessionManager = new SessionManager(this);

        TextView tabView = findViewById(R.id.tab_view);
        TextView tabCreate = findViewById(R.id.tab_create);


        tabView.setBackgroundResource(R.drawable.bg_tab_unselected);
        tabView.setTextColor(getColor(R.color.text_secondary));
        tabCreate.setBackgroundResource(R.drawable.bg_tab_selected);
        tabCreate.setTextColor(getColor(R.color.white));

        tabView.setOnClickListener(v -> {
            Intent intent = new Intent(PlanActivity.this, ScheduleListActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });


        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            selectedDateMillis = cal.getTimeInMillis();

            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Toast.makeText(this, "Đã chọn ngày: " + fmt.format(cal.getTime()), Toast.LENGTH_SHORT).show();

            // Cập nhật spinner Completion theo ngày
            updateCompletionSpinnerForDate(cal);
        });

        selectedDateMillis = calendarView.getDate();
        updateCompletionSpinnerForDate(Calendar.getInstance()); // mặc định hôm nay

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

    private void updateCompletionSpinnerForDate(Calendar selectedDate) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        String[] displayOptions;
        boolean enabled;

        if (selectedDate.before(today)) { // ngày đã qua
            displayOptions = displayOptionsComplete;
            apiValuesCurrent = apiValuesComplete;
            enabled = false;
        } else if (selectedDate.equals(today)) { // hôm nay
            displayOptions = displayOptionsNotDone;
            apiValuesCurrent = apiValuesNotDone;
            enabled = true;
        } else { // ngày tương lai
            displayOptions = new String[]{"🌱 Chưa thực hiện"};
            apiValuesCurrent = new String[]{"NotDone"};
            enabled = false;
        }

        spinnerCompletion.setEnabled(enabled);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, displayOptions) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                styleSpinnerItem(view, selectedDate, position);
                return view;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                styleSpinnerItem(view, selectedDate, position);
                return view;
            }

            private void styleSpinnerItem(TextView view, Calendar date, int position) {
                if (date.before(today)) {
                    view.setTextColor(Color.GRAY);
                } else if (date.equals(today)) {
                    view.setTextColor(Color.parseColor("#388E3C"));
                } else {
                    view.setTextColor(Color.GRAY);
                }
                view.setTextSize(16);
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCompletion.setAdapter(adapter);
        spinnerCompletion.setSelection(0);
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
        String completion = apiValuesCurrent[selectedIndex];

        Date date = new Date(selectedDateMillis);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String datePart = df.format(date);
        String timePart = String.format(Locale.getDefault(), "%02d:%02d:00", selectedHour, selectedMinute);
        String scheduledTime = datePart + "T" + timePart;

        if (cbWatering.isChecked()) requests.add(makeReq("WATERING", scheduledTime, completion));
        if (cbFertilizing.isChecked()) requests.add(makeReq("FERTILIZING", scheduledTime, completion));
        if (cbPruning.isChecked()) requests.add(makeReq("PRUNING", scheduledTime, completion));
        if (cbMisting.isChecked()) requests.add(makeReq("MIST", scheduledTime, completion));
        if (cbOther.isChecked()) requests.add(makeReq("OTHER", scheduledTime, completion));

        if (!cbWatering.isChecked() && !cbFertilizing.isChecked() && !cbPruning.isChecked()
                && !cbMisting.isChecked() && !cbOther.isChecked()
                && !etNote.getText().toString().trim().isEmpty()) {
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
            String gardenName = spinnerGarden.getSelectedItem().toString();
            long triggerTimeMillis = selectedDateMillis + selectedHour * 3600000L + selectedMinute * 60000L;

            List<String> actions = new ArrayList<>();
            if (cbWatering.isChecked()) actions.add("tưới cây");
            if (cbFertilizing.isChecked()) actions.add("bón phân");
            if (cbPruning.isChecked()) actions.add("tỉa lá");
            if (cbMisting.isChecked()) actions.add("phun ẩm");
            if (cbOther.isChecked()) actions.add("hoạt động khác");
            if (actions.isEmpty()) actions.add("ghi chú");

            String actionText = String.join(" và ", actions);
            scheduleWorkNotification(gardenName, actionText, triggerTimeMillis);

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

    private void scheduleWorkNotification(String gardenName, String type, long triggerTimeMillis) {
        long delay = triggerTimeMillis - System.currentTimeMillis();
        if (delay < 0) delay = 0; // tránh lỗi nếu người dùng chọn giờ quá khứ

        Data data = new Data.Builder()
                .putString("gardenName", gardenName)
                .putString("type", type)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInputData(data)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(this).enqueue(workRequest);
    }
}