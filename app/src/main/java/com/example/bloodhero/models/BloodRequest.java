package com.example.bloodhero.models;

public class BloodRequest {
    private String id;
    private String patientName;
    private String bloodType;
    private String hospital;
    private String city;
    private String urgencyLevel; // "critical", "urgent", "normal"
    private int unitsNeeded;
    private String contactPhone;
    private String postedTime;
    private boolean isActive;

    public BloodRequest(String id, String patientName, String bloodType, String hospital,
                        String city, String urgencyLevel, int unitsNeeded, 
                        String contactPhone, String postedTime) {
        this.id = id;
        this.patientName = patientName;
        this.bloodType = bloodType;
        this.hospital = hospital;
        this.city = city;
        this.urgencyLevel = urgencyLevel;
        this.unitsNeeded = unitsNeeded;
        this.contactPhone = contactPhone;
        this.postedTime = postedTime;
        this.isActive = true;
    }

    // Getters
    public String getId() { return id; }
    public String getPatientName() { return patientName; }
    public String getBloodType() { return bloodType; }
    public String getHospital() { return hospital; }
    public String getCity() { return city; }
    public String getUrgencyLevel() { return urgencyLevel; }
    public int getUnitsNeeded() { return unitsNeeded; }
    public String getContactPhone() { return contactPhone; }
    public String getPostedTime() { return postedTime; }
    public boolean isActive() { return isActive; }

    // Setters
    public void setActive(boolean active) { isActive = active; }
}
