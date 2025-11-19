package com.example.planforplant.DTO;

public class GardenDiseaseResponse {

    private Long diseaseId;
    private String diseaseName;
    private String status;
    private String detectedDate;

    public Long getDiseaseId() { return diseaseId; }
    public String getDiseaseName() { return diseaseName; }
    public String getStatus() { return status; }
    public String getDetectedDate() { return detectedDate; }
}