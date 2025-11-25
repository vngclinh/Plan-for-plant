package com.example.planforplant.DTO;

import com.example.planforplant.model.Plant;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class GardenResponse {

    private Long id;
    private String nickname;

    @SerializedName("plant")
    private Plant plant;

    @SerializedName("dateAdded")
    private String dateAdded;  // LocalDateTime → String

    private String status;     // Enum → String
    private String type;       // Enum → String
    private String potType;    // Enum → String

    @SerializedName("diseases")
    private List<GardenDiseaseResponse> diseases;

    @SerializedName("diseaseStatuses")
    private Map<Long, String> diseaseStatuses; // Enum → String

    @SerializedName("diseaseNames")
    private List<String> diseaseNames;

    @SerializedName("detectedDates")
    private Map<Long, String> detectedDates;   // LocalDateTime → String

    @SerializedName("diaries")
    private List<DiaryResponse> diaries;

    // ---------- GETTERS & SETTERS ----------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public Plant getPlant() { return plant; }
    public void setPlant(Plant plant) { this.plant = plant; }

    public String getDateAdded() { return dateAdded; }
    public void setDateAdded(String dateAdded) { this.dateAdded = dateAdded; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPotType() { return potType; }
    public void setPotType(String potType) { this.potType = potType; }

    public List<GardenDiseaseResponse> getDiseases() { return diseases; }
    public void setDiseases(List<GardenDiseaseResponse> diseases) { this.diseases = diseases; }

    public Map<Long, String> getDiseaseStatuses() { return diseaseStatuses; }
    public void setDiseaseStatuses(Map<Long, String> diseaseStatuses) { this.diseaseStatuses = diseaseStatuses; }

    public List<String> getDiseaseNames() { return diseaseNames; }
    public void setDiseaseNames(List<String> diseaseNames) { this.diseaseNames = diseaseNames; }

    public Map<Long, String> getDetectedDates() { return detectedDates; }
    public void setDetectedDates(Map<Long, String> detectedDates) { this.detectedDates = detectedDates; }

    public List<DiaryResponse> getDiaries() { return diaries; }
    public void setDiaries(List<DiaryResponse> diaries) { this.diaries = diaries; }
}
