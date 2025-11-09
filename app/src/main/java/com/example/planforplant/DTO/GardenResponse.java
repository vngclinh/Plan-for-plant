package com.example.planforplant.DTO;

import com.example.planforplant.model.Plant;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GardenResponse {
    private Long id;
    private String nickname;

    @SerializedName("plant")
    private Plant plant;

    private String status;
    private String type;
    private String potType;

    @SerializedName("diseaseNames")
    private List<String> diseaseNames;
    @SerializedName("dateAdded")
    private String dateAdded;
    @SerializedName("diaries")
    private List<DiaryResponse> diaries;

    // --- Getter ---
    public Long getId() { return id; }
    public String getNickname() { return nickname; }
    public String getDateAdded() {return dateAdded; }
    public Plant getPlant() { return plant; }
    public String getStatus() { return status; }
    public String getType() { return type; }
    public String getPotType() { return potType; }
    public List<String> getDiseaseNames() { return diseaseNames; }
    public List<DiaryResponse> getDiaries() { return diaries; }
    public void setDiaries(List<DiaryResponse> diaries) { this.diaries = diaries; }
}
