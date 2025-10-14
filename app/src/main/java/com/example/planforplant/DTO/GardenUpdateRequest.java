package com.example.planforplant.DTO;

import java.util.List;

public class GardenUpdateRequest {
    private String nickname;
    private String status;    // String để Retrofit tự serialize enum
    private String type;
    private String potType;
    private List<Long> diseaseIds;

    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
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

    public List<Long> getDiseaseIds() {
        return diseaseIds;
    }
    public void setDiseaseIds(List<Long> diseaseIds) {
        this.diseaseIds = diseaseIds;
    }
}
