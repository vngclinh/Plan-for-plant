package com.example.planforplant.DTO;

import java.util.List;

public class AddDiseasesRequest {

    private Long gardenId;
    private List<Long> diseaseIds;

    public Long getGardenId() {
        return gardenId;
    }

    public void setGardenId(Long gardenId) {
        this.gardenId = gardenId;
    }

    public List<Long> getDiseaseIds() {
        return diseaseIds;
    }

    public void setDiseaseIds(List<Long> diseaseIds) {
        this.diseaseIds = diseaseIds;
    }
}