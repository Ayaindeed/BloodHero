package com.example.bloodhero.models;

public class Donation {
    private String id;
    private String userId;
    private String campaignId;
    private String campaignName;
    private String location;
    private String date;
    private String bloodType;
    private int pointsEarned;
    private String status;
    private DonationDetails details;

    public Donation() {}

    public Donation(String id, String userId, String campaignName, String location,
                   String date, String bloodType, int pointsEarned) {
        this.id = id;
        this.userId = userId;
        this.campaignName = campaignName;
        this.location = location;
        this.date = date;
        this.bloodType = bloodType;
        this.pointsEarned = pointsEarned;
        this.status = "COMPLETED";
    }

    public Donation(String id, String userId, String campaignId, String campaignName, 
                   String location, String date, String bloodType, int pointsEarned, String status) {
        this.id = id;
        this.userId = userId;
        this.campaignId = campaignId;
        this.campaignName = campaignName;
        this.location = location;
        this.date = date;
        this.bloodType = bloodType;
        this.pointsEarned = pointsEarned;
        this.status = status;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }

    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }

    public int getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(int pointsEarned) { this.pointsEarned = pointsEarned; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public DonationDetails getDetails() { return details; }
    public void setDetails(DonationDetails details) { this.details = details; }

    // Inner class for donation health details
    public static class DonationDetails {
        private String pressure;
        private String hemoglobin;
        private int pulse;
        private String antibodyTest;

        public DonationDetails() {}

        public DonationDetails(String pressure, String hemoglobin, int pulse, String antibodyTest) {
            this.pressure = pressure;
            this.hemoglobin = hemoglobin;
            this.pulse = pulse;
            this.antibodyTest = antibodyTest;
        }

        public String getPressure() { return pressure; }
        public void setPressure(String pressure) { this.pressure = pressure; }

        public String getHemoglobin() { return hemoglobin; }
        public void setHemoglobin(String hemoglobin) { this.hemoglobin = hemoglobin; }

        public int getPulse() { return pulse; }
        public void setPulse(int pulse) { this.pulse = pulse; }

        public String getAntibodyTest() { return antibodyTest; }
        public void setAntibodyTest(String antibodyTest) { this.antibodyTest = antibodyTest; }
    }
}