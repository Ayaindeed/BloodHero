package com.example.bloodhero.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Simple user storage using SharedPreferences to sync registered users
 * In a real app, this would be a database or backend API
 */
public class UserStorage {

    private static final String PREFS_NAME = "BloodHeroUsers";
    private static final String KEY_USERS = "registered_users";
    private static final String KEY_APPOINTMENTS = "all_appointments";
    private static final String KEY_DONATIONS = "completed_donations";

    /**
     * Clear login session (logout user)
     */
    public static void clearSession(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("BloodHeroPrefs", Context.MODE_PRIVATE);
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .putBoolean("is_admin", false)
            .remove("user_email")
            .apply();
    }

    /**
     * Update user's points in central storage
     */
    public static void updateUserPoints(Context context, String email, int newPoints) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String usersJson = prefs.getString(KEY_USERS, "[]");
        
        try {
            JSONArray usersArray = new JSONArray(usersJson);
            for (int i = 0; i < usersArray.length(); i++) {
                JSONObject user = usersArray.getJSONObject(i);
                if (user.getString("email").equals(email)) {
                    user.put("points", newPoints);
                    prefs.edit().putString(KEY_USERS, usersArray.toString()).apply();
                    return;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save a new registered user with extended info
     */
    public static void saveUser(Context context, String name, String email, String bloodType) {
        saveUser(context, name, email, bloodType, null, null);
    }

    /**
     * Save a new registered user with full profile data
     */
    public static void saveUser(Context context, String name, String email, String bloodType, 
                                String phone, String location) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String usersJson = prefs.getString(KEY_USERS, "[]");
        
        try {
            JSONArray usersArray = new JSONArray(usersJson);
            
            // Check if user already exists
            for (int i = 0; i < usersArray.length(); i++) {
                JSONObject user = usersArray.getJSONObject(i);
                if (user.getString("email").equals(email)) {
                    // User exists, update info
                    user.put("name", name);
                    if (bloodType != null && !bloodType.isEmpty()) {
                        user.put("bloodType", bloodType);
                    }
                    if (phone != null && !phone.isEmpty()) {
                        user.put("phone", phone);
                    }
                    if (location != null && !location.isEmpty()) {
                        user.put("location", location);
                    }
                    prefs.edit().putString(KEY_USERS, usersArray.toString()).apply();
                    return;
                }
            }
            
            // Add new user with timestamp
            JSONObject newUser = new JSONObject();
            newUser.put("id", UUID.randomUUID().toString());
            newUser.put("name", name);
            newUser.put("email", email);
            newUser.put("bloodType", bloodType != null ? bloodType : "Unknown");
            newUser.put("phone", phone != null ? phone : "");
            newUser.put("location", location != null ? location : "");
            newUser.put("donations", 0);
            newUser.put("points", 0);
            newUser.put("verified", false);
            newUser.put("registeredAt", System.currentTimeMillis());
            
            usersArray.put(newUser);
            prefs.edit().putString(KEY_USERS, usersArray.toString()).apply();
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update user donation count and verify status
     */
    public static void updateUserDonations(Context context, String email, int donations, boolean verified) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String usersJson = prefs.getString(KEY_USERS, "[]");
        
        try {
            JSONArray usersArray = new JSONArray(usersJson);
            
            for (int i = 0; i < usersArray.length(); i++) {
                JSONObject user = usersArray.getJSONObject(i);
                if (user.getString("email").equals(email)) {
                    user.put("donations", donations);
                    user.put("verified", verified);
                    prefs.edit().putString(KEY_USERS, usersArray.toString()).apply();
                    return;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all registered users
     */
    public static List<UserData> getAllUsers(Context context) {
        List<UserData> users = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String usersJson = prefs.getString(KEY_USERS, "[]");
        
        try {
            JSONArray usersArray = new JSONArray(usersJson);
            
            for (int i = 0; i < usersArray.length(); i++) {
                JSONObject userObj = usersArray.getJSONObject(i);
                int donations = userObj.optInt("donations", 0);
                int points = userObj.optInt("points", donations * 50); // Default to 50 points per donation
                UserData user = new UserData(
                    userObj.optString("id", String.valueOf(i)),
                    userObj.optString("name", "Unknown"),
                    userObj.optString("email", ""),
                    userObj.optString("bloodType", "Unknown"),
                    userObj.optString("phone", ""),
                    userObj.optString("location", ""),
                    donations,
                    points,
                    userObj.optBoolean("verified", false),
                    userObj.optLong("registeredAt", System.currentTimeMillis())
                );
                users.add(user);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return users;
    }

    /**
     * Get user by email
     */
    public static UserData getUserByEmail(Context context, String email) {
        List<UserData> users = getAllUsers(context);
        for (UserData user : users) {
            if (user.email.equals(email)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Get user count
     */
    public static int getUserCount(Context context) {
        return getAllUsers(context).size();
    }

    /**
     * Get total donations across all users
     */
    public static int getTotalDonations(Context context) {
        List<UserData> users = getAllUsers(context);
        int total = 0;
        for (UserData user : users) {
            total += user.donations;
        }
        return total;
    }

    /**
     * Get blood type statistics
     */
    public static java.util.Map<String, Integer> getBloodTypeStats(Context context) {
        java.util.Map<String, Integer> stats = new java.util.LinkedHashMap<>();
        stats.put("O+", 0);
        stats.put("O-", 0);
        stats.put("A+", 0);
        stats.put("A-", 0);
        stats.put("B+", 0);
        stats.put("B-", 0);
        stats.put("AB+", 0);
        stats.put("AB-", 0);
        
        List<UserData> users = getAllUsers(context);
        for (UserData user : users) {
            if (user.bloodType != null && stats.containsKey(user.bloodType)) {
                stats.put(user.bloodType, stats.get(user.bloodType) + 1);
            }
        }
        
        return stats;
    }

    // ==================== APPOINTMENT STORAGE ====================

    /**
     * Save a new appointment and return the appointment ID
     */
    public static String saveAppointment(Context context, String userEmail, String userName, 
                                        String bloodType, String campaignName, String location, 
                                        String date, String time) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String appointmentsJson = prefs.getString(KEY_APPOINTMENTS, "[]");
        String appointmentId = UUID.randomUUID().toString();
        
        try {
            JSONArray appointmentsArray = new JSONArray(appointmentsJson);
            
            JSONObject newAppointment = new JSONObject();
            newAppointment.put("id", appointmentId);
            newAppointment.put("userEmail", userEmail);
            newAppointment.put("userName", userName);
            newAppointment.put("bloodType", bloodType != null ? bloodType : "Unknown");
            newAppointment.put("campaignName", campaignName);
            newAppointment.put("location", location);
            newAppointment.put("date", date);
            newAppointment.put("time", time);
            newAppointment.put("status", "Pending"); // Pending, Confirmed, Completed, Cancelled
            newAppointment.put("createdAt", System.currentTimeMillis());
            
            appointmentsArray.put(newAppointment);
            prefs.edit().putString(KEY_APPOINTMENTS, appointmentsArray.toString()).apply();
            
            return appointmentId;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get all appointments
     */
    public static List<AppointmentData> getAllAppointments(Context context) {
        List<AppointmentData> appointments = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String appointmentsJson = prefs.getString(KEY_APPOINTMENTS, "[]");
        
        try {
            JSONArray appointmentsArray = new JSONArray(appointmentsJson);
            
            for (int i = 0; i < appointmentsArray.length(); i++) {
                JSONObject apptObj = appointmentsArray.getJSONObject(i);
                AppointmentData appointment = new AppointmentData(
                    apptObj.optString("id", String.valueOf(i)),
                    apptObj.optString("userEmail", ""),
                    apptObj.optString("userName", "Unknown"),
                    apptObj.optString("bloodType", "Unknown"),
                    apptObj.optString("campaignName", ""),
                    apptObj.optString("location", ""),
                    apptObj.optString("date", ""),
                    apptObj.optString("time", ""),
                    apptObj.optString("status", "Pending"),
                    apptObj.optLong("createdAt", System.currentTimeMillis())
                );
                appointments.add(appointment);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return appointments;
    }

    /**
     * Get appointments by status
     */
    public static List<AppointmentData> getAppointmentsByStatus(Context context, String status) {
        List<AppointmentData> all = getAllAppointments(context);
        List<AppointmentData> filtered = new ArrayList<>();
        for (AppointmentData appt : all) {
            if (appt.status.equals(status)) {
                filtered.add(appt);
            }
        }
        return filtered;
    }

    /**
     * Update appointment status
     */
    public static void updateAppointmentStatus(Context context, String appointmentId, String newStatus) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String appointmentsJson = prefs.getString(KEY_APPOINTMENTS, "[]");
        
        try {
            JSONArray appointmentsArray = new JSONArray(appointmentsJson);
            
            for (int i = 0; i < appointmentsArray.length(); i++) {
                JSONObject appt = appointmentsArray.getJSONObject(i);
                if (appt.getString("id").equals(appointmentId)) {
                    appt.put("status", newStatus);
                    prefs.edit().putString(KEY_APPOINTMENTS, appointmentsArray.toString()).apply();
                    
                    // If completed, increment user donation count
                    if (newStatus.equals("Completed")) {
                        String email = appt.getString("userEmail");
                        incrementUserDonation(context, email);
                    }
                    return;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Increment user donation count and add points
     */
    private static void incrementUserDonation(Context context, String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String usersJson = prefs.getString(KEY_USERS, "[]");
        
        try {
            JSONArray usersArray = new JSONArray(usersJson);
            
            for (int i = 0; i < usersArray.length(); i++) {
                JSONObject user = usersArray.getJSONObject(i);
                if (user.getString("email").equals(email)) {
                    int currentDonations = user.optInt("donations", 0);
                    int currentPoints = user.optInt("points", 0);
                    int newDonationCount = currentDonations + 1;
                    user.put("donations", newDonationCount);
                    user.put("points", currentPoints + 50); // Award 50 points per donation
                    user.put("verified", true); // Verify after first donation
                    prefs.edit().putString(KEY_USERS, usersArray.toString()).apply();
                    
                    // Store the updated user data in a per-user preferences file
                    // This ensures the donor sees their updates when they log back in
                    String sanitizedEmail = email.replace("@", "_at_").replace(".", "_dot_");
                    android.content.SharedPreferences donorPrefs = context.getSharedPreferences(
                            "user_data_" + sanitizedEmail, Context.MODE_PRIVATE);
                    donorPrefs.edit()
                        .putInt("user_points", currentPoints + 50)
                        .putInt("user_donations", newDonationCount)
                        .putInt("total_donations", newDonationCount)
                        .putBoolean("data_updated", true)
                        .apply();
                    
                    // Also update current user's SharedPreferences if it's the logged-in user
                    android.content.SharedPreferences userPrefs = context.getSharedPreferences("BloodHeroPrefs", Context.MODE_PRIVATE);
                    String currentEmail = userPrefs.getString("user_email", "");
                    if (currentEmail.equals(email)) {
                        userPrefs.edit()
                            .putInt("user_points", currentPoints + 50)
                            .putInt("user_donations", newDonationCount)
                            .putInt("total_donations", newDonationCount)
                            .apply();
                        
                        // Check and store newly unlocked badges
                        checkAndUnlockBadges(context, newDonationCount, userPrefs);
                    } else {
                        // Unlock badges for the donor even if not logged in
                        checkAndUnlockBadges(context, newDonationCount, donorPrefs);
                    }
                    return;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check and unlock badges based on donation count
     */
    private static void checkAndUnlockBadges(Context context, int donationCount, 
                                              android.content.SharedPreferences prefs) {
        int badgePoints = 0;
        StringBuilder unlockedBadges = new StringBuilder();
        
        // First Drop - 1 donation
        if (donationCount == 1) {
            if (!prefs.getBoolean("badge_first_drop", false)) {
                prefs.edit().putBoolean("badge_first_drop", true).apply();
                badgePoints += 50;
                unlockedBadges.append("First Drop,");
            }
        }
        
        // Regular Donor - 5 donations
        if (donationCount == 5) {
            if (!prefs.getBoolean("badge_regular_donor", false)) {
                prefs.edit().putBoolean("badge_regular_donor", true).apply();
                badgePoints += 100;
                unlockedBadges.append("Regular Donor,");
            }
            if (!prefs.getBoolean("badge_lifesaver", false)) {
                prefs.edit().putBoolean("badge_lifesaver", true).apply();
                badgePoints += 250;
                unlockedBadges.append("Lifesaver,");
            }
        }
        
        // Hero Status - 10 donations
        if (donationCount == 10) {
            if (!prefs.getBoolean("badge_hero_status", false)) {
                prefs.edit().putBoolean("badge_hero_status", true).apply();
                badgePoints += 200;
                unlockedBadges.append("Hero Status,");
            }
        }
        
        // Marathon Donor - 25 donations
        if (donationCount == 25) {
            if (!prefs.getBoolean("badge_marathon_donor", false)) {
                prefs.edit().putBoolean("badge_marathon_donor", true).apply();
                badgePoints += 500;
                unlockedBadges.append("Marathon Donor,");
            }
        }
        
        // Blood Legend - 50 donations
        if (donationCount == 50) {
            if (!prefs.getBoolean("badge_blood_legend", false)) {
                prefs.edit().putBoolean("badge_blood_legend", true).apply();
                badgePoints += 1000;
                unlockedBadges.append("Blood Legend,");
            }
        }
        
        // Platinum Donor - 100 donations
        if (donationCount == 100) {
            if (!prefs.getBoolean("badge_platinum_donor", false)) {
                prefs.edit().putBoolean("badge_platinum_donor", true).apply();
                badgePoints += 2000;
                unlockedBadges.append("Platinum Donor,");
            }
        }
        
        // Add badge points to total
        if (badgePoints > 0) {
            int currentPoints = prefs.getInt("user_points", 0);
            prefs.edit()
                .putInt("user_points", currentPoints + badgePoints)
                .putString("last_unlocked_badges", unlockedBadges.toString())
                .apply();
        }
    }

    // ==================== DONATION TRACKING ====================

    /**
     * Save a completed donation record
     */
    public static void saveDonation(Context context, String donationId, String userEmail, 
                                     String userName, String bloodType, String campaignName,
                                     String location, String date, int pointsEarned) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String donationsJson = prefs.getString(KEY_DONATIONS, "[]");
        
        try {
            JSONArray donationsArray = new JSONArray(donationsJson);
            
            JSONObject donation = new JSONObject();
            donation.put("id", donationId);
            donation.put("userEmail", userEmail);
            donation.put("userName", userName);
            donation.put("bloodType", bloodType);
            donation.put("campaignName", campaignName);
            donation.put("location", location);
            donation.put("date", date);
            donation.put("pointsEarned", pointsEarned);
            donation.put("completedAt", System.currentTimeMillis());
            donation.put("journeyStage", 5); // Completed
            
            donationsArray.put(donation);
            prefs.edit().putString(KEY_DONATIONS, donationsArray.toString()).apply();
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all donations for a user
     */
    public static List<DonationData> getDonationsByEmail(Context context, String email) {
        List<DonationData> donations = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String donationsJson = prefs.getString(KEY_DONATIONS, "[]");
        
        try {
            JSONArray donationsArray = new JSONArray(donationsJson);
            
            for (int i = 0; i < donationsArray.length(); i++) {
                JSONObject donObj = donationsArray.getJSONObject(i);
                if (donObj.optString("userEmail", "").equals(email)) {
                    DonationData donation = new DonationData(
                        donObj.optString("id", ""),
                        donObj.optString("userEmail", ""),
                        donObj.optString("userName", ""),
                        donObj.optString("bloodType", ""),
                        donObj.optString("campaignName", ""),
                        donObj.optString("location", ""),
                        donObj.optString("date", ""),
                        donObj.optInt("pointsEarned", 50),
                        donObj.optInt("journeyStage", 5),
                        donObj.optLong("completedAt", System.currentTimeMillis())
                    );
                    donations.add(donation);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return donations;
    }

    /**
     * Get all donations
     */
    public static List<DonationData> getAllDonations(Context context) {
        List<DonationData> donations = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String donationsJson = prefs.getString(KEY_DONATIONS, "[]");
        
        try {
            JSONArray donationsArray = new JSONArray(donationsJson);
            
            for (int i = 0; i < donationsArray.length(); i++) {
                JSONObject donObj = donationsArray.getJSONObject(i);
                DonationData donation = new DonationData(
                    donObj.optString("id", ""),
                    donObj.optString("userEmail", ""),
                    donObj.optString("userName", ""),
                    donObj.optString("bloodType", ""),
                    donObj.optString("campaignName", ""),
                    donObj.optString("location", ""),
                    donObj.optString("date", ""),
                    donObj.optInt("pointsEarned", 50),
                    donObj.optInt("journeyStage", 5),
                    donObj.optLong("completedAt", System.currentTimeMillis())
                );
                donations.add(donation);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return donations;
    }

    /**
     * Get appointments count by status
     */
    public static int getAppointmentsCountByStatus(Context context, String status) {
        return getAppointmentsByStatus(context, status).size();
    }

    /**
     * Simple user data class
     */
    public static class UserData {
        public String id;
        public String name;
        public String email;
        public String bloodType;
        public String phone;
        public String location;
        public int donations;
        public int points;
        public boolean verified;
        public long registeredAt;

        public UserData(String id, String name, String email, String bloodType, 
                        String phone, String location, int donations, int points, boolean verified, long registeredAt) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.bloodType = bloodType;
            this.phone = phone;
            this.location = location;
            this.donations = donations;
            this.points = points;
            this.verified = verified;
            this.registeredAt = registeredAt;
        }

        // Legacy constructor for compatibility
        public UserData(String name, String email, String bloodType, int donations, boolean verified) {
            this("", name, email, bloodType, "", "", donations, donations * 50, verified, System.currentTimeMillis());
        }
    }

    /**
     * Appointment data class
     */
    public static class AppointmentData {
        public String id;
        public String userEmail;
        public String userName;
        public String bloodType;
        public String campaignName;
        public String location;
        public String date;
        public String time;
        public String status;
        public long createdAt;

        public AppointmentData(String id, String userEmail, String userName, String bloodType,
                               String campaignName, String location, String date, String time,
                               String status, long createdAt) {
            this.id = id;
            this.userEmail = userEmail;
            this.userName = userName;
            this.bloodType = bloodType;
            this.campaignName = campaignName;
            this.location = location;
            this.date = date;
            this.time = time;
            this.status = status;
            this.createdAt = createdAt;
        }

        public String getFormattedDate() {
            try {
                String[] parts = date.split("-");
                String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                int monthIndex = Integer.parseInt(parts[1]) - 1;
                return months[monthIndex] + " " + parts[2] + ", " + parts[0];
            } catch (Exception e) {
                return date;
            }
        }
    }

    /**
     * Donation data class
     */
    public static class DonationData {
        public String id;
        public String userEmail;
        public String userName;
        public String bloodType;
        public String campaignName;
        public String location;
        public String date;
        public int pointsEarned;
        public int journeyStage;
        public long completedAt;

        public DonationData(String id, String userEmail, String userName, String bloodType,
                            String campaignName, String location, String date, int pointsEarned,
                            int journeyStage, long completedAt) {
            this.id = id;
            this.userEmail = userEmail;
            this.userName = userName;
            this.bloodType = bloodType;
            this.campaignName = campaignName;
            this.location = location;
            this.date = date;
            this.pointsEarned = pointsEarned;
            this.journeyStage = journeyStage;
            this.completedAt = completedAt;
        }

        public String getFormattedDate() {
            try {
                String[] parts = date.split("-");
                String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                int monthIndex = Integer.parseInt(parts[1]) - 1;
                return months[monthIndex] + " " + parts[2] + ", " + parts[0];
            } catch (Exception e) {
                return date;
            }
        }
    }
}
