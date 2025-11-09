package com.example.planforplant.DTO;

import com.google.gson.annotations.SerializedName;

public class DiaryResponse {
    private Long id;

    // private Long gardenId;

    @SerializedName("entryTime")
    private String entryTime;

    @SerializedName("content")
    private String content;

    // --- Getter ---
    public Long getId() { return id; }
    public String getEntryTime() { return entryTime; }
    public String getContent() { return content; }

    // --- (Nếu cần Setter cho Gson/Retrofit) ---
    // Ví dụ:
    public void setId(Long id) { this.id = id; }
    public void setEntryTime(String entryTime) { this.entryTime = entryTime; }
    public void setContent(String content) { this.content = content; }
}