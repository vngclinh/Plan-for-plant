package com.example.planforplant.DTO;

public class UserResponse {

    private Long id;
    private String fullname;
    private String username;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private Double lat;
    private Double lon;

    private String level;         // "MAM", "TRUONG_THANH", "CO_THU"
    private int streak;           // số ngày streak
    private String lastStreakDate; // "2025-11-27"

    public UserResponse() {
    }

    public UserResponse(Long id, String fullname, String username, String email,
                        String phoneNumber, String avatarUrl, Double lat, Double lon,
                        String level, int streak, String lastStreakDate) {
        this.id = id;
        this.fullname = fullname;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
        this.lat = lat;
        this.lon = lon;
        this.level = level;
        this.streak = streak;
        this.lastStreakDate = lastStreakDate;
    }

    // getters / setters cho tất cả field

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLon() { return lon; }
    public void setLon(Double lon) { this.lon = lon; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }

    public String getLastStreakDate() { return lastStreakDate; }
    public void setLastStreakDate(String lastStreakDate) { this.lastStreakDate = lastStreakDate; }
}
