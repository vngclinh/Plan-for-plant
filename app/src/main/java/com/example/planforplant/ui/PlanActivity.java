package com.example.planforplant.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;
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

    private TimePicker timePickerAction;
    private RadioGroup radioActivityGroup;
    private LinearLayout layoutWatering, layoutFertilizing, layoutPruning;
    private EditText etWaterAmount, etFertilizerType, etFertilizerAmount, etPruningNote, etNote;
    private Button btnCreatePlan;
    private GardenScheduleApi scheduleApi;

    private final Long gardenId = 1L; // Hardcoded garden ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.care_plant);

        // Ánh xạ View
        timePickerAction = findViewById(R.id.timePickerAction);
        radioActivityGroup = findViewById(R.id.radioActivityGroup);
        layoutWatering = findViewById(R.id.layoutWateringInputs);
        layoutFertilizing = findViewById(R.id.layoutFertilizingInputs);
        layoutPruning = findViewById(R.id.layoutPruningInputs);
        etWaterAmount = findViewById(R.id.etWaterAmount);
        etFertilizerType = findViewById(R.id.etFertilizerType);
        etFertilizerAmount = findViewById(R.id.etFertilizerAmount);
        etPruningNote = findViewById(R.id.etPruningNote);
        etNote = findViewById(R.id.etNote);
        btnCreatePlan = findViewById(R.id.btnCreatePlan);

        scheduleApi = ApiClient.getLocalClient(this).create(GardenScheduleApi.class);
        timePickerAction.setIs24HourView(true);

        radioActivityGroup.setOnCheckedChangeListener((group, checkedId) -> {
            layoutWatering.setVisibility(View.GONE);
            layoutFertilizing.setVisibility(View.GONE);
            layoutPruning.setVisibility(View.GONE);

            if (checkedId == R.id.rbWatering) {
                layoutWatering.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.rbFertilizing) {
                layoutFertilizing.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.rbPruning) {
                layoutPruning.setVisibility(View.VISIBLE);
            }
        });

        btnCreatePlan.setOnClickListener(v -> submitPlan());
    }

    private void submitPlan() {
        int checkedId = radioActivityGroup.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(this, "Vui lòng chọn một công việc", Toast.LENGTH_SHORT).show();
            return;
        }

        GardenScheduleRequest request = new GardenScheduleRequest();
        request.setGardenId(gardenId);
        request.setNote(etNote.getText().toString());

        if (checkedId == R.id.rbWatering) {
            request.setType("WATERING");
            try {
                request.setWaterAmount(Double.parseDouble(etWaterAmount.getText().toString()));
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Vui lòng nhập lượng nước hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (checkedId == R.id.rbFertilizing) {
            request.setType("FERTILIZER");
            request.setFertilityType(etFertilizerType.getText().toString());
            try {
                request.setFertilityAmount(Double.parseDouble(etFertilizerAmount.getText().toString()));
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Vui lòng nhập lượng phân bón hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (checkedId == R.id.rbPruning) {
            request.setType("PRUNING");
            // Ghi chú tỉa lá có thể được thêm vào ghi chú chung
            request.setNote(etPruningNote.getText().toString() + ". " + etNote.getText().toString());
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, timePickerAction.getHour());
        calendar.set(Calendar.MINUTE, timePickerAction.getMinute());
        calendar.set(Calendar.SECOND, 0);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        request.setScheduledTime(sdf.format(calendar.getTime()));

        sendSchedule(request);
    }

    private void sendSchedule(GardenScheduleRequest request) {
        Log.d("PlanActivity", "Gửi kế hoạch: " + request.getType() + " | time=" + request.getScheduledTime());

        scheduleApi.createSchedule(request).enqueue(new Callback<GardenScheduleResponse>() {
            @Override
            public void onResponse(Call<GardenScheduleResponse> call, Response<GardenScheduleResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PlanActivity.this, "Đã lưu kế hoạch thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(PlanActivity.this, "Lưu kế hoạch không thành công!", Toast.LENGTH_SHORT).show();
                    Log.e("PlanActivity", "API Error: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<GardenScheduleResponse> call, Throwable t) {
                Toast.makeText(PlanActivity.this, "Không thể kết nối tới server!", Toast.LENGTH_LONG).show();
                Log.e("PlanActivity", "API Failure: " + t.getMessage(), t);
            }
        });
    }
}
