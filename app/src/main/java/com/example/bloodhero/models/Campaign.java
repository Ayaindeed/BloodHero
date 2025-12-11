package com.example.bloodhero.models;

import java.util.List;

public class Campaign {
    private String id;
    private String name;
    private String organizer;
    private String location;
    private String date;
    private String time;
    private double distance;
    private List<String> bloodTypesNeeded;
    private String description;

    public Campaign() {}

    public Campaign(String id, String name, String organizer, String location, 
                   String date, String time, double distance, 
                   List<String> bloodTypesNeeded, String description) {
        this.id = id;
        this.name = name;
        this.organizer = organizer;
        this.location = location;
        this.date = date;
        this.time = time;
        this.distance = distance;
        this.bloodTypesNeeded = bloodTypesNeeded;
        this.description = description;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public List<String> getBloodTypesNeeded() { return bloodTypesNeeded; }
    public void setBloodTypesNeeded(List<String> bloodTypesNeeded) { this.bloodTypesNeeded = bloodTypesNeeded; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBloodTypesString() {
        if (bloodTypesNeeded == null || bloodTypesNeeded.isEmpty()) {
            return "All Types";
        }
        return String.join("  ", bloodTypesNeeded);
    }

    public String getDistanceString() {
        return String.format("%.1f km", distance);
    }
}