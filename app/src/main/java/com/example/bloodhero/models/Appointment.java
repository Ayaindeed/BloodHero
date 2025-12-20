package com.example.bloodhero.models;

public class Appointment {
    public enum Status {
        SCHEDULED,      // Initial booking
        CONFIRMED,      // Admin confirmed
        CHECKED_IN,     // QR code scanned at center
        IN_PROGRESS,    // Assigned to bed, donation in progress
        PENDING_VERIFICATION, // Donation done, awaiting code verification
        COMPLETED,      // Code verified, donation confirmed
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
    private Long checkedInAt;
    private String verificationCode; // 4-character code for donation verification
    private Integer bedNumber; // Bed number (1-4) when IN_PROGRESS

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

    public Long getCheckedInAt() { return checkedInAt; }
    public void setCheckedInAt(Long checkedInAt) { this.checkedInAt = checkedInAt; }

    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }

    public Integer getBedNumber() { return bedNumber; }
    public void setBedNumber(Integer bedNumber) { this.bedNumber = bedNumber; }

    public void cancel() {
        this.status = Status.CANCELLED;
    }

    public void checkIn() {
        this.status = Status.CHECKED_IN;
        this.checkedInAt = System.currentTimeMillis();
    }

    public void complete() {
        this.status = Status.COMPLETED;
    }

    public void assignToBed(int bedNumber) {
        this.status = Status.IN_PROGRESS;
        this.bedNumber = bedNumber;
    }

    public void setPendingVerification(String code) {
        this.status = Status.PENDING_VERIFICATION;
        this.verificationCode = code;
    }

    public boolean verifyCode(String enteredCode) {
        return verificationCode != null && verificationCode.equalsIgnoreCase(enteredCode);
    }

    public boolean isUpcoming() {
        return status == Status.SCHEDULED;
    }
    
    public boolean isCheckedIn() {
        return status == Status.CHECKED_IN;
    }

    public boolean isInProgress() {
        return status == Status.IN_PROGRESS;
    }

    public boolean isPendingVerification() {
        return status == Status.PENDING_VERIFICATION;
    }
}