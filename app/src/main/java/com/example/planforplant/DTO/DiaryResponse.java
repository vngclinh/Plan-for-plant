package com.example.planforplant.DTO;

import com.google.gson.annotations.SerializedName;

public class DiaryResponse {

    private Long id;

    @SerializedName("gardenId")
    private Long gardenId;

    @SerializedName("entryTime")
    private String entryTime;

    private String content;

    public Long getId() { return id; }
    public Long getGardenId() { return gardenId; }
    public String getEntryTime() { return entryTime; }
    public String getContent() { return content; }
}