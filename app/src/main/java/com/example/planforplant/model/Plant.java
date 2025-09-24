package com.example.planforplant.model;

public class Plant {
    private Long id;
    private String commonName;
    private String scientificName;
    private String phylum;
    private String plantClass;
    private String plantOrder;
    private String family;
    private String genus;
    private String species;
    private String description;
    private String waterSchedule;
    private String light;
    private String temperature;
    private String careguide;
    private String imageUrl;

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

    public String getCareGuide() {
        return careguide;
    }

    public void setCareGuide(String careguide) {
        this.careguide = careguide;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}