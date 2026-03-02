package com.example.planforplant.model;

import com.example.planforplant.DTO.GardenScheduleResponse;

import java.util.List;

public class HourGroup {
    private String hour;
    private List<GardenScheduleResponse> schedules;

    public HourGroup(String hour, List<GardenScheduleResponse> schedules) {
        this.hour = hour;
        this.schedules = schedules;
    }

    public String getHour() { return hour; }
    public List<GardenScheduleResponse> getSchedules() { return schedules; }
}
