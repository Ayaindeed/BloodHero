package com.example.bloodhero.models;

public class LeaderboardEntry {
    private int rank;
    private String userId;
    private String userName;
    private String profileImageUrl;
    private int totalPoints;
    private int totalDonations;
    private boolean isCurrentUser;

    public LeaderboardEntry() {}

    public LeaderboardEntry(int rank, String userId, String userName, 
                           int totalPoints, int totalDonations) {
        this.rank = rank;
        this.userId = userId;
        this.userName = userName;
        this.totalPoints = totalPoints;
        this.totalDonations = totalDonations;
        this.isCurrentUser = false;
    }

    public LeaderboardEntry(int rank, String userId, String userName, 
                           String profileImageUrl, int totalPoints, int totalDonations) {
        this.rank = rank;
        this.userId = userId;
        this.userName = userName;
        this.profileImageUrl = profileImageUrl;
        this.totalPoints = totalPoints;
        this.totalDonations = totalDonations;
        this.isCurrentUser = false;
    }

    // Getters and Setters
    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public int getTotalDonations() { return totalDonations; }
    public void setTotalDonations(int totalDonations) { this.totalDonations = totalDonations; }

    public boolean isCurrentUser() { return isCurrentUser; }
    public void setCurrentUser(boolean currentUser) { isCurrentUser = currentUser; }

    public String getRankDisplay() {
        switch (rank) {
            case 1: return "🥇";
            case 2: return "🥈";
            case 3: return "🥉";
            default: return String.valueOf(rank);
        }
    }
}