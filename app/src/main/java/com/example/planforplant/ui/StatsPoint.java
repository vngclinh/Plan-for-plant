package com.example.planforplant.ui;

public class StatsPoint {
    private String label;
    private int count;

    public StatsPoint(String label, int count) {
        this.label = label;
        this.count = count;
    }

    public String getLabel() {
        return label;
    }

    public int getCount() {
        return count;
    }
}

