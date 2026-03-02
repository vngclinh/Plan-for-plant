package com.example.planforplant.DTO;

import java.io.Serializable;

/**
 * Dữ liệu phản hồi cho kế hoạch chăm sóc cây.
 * ⚙️ Được implement Serializable để truyền qua Intent giữa Activity mà không bị crash.
 */
public class GardenScheduleResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long gardenId;
    private String gardenNickname;
    private Long plantId;
    private String plantName;
    private String type;
    private String scheduledTime;  // e.g., "2025-10-13T14:00:00"
    private String completion;
    private String note;
    private Double waterAmount;
    private Double fertilityAmount;
    private String fertilityType;

    private String fungicideType;


    private String createdAt;
    private String updatedAt;

    // --- Getters ---
    public Long getId() {
        return id;
    }

    public Long getGardenId() {
        return gardenId;
    }

    public String getGardenNickname() {
        return gardenNickname;
    }

    public Long getPlantId() {
        return plantId;
    }

    public String getPlantName() {
        return plantName;
    }

    public String getType() {
        return type;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public String getCompletion() {
        return completion;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    // --- Setters ---
    public void setId(Long id) {
        this.id = id;
    }

    public void setGardenId(Long gardenId) {
        this.gardenId = gardenId;
    }

    public void setGardenNickname(String gardenNickname) {
        this.gardenNickname = gardenNickname;
    }

    public void setPlantId(Long plantId) {
        this.plantId = plantId;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public void setCompletion(String completion) {
        this.completion = completion;
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

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getFungicideType() {
        return fungicideType;
    }

    public void setFungicideType(String fungicideType) {
        this.fungicideType = fungicideType;
    }
}
