package com.example.planforplant.DTO;

public class GardenScheduleRequest {
    private Long gardenId;
    private String type;              // “WATERING”, “FERTILIZING”, etc.
    private String scheduledTime;     // ISO format: "2025-10-08T09:30:00"
    private String note;
    private Double waterAmount;
    private Double fertilityAmount;
    private String fertilityType;

    private String completion;

    public String getCompletion() {
        return completion;
    }


    // ======= Getters =======
    public Long getGardenId() {
        return gardenId;
    }

    public String getType() {
        return type;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public String getNote() {
        return note;
    }

    public Double getWaterAmount() {
        return waterAmount;
    }

    public Double getFertilityAmount() {
        return fertilityAmount;
    }

    public String getFertilityType() {
        return fertilityType;
    }

    // ======= Setters =======
    public void setGardenId(Long gardenId) {
        this.gardenId = gardenId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setWaterAmount(Double waterAmount) {
        this.waterAmount = waterAmount;
    }

    public void setFertilityAmount(Double fertilityAmount) {
        this.fertilityAmount = fertilityAmount;
    }

    public void setFertilityType(String fertilityType) {
        this.fertilityType = fertilityType;
    }

    public void setCompletion(String completion) {
        this.completion = completion;
    }
}
