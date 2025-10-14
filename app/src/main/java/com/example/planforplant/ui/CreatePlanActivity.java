package com.example.planforplant.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.planforplant.DTO.GardenResponse;
import com.example.planforplant.DTO.GardenScheduleRequest;
import com.example.planforplant.DTO.GardenScheduleResponse;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class CreatePlanActivity extends AppCompatActivity {

    private Spinner spinnerPlant, spinnerCompletion;
    private TimePicker timePicker;
    private CalendarView calendarView;
    private RadioGroup radioGroup;
    private LinearLayout layoutWatering, layoutFertilizing, layoutPruningInputs;
    private EditText etWater, etFertilizerType, etFertilizerAmount, etPruningNote, etNote;
    private Button btnCreatePlan;

    private ApiService api;
    private Calendar selectedDateTime;
    private ArrayAdapter<String> completionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.care_plant);

        // --- Init views ---
        TextView tabHistory = findViewById(R.id.tab_history);
        TextView tabPlan = findViewById(R.id.tab_plan);

// highlight current tab (Kế hoạch)
        tabPlan.setBackgroundResource(R.drawable.bg_tab_selected);
        tabPlan.setTextColor(getColor(R.color.white));

        tabHistory.setBackgroundResource(R.drawable.bg_tab_unselected);
        tabHistory.setTextColor(getColor(R.color.text_secondary));

