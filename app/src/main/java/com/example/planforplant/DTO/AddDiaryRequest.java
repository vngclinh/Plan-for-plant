// File: AddDiaryRequest.java
package com.example.planforplant.DTO;

import com.google.gson.annotations.SerializedName;

public class AddDiaryRequest {
    @SerializedName("content")
    private String content;

    // Có thể thêm trường này nếu cần gửi thời gian tùy chỉnh
    // @SerializedName("entryTime")
    // private String entryTime;

    public AddDiaryRequest(String content) {
        this.content = content;
    }

}