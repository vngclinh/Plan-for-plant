package com.example.planforplant.ui;

public class StatsPoint {
    private String label;
    private int value;

    public StatsPoint(String label, int value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public int getValue() {
        return value;
    }
}
