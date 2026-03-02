package com.example.planforplant.DTO;

public class UpdateUserRequest {

    private String fullname;
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private Double lat;
    private Double lon;

    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String fullname, String username, String password, String email,
                             String phoneNumber, String avatarUrl, Double lat, Double lon) {
        this.fullname = fullname;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
        this.lat = lat;
        this.lon = lon;
    }

    public UpdateUserRequest(Double lat, Double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    // Getters and setters
    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }
}