package com.example.planforplant.DTO;

public class AddGardenRequest {
    private Long plantId;
    private String nickname;
    private String type;
    private String potType;

    // --- Constructors ---
    public AddGardenRequest() {}

    public AddGardenRequest(Long plantId, String nickname, String type, String potType) {
        this.plantId = plantId;
        this.nickname = nickname;
        this.type = type;
        this.potType = potType;
    }

    // --- Getters & Setters ---
    public Long getPlantId() {
        return plantId;
    }

    public void setPlantId(Long plantId) {
        this.plantId = plantId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPotType() {
        return potType;
    }

    public void setPotType(String potType) {
        this.potType = potType;
    }
}
