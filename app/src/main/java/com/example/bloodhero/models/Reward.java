package com.example.bloodhero.models;

public class Reward {
    private String id;
    private String name;
    private String description;
    private String partnerName;
    private int pointsCost;
    private int iconResId;
    private String category; // "food", "shopping", "health", "entertainment"
    private boolean isRedeemed;
    private String expiryDate;

    public Reward(String id, String name, String description, String partnerName, 
                  int pointsCost, int iconResId, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.partnerName = partnerName;
        this.pointsCost = pointsCost;
        this.iconResId = iconResId;
        this.category = category;
        this.isRedeemed = false;
        this.expiryDate = "";
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPartnerName() { return partnerName; }
    public int getPointsCost() { return pointsCost; }
    public int getIconResId() { return iconResId; }
    public String getCategory() { return category; }
    public boolean isRedeemed() { return isRedeemed; }
    public String getExpiryDate() { return expiryDate; }

    // Setters
    public void setRedeemed(boolean redeemed) { isRedeemed = redeemed; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
}
