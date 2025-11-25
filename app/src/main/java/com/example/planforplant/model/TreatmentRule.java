package com.example.planforplant.model;

import com.google.gson.annotations.SerializedName;

public class TreatmentRule {

    private Long id;

    // Avoid recursion (BackReference) — your backend usually ignores disease field in JSON
    @SerializedName("disease")
    private Disease disease;

    private String type;     // ScheduleType enum as STRING
    private int intervalDays;
    private String fungicideType;
    private String description;

    public Long getId() { return id; }
    public Disease getDisease() { return disease; }
    public String getType() { return type; }
    public int getIntervalDays() { return intervalDays; }
    public String getFungicideType() { return fungicideType; }
    public String getDescription() { return description; }

    public void setId(Long id) { this.id = id; }
    public void setDisease(Disease disease) { this.disease = disease; }
    public void setType(String type) { this.type = type; }
    public void setIntervalDays(int intervalDays) { this.intervalDays = intervalDays; }
    public void setFungicideType(String fungicideType) { this.fungicideType = fungicideType; }
    public void setDescription(String description) { this.description = description; }
}