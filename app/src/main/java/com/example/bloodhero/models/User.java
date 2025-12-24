package com.example.bloodhero.models;

public class User {
    private String id;
    private String email;
    private String password;
    private String name;
    private String bloodType;
    private String location;
    private String phoneNumber;
    private String dateOfBirth;
    private String gender;
    private Double weight; // in kg
    private int totalDonations;
    private int totalPoints;
    private String profileImageUrl;
    private long createdAt;
    private long lastDonationDate; // Timestamp of last blood donation
    private String securityQuestion; // First security question for password recovery
    private String securityAnswer; // Hashed answer for first security question
    private String securityQuestion2; // Second security question for password recovery
    private String securityAnswer2; // Hashed answer for second security question

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

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

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

    public long getLastDonationDate() { return lastDonationDate; }
    public void setLastDonationDate(long lastDonationDate) { this.lastDonationDate = lastDonationDate; }

    public String getSecurityQuestion() { return securityQuestion; }
    public void setSecurityQuestion(String securityQuestion) { this.securityQuestion = securityQuestion; }

    public String getSecurityAnswer() { return securityAnswer; }
    public void setSecurityAnswer(String securityAnswer) { this.securityAnswer = securityAnswer; }

    public String getSecurityQuestion2() { return securityQuestion2; }
    public void setSecurityQuestion2(String securityQuestion2) { this.securityQuestion2 = securityQuestion2; }

    public String getSecurityAnswer2() { return securityAnswer2; }
    public void setSecurityAnswer2(String securityAnswer2) { this.securityAnswer2 = securityAnswer2; }

    /**
     * Check if user can donate blood now based on last donation date.
     * Users must wait 56 days (8 weeks) between whole blood donations.
     * @return true if eligible to donate, false otherwise
     */
    public boolean canDonateNow() {
        if (lastDonationDate == 0) {
            return true; // Never donated before
        }
        
        long daysSinceLastDonation = (System.currentTimeMillis() - lastDonationDate) / (1000 * 60 * 60 * 24);
        return daysSinceLastDonation >= 56; // 8 weeks = 56 days
    }

    /**
     * Get the number of days remaining until eligible to donate again.
     * @return days remaining, or 0 if already eligible
     */
    public int getDaysUntilEligible() {
        if (canDonateNow()) {
            return 0;
        }
        
        long daysSinceLastDonation = (System.currentTimeMillis() - lastDonationDate) / (1000 * 60 * 60 * 24);
        return (int) (56 - daysSinceLastDonation);
    }

    public int getLivesSaved() {
        return totalDonations * 3;
    }
}