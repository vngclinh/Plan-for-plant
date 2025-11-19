package com.example.planforplant.DTO;

import com.example.planforplant.model.Plant;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class GardenResponse {
    private Long id;
    private String nickname;

    @SerializedName("plant")
    private Plant plant;

    @SerializedName("dateAdded")
    private String dateAdded;

    private String status;
    private String type;
    private String potType;

    @SerializedName("diseases")
    private List<GardenDiseaseResponse> diseases;

    @SerializedName("diseaseStatuses")
    private Map<Long, String> diseaseStatuses;
    @SerializedName("diseaseNames")
    private List<String> diseaseNames;
    @SerializedName("dateAdded")
    private String dateAdded;
    @SerializedName("diaries")
    private List<DiaryResponse> diaries;

    @SerializedName("detectedDates")
    private Map<Long, String> detectedDates;

    @SerializedName("diaries")
    private List<DiaryResponse> diaries;

    // --- Getters ---
    public Long getId() { return id; }
    public String getNickname() { return nickname; }
    public Plant getPlant() { return plant; }
    public String getDateAdded() { return dateAdded; }
    public String getStatus() { return status; }
    public String getType() { return type; }
    public String getPotType() { return potType; }
    public List<String> getDiseaseNames() { return diseaseNames; }
    public List<DiaryResponse> getDiaries() { return diaries; }
    public void setDiaries(List<DiaryResponse> diaries) { this.diaries = diaries; }
}

    public List<GardenDiseaseResponse> getDiseases() { return diseases; }
    public Map<Long, String> getDiseaseStatuses() { return diseaseStatuses; }
    public Map<Long, String> getDetectedDates() { return detectedDates; }
    public List<DiaryResponse> getDiaries() { return diaries; }
}