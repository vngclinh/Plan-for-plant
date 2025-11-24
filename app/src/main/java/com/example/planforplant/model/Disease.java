package com.example.planforplant.model;

public class Disease {
    private long id;
    private String name;
    private String scientificName;
    private String description;
    private String symptoms;
    private String causes;
    private String careguide;
    private String imageUrl;

    // --- Getters & Setters ---
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public  String getScientificName(){return scientificName;}
    public  void setScientificName(String scientificName) {this.scientificName=scientificName;}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getCauses() { return causes; }
    public void setCauses(String causes) { this.causes = causes; }

    public String getCareguide() { return careguide; }
    public void setCareguide(String careguide) { this.careguide = careguide; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

}
