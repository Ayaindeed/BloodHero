package com.example.bloodhero.models;

public class User {
    private String id;
    private String email;
    private String password;
    private String name;
    private String bloodType;
    private String location;
    private String phoneNumber;
    private int totalDonations;
    private int totalPoints;
    private String profileImageUrl;
    private long createdAt;

    public User() {}

    public User(String id, String email, String name, String bloodType) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.bloodType = bloodType;
        this.totalDonations = 0;
        this.totalPoints = 0;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public int getTotalDonations() { return totalDonations; }
    public void setTotalDonations(int totalDonations) { this.totalDonations = totalDonations; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
    
    // Alias for getTotalPoints (for consistency)
    public int getPoints() { return totalPoints; }
    public void setPoints(int points) { this.totalPoints = points; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setCreatedAt(String createdAt) { 
        try {
            this.createdAt = Long.parseLong(createdAt);
        } catch (NumberFormatException e) {
            this.createdAt = System.currentTimeMillis();
        }
    }

    public int getLivesSaved() {
        return totalDonations * 3;
    }
}