// when user clicks "Lịch sử"
        tabHistory.setOnClickListener(v -> {
            Intent intent = new Intent(CreatePlanActivity.this, ScheduleHistoryActivity.class);
            startActivity(intent);
            finish(); // optional: close current activity
        });


        spinnerPlant = findViewById(R.id.spinnerPlant);
        spinnerCompletion = findViewById(R.id.spinnerCompletion);
        timePicker = findViewById(R.id.timePickerAction);
        calendarView = findViewById(R.id.calendarView);

        radioGroup = findViewById(R.id.radioActivityGroup);
        layoutWatering = findViewById(R.id.layoutWateringInputs);
        layoutFertilizing = findViewById(R.id.layoutFertilizingInputs);
        layoutPruningInputs = findViewById(R.id.layoutPruningInputs);

        etWater = findViewById(R.id.etWaterDuration);
        etFertilizerType = findViewById(R.id.etFertilizerType);
        etFertilizerAmount = findViewById(R.id.etFertilizerAmount);
        etPruningNote = findViewById(R.id.etPruningNote);
        etNote = findViewById(R.id.etNote);
        btnCreatePlan = findViewById(R.id.btnCreatePlan);

        api = ApiClient.getLocalClient(this).create(ApiService.class);
        selectedDateTime = Calendar.getInstance();

        // --- Calendar ---
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDateTime.set(Calendar.YEAR, year);
            selectedDateTime.set(Calendar.MONTH, month);
            selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            updateCompletionSpinner();
        });

        // --- TimePicker ---
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener((picker, hour, minute) -> {
            selectedDateTime.set(Calendar.HOUR_OF_DAY, hour);
            selectedDateTime.set(Calendar.MINUTE, minute);
        });

        // --- Load garden list ---
        loadGardenList();

        // --- RadioGroup dynamic input ---
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            layoutWatering.setVisibility(View.GONE);
            layoutFertilizing.setVisibility(View.GONE);
            layoutPruningInputs.setVisibility(View.GONE);

            if (checkedId == R.id.rbWatering) {
                layoutWatering.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.rbFertilizer) {
                layoutFertilizing.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.rbPruning) {
                layoutPruningInputs.setVisibility(View.VISIBLE);
            }
            // rbOther (MISTING) uses only etNote
        });

        // --- Completion spinner initial setup ---
        updateCompletionSpinner();

        // --- Create plan button ---
        btnCreatePlan.setOnClickListener(v -> createSchedule());
    }

    // --- Load gardens ---
    private void loadGardenList() {
        api.getMyGarden().enqueue(new Callback<List<GardenResponse>>() {
            @Override
            public void onResponse(Call<List<GardenResponse>> call, Response<List<GardenResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GardenResponse> gardens = response.body();
                    List<String> names = new ArrayList<>();
                    for (GardenResponse g : gardens) names.add(g.getNickname());

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            CreatePlanActivity.this,
                            android.R.layout.simple_spinner_item,
                            names
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerPlant.setAdapter(adapter);
                    spinnerPlant.setTag(gardens);
                }
            }

            @Override
            public void onFailure(Call<List<GardenResponse>> call, Throwable t) {
                Toast.makeText(CreatePlanActivity.this, "Lỗi tải danh sách vườn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Update completion spinner based on date ---
    private void updateCompletionSpinner() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        List<String> options = new ArrayList<>();
        if (selectedDateTime.before(today)) {
            // Past date → Skipped / Complete
            options.add("Skipped");
            options.add("Complete");
            spinnerCompletion.setEnabled(true);
        } else {
            // Today or future → Not Done
            options.add("NotDone");
            spinnerCompletion.setEnabled(false);
        }

        completionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        completionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCompletion.setAdapter(completionAdapter);
    }

    // --- Create schedule ---
    private void createSchedule() {
        if (spinnerPlant.getSelectedItem() == null) {
            Toast.makeText(this, "Vui lòng chọn cây", Toast.LENGTH_SHORT).show();
            return;
        }

        List<GardenResponse> gardens = (List<GardenResponse>) spinnerPlant.getTag();
        final GardenResponse selectedGarden = gardens.get(spinnerPlant.getSelectedItemPosition());
        final Long gardenId = selectedGarden.getId();

        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        selectedDateTime.set(Calendar.HOUR_OF_DAY, hour);
        selectedDateTime.set(Calendar.MINUTE, minute);


        String activityType = getSelectedActivity();
        if (activityType == null) {
            Toast.makeText(this, "Vui lòng chọn hoạt động", Toast.LENGTH_SHORT).show();
            return;
        }

        final String finalActivityType = activityType;
        final String note = etNote.getText().toString();
        Double water = null, fertilizerAmount = null;
        String fertilizerType = null;

        if (finalActivityType.equals("WATERING")) {
            String w = etWater.getText().toString();
            if (!w.isEmpty()) water = Double.parseDouble(w);
        } else if (finalActivityType.equals("FERTILIZING")) {
            String type = etFertilizerType.getText().toString();
            String amount = etFertilizerAmount.getText().toString();
            if (!type.isEmpty()) fertilizerType = type;
            if (!amount.isEmpty()) fertilizerAmount = Double.parseDouble(amount);
        }

        final Double finalWater = water;
        final String finalFertilizerType = fertilizerType;
        final Double finalFertilizerAmount = fertilizerAmount;

        // Format date-time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        final String scheduledTime = sdf.format(selectedDateTime.getTime());

        // Determine completion status from spinner
        final String completionStatus = (String) spinnerCompletion.getSelectedItem();

        // Check existing schedule
        api.checkScheduleExists(gardenId, scheduledTime).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean exists = response.body();
                    if (exists) {
                        new AlertDialog.Builder(CreatePlanActivity.this)
                                .setTitle("Schedule Exists")
                                .setMessage("A schedule already exists at this time. Overwrite or pick another time?")
                                .setPositiveButton("Overwrite", (dialog, which) ->
                                        sendCreateRequest(gardenId, finalActivityType, scheduledTime, note,
                                                finalWater, finalFertilizerType, finalFertilizerAmount, completionStatus))
                                .setNegativeButton("Pick another time", (dialog, which) -> dialog.dismiss())
                                .show();
                    } else {
                        sendCreateRequest(gardenId, finalActivityType, scheduledTime, note,
                                finalWater, finalFertilizerType, finalFertilizerAmount, completionStatus);
                    }
                } else {
                    Toast.makeText(CreatePlanActivity.this, "Failed to check schedule", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Toast.makeText(CreatePlanActivity.this, "Server error when checking schedule", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Send create request ---
    private void sendCreateRequest(Long gardenId, String activityType, String scheduledTime, String note,
                                   Double water, String fertilizerType, Double fertilizerAmount, String completionStatus) {
        GardenScheduleRequest request = new GardenScheduleRequest();
        request.setGardenId(gardenId);
        request.setType(activityType);
        request.setScheduledTime(scheduledTime);
        request.setNote(note);
        request.setWaterAmount(water);
        request.setFertilityType(fertilizerType);
        request.setFertilityAmount(fertilizerAmount);
        request.setCompletion(completionStatus); // send spinner value

        api.createSchedule(request).enqueue(new Callback<GardenScheduleResponse>() {
            @Override
            public void onResponse(Call<GardenScheduleResponse> call, Response<GardenScheduleResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreatePlanActivity.this, "Tạo kế hoạch thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CreatePlanActivity.this, "Tạo thất bại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GardenScheduleResponse> call, Throwable t) {
                Toast.makeText(CreatePlanActivity.this, "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Get selected activity ---
    private String getSelectedActivity() {
        int checkedId = radioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.rbWatering) return "WATERING";
        if (checkedId == R.id.rbFertilizer) return "FERTILIZING";
        if (checkedId == R.id.rbPruning) return "PRUNING";
        if (checkedId == R.id.rbOther) return "MISTING";
        return null;
    }
}