package com.example.planforplant.ui;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.planforplant.DTO.GardenDiseaseResponse;
import com.example.planforplant.DTO.UpdateGardenDiseaseRequest;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GardenDiseaseAdapter extends RecyclerView.Adapter<GardenDiseaseAdapter.DiseaseViewHolder> {

    private List<GardenDiseaseResponse> list = new ArrayList<>();
    private final Context context;

    public GardenDiseaseAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<GardenDiseaseResponse> data) {
        this.list = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DiseaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.garden_disease, parent, false);
        return new DiseaseViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DiseaseViewHolder holder, int position) {
        GardenDiseaseResponse d = list.get(position);

        holder.tvDiseaseName.setText("🦠 " + d.getDiseaseName());

        if (d.getDetectedDate() != null && d.getDetectedDate().length() >= 10) {
            holder.tvDiseaseDate.setText("Detected: " + d.getDetectedDate().substring(0, 10));
        } else {
            holder.tvDiseaseDate.setText("Detected: Unknown");
        }

        // Remove old listener
        holder.chkCured.setOnCheckedChangeListener(null);

        boolean isCured = "CURED".equalsIgnoreCase(d.getStatus());
        boolean isActive = "ACTIVE".equalsIgnoreCase(d.getStatus());

        holder.chkCured.setChecked(isCured);
        holder.chkCured.setEnabled(isActive);

        applyCheckboxStyle(holder.chkCured, d.getStatus());

        holder.chkCured.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String newStatus = isChecked ? "CURED" : "ACTIVE";

            // Revert UI until API succeeds
            buttonView.setOnCheckedChangeListener(null);
            buttonView.setChecked(!isChecked);
            buttonView.setOnCheckedChangeListener((bv, chk) -> {});

            updateGardenDiseaseStatus(d, newStatus, holder.getAdapterPosition(), buttonView);
        });
    }

    private void applyCheckboxStyle(CheckBox checkbox, String status) {
        if ("CURED".equalsIgnoreCase(status)) {
            checkbox.setTextColor(Color.parseColor("#388E3C")); // Green
        } else {
            checkbox.setTextColor(Color.parseColor("#D32F2F")); // Red
        }
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void updateGardenDiseaseStatus(GardenDiseaseResponse gd, String newStatus,
                                           int position, CompoundButton buttonView) {

        UpdateGardenDiseaseRequest req = new UpdateGardenDiseaseRequest();

        // FIX: Must send gardenDiseaseId, NOT diseaseId
        req.setGardenDiseaseId(gd.getGardenDiseaseId());
        req.setStatus(newStatus);
        req.setNote("Cập nhật từ GardenDetail");

        if ("CURED".equalsIgnoreCase(newStatus)) {
            req.setCuredDate(getCurrentDateTime());
        }

        ApiService api = ApiClient.getLocalClient(buttonView.getContext()).create(ApiService.class);

        api.updateDisease(req).enqueue(new Callback<GardenDiseaseResponse>() {
            @Override
            public void onResponse(Call<GardenDiseaseResponse> call, Response<GardenDiseaseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // Update local model
                    gd.setStatus(newStatus);

                    // Refresh item
                    notifyItemChanged(position);

                    Toast.makeText(buttonView.getContext(),
                            "Cập nhật thành công!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(buttonView.getContext(),
                            "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GardenDiseaseResponse> call, Throwable t) {
                Toast.makeText(buttonView.getContext(),
                        "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class DiseaseViewHolder extends RecyclerView.ViewHolder {

        TextView tvDiseaseName, tvDiseaseDate;
        CheckBox chkCured;

        public DiseaseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDiseaseName = itemView.findViewById(R.id.tvDiseaseName);
            tvDiseaseDate = itemView.findViewById(R.id.tvDiseaseDate);
            chkCured = itemView.findViewById(R.id.chkCured);
        }
    }
}