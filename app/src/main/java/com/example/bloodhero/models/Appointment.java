package com.example.bloodhero.models;

public class Appointment {
    public enum Status {
        SCHEDULED,
        COMPLETED,
        CANCELLED,
        NO_SHOW
    }

    private String id;
    private String userId;
    private String campaignId;
    private String campaignName;
    private String location;
    private String date;
    private String timeSlot;
    private Status status;
    private long createdAt;

    public Appointment() {}

    public Appointment(String id, String userId, String campaignId, String campaignName,
                      String location, String date, String timeSlot) {
        this.id = id;
        this.userId = userId;
        this.campaignId = campaignId;
        this.campaignName = campaignName;
        this.location = location;
        this.date = date;
        this.timeSlot = timeSlot;
        this.status = Status.SCHEDULED;
        this.createdAt = System.currentTimeMillis();
    }

    public Appointment(String id, String userId, String campaignId, String campaignName,
                      String location, String date, String timeSlot, Status status) {
        this.id = id;
        this.userId = userId;
        this.campaignId = campaignId;
        this.campaignName = campaignName;
        this.location = location;
        this.date = date;
        this.timeSlot = timeSlot;
        this.status = status;
        this.createdAt = System.currentTimeMillis();
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

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    
    // Alias for getTimeSlot
    public String getTime() { return timeSlot; }
    public void setTime(String time) { this.timeSlot = time; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public void cancel() {
        this.status = Status.CANCELLED;
    }

    public void complete() {
        this.status = Status.COMPLETED;
    }

    public boolean isUpcoming() {
        return status == Status.SCHEDULED;
    }
}