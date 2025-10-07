package com.example.planforplant.model;

public class GardenItem {
    public long id;
    public String dateAdded;
    public String status;
    public Plant plant;

    public static class Plant {
        public String commonName;
        public String imageUrl;
    }
}
