package com.example.planforplant.model;

import java.util.List;

public class Plant {
    private Long id;
    private String commonName;
    private String scientificName;
    private String phylum;
    private String plantClass;      // matches @Column(name = "class")
    private String plantOrder;      // matches @Column(name = "\"order\"")
    private String family;
    private String genus;
    private String species;
    private String description;
    private String waterSchedule;
    private String light;
    private String temperature;
    private String careguide;       // ✅ lowercase 'careguide' to match BE field name
    private String imageUrl;

    // If you also return diseases in JSON from backend, include this:
    private List<Disease> diseases;

    // --- Getters and Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getPhylum() {
        return phylum;
    }

    public void setPhylum(String phylum) {
        this.phylum = phylum;
    }

    public String getPlantClass() {
        return plantClass;
    }

    public void setPlantClass(String plantClass) {
        this.plantClass = plantClass;
    }

    public String getPlantOrder() {
        return plantOrder;
    }

    public void setPlantOrder(String plantOrder) {
        this.plantOrder = plantOrder;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getGenus() {
        return genus;
    }

    public void setGenus(String genus) {
        this.genus = genus;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWaterSchedule() {
        return waterSchedule;
    }

    public void setWaterSchedule(String waterSchedule) {
        this.waterSchedule = waterSchedule;
    }

    public String getLight() {
        return light;
    }

    public void setLight(String light) {
        this.light = light;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getCareguide() {
        return careguide;
    }

    public void setCareguide(String careguide) {
        this.careguide = careguide;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<Disease> getDiseases() {
        return diseases;
    }

    public void setDiseases(List<Disease> diseases) {
        this.diseases = diseases;
    }
}