package com.example.planforplant.DTO;

import com.google.gson.annotations.SerializedName;

public class GardenDiseaseResponse {

    @SerializedName("diseaseId")
    private Long diseaseId;

    @SerializedName("gardenDiseaseId")
    private Long gardenDiseaseId;

    @SerializedName("name")   // backend field name
    private String diseaseName;

    @SerializedName("status")
    private String status;

    @SerializedName("detectedDate")
    private String detectedDate;

    @SerializedName("curedDate")
    private String curedDate;

    public Long getDiseaseId() { return diseaseId; }
    public String getDiseaseName() { return diseaseName; }
    public String getStatus() { return status; }
    public String getDetectedDate() { return detectedDate; }
    public String getCuredDate(){return curedDate;}
    public Long getGardenDiseaseId(){return gardenDiseaseId;}

    public void setStatus(String status) {
        this.status = status;
    }

}