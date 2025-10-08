package com.example.planforplant.DTO;

public class GardenScheduleResponse {
    private Long id;
    private Long gardenId;
    private String gardenNickname;
    private String type;
    private String scheduledTime;
    private String note;
    private Double waterAmount;
    private Double fertilityAmount;
    private String fertilityType;

    private String completion;

    // ======= Getters =======
    public Long getId() {
        return id;
    }

    public Long getGardenId() {
        return gardenId;
    }

    public String getGardenNickname() {
        return gardenNickname;
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

    public String getCompletion() {
        return completion;
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
    public void setId(Long id) {
        this.id = id;
    }

    public void setGardenId(Long gardenId) {
        this.gardenId = gardenId;
    }

    public void setGardenNickname(String gardenNickname) {
        this.gardenNickname = gardenNickname;
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