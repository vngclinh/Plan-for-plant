package com.example.planforplant.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Disease {

    private long id;
    private String name;
    private String scientificName;
    private String description;
    private String symptoms;
    private String causes;
    private String careguide;
    private int priority;
    private String imageUrl;

    // From ManyToMany with Plant
    @SerializedName("plants")
    private List<Plant> plants;

    public long getId() { return id; }
    public String getName() { return name; }
    public String getScientificName() { return scientificName; }
    public String getDescription() { return description; }
    public String getSymptoms() { return symptoms; }
    public String getCauses() { return causes; }
    public String getCareguide() { return careguide; }
    public int getPriority() { return priority; }
    public String getImageUrl() { return imageUrl; }
    public List<Plant> getPlants() { return plants; }



    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setScientificName(String scientificName) { this.scientificName = scientificName; }
    public void setDescription(String description) { this.description = description; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    public void setCauses(String causes) { this.causes = causes; }
    public void setCareguide(String careguide) { this.careguide = careguide; }
    public void setPriority(int priority) { this.priority = priority; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setPlants(List<Plant> plants) { this.plants = plants; }
}