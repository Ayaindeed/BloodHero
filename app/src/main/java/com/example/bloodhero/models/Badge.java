package com.example.bloodhero.models;

public class Badge {
    private String id;
    private String name;
    private String description;
    private int iconResId;
    private int pointValue;
    private String unlockCriteria;
    private boolean isUnlocked;
    private long unlockedAt;

    public Badge() {}

    public Badge(String id, String name, String description, int iconResId,
                int pointValue, String unlockCriteria) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconResId = iconResId;
        this.pointValue = pointValue;
        this.unlockCriteria = unlockCriteria;
        this.isUnlocked = false;
    }

    public Badge(String id, String name, String description, int iconResId,
                int pointValue, String unlockCriteria, boolean isUnlocked) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconResId = iconResId;
        this.pointValue = pointValue;
        this.unlockCriteria = unlockCriteria;
        this.isUnlocked = isUnlocked;
        if (isUnlocked) {
            this.unlockedAt = System.currentTimeMillis();
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getIconResId() { return iconResId; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }

    public int getPointValue() { return pointValue; }
    public void setPointValue(int pointValue) { this.pointValue = pointValue; }

    public String getUnlockCriteria() { return unlockCriteria; }
    public void setUnlockCriteria(String unlockCriteria) { this.unlockCriteria = unlockCriteria; }

    public boolean isUnlocked() { return isUnlocked; }
    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }

    public long getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(long unlockedAt) { this.unlockedAt = unlockedAt; }

    public void unlock() {
        this.isUnlocked = true;
        this.unlockedAt = System.currentTimeMillis();
    }
}