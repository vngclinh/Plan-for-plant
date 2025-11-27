package com.example.planforplant.DTO;

public class UpdateGardenDiseaseRequest {

    private Long gardenDiseaseId;
    private String status;
    private String note;
    private String detectedDate;
    private String curedDate;

    public UpdateGardenDiseaseRequest() {
    }

    public UpdateGardenDiseaseRequest(Long gardenDiseaseId, String status, String note, String detectedDate, String curedDate) {
        this.gardenDiseaseId = gardenDiseaseId;
        this.status = status;
        this.note = note;
        this.detectedDate = detectedDate;
        this.curedDate = curedDate;
    }

    public Long getGardenDiseaseId() {
        return gardenDiseaseId;
    }

    public void setGardenDiseaseId(Long gardenDiseaseId) {
        this.gardenDiseaseId = gardenDiseaseId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDetectedDate() {
        return detectedDate;
    }

    public void setDetectedDate(String detectedDate) {
        this.detectedDate = detectedDate;
    }

    public String getCuredDate() {
        return curedDate;
    }

    public void setCuredDate(String curedDate) {
        this.curedDate = curedDate;
    }
}