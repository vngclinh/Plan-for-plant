package com.example.planforplant.ui;

import com.example.planforplant.DTO.GardenScheduleResponse;
import java.util.List;

public class GroupedSchedule {
    private String time;
    private List<GardenScheduleResponse> items;

    public GroupedSchedule(String time, List<GardenScheduleResponse> items) {
        this.time = time;
        this.items = items;
    }

    public String getTime() {
        return time;
    }

    public List<GardenScheduleResponse> getItems() {
        return items;
    }
}
