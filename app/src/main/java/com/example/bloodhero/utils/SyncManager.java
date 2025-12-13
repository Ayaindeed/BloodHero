package com.example.bloodhero.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * SyncManager handles real-time synchronization between admin and donator spaces.
 * Ensures data consistency across both platforms.
 */
public class SyncManager {
    
    private static final String TAG = "SyncManager";
    private static final String PREFS_NAME = "BloodHeroPrefs";
    private static final String SYNC_PREFS = "BloodHeroSync";
    
    /**
     * Sync all user data from central storage to local preferences.
     * Call this on activity resume to ensure fresh data.
     * 
     * @param context Application context
     * @return true if sync was successful and data changed
     */
    public static boolean syncUserData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String email = prefs.getString("user_email", "");
        
        if (email.isEmpty() || "admin@contact.me".equals(email)) {
            return false;
        }
        
        UserStorage.UserData userData = UserStorage.getUserByEmail(context, email);
        if (userData == null) {
            return false;
        }
        
        // Compare and update if different
        int localPoints = prefs.getInt("total_points", 0);
        int localDonations = prefs.getInt("total_donations", 0);
        
        boolean dataChanged = false;
        SharedPreferences.Editor editor = prefs.edit();
        
        if (userData.points != localPoints) {
            editor.putInt("total_points", userData.points);
            editor.putInt("user_points", userData.points);
            dataChanged = true;
            Log.d(TAG, "Synced points: " + localPoints + " -> " + userData.points);
        }
        
        if (userData.donations != localDonations) {
            editor.putInt("total_donations", userData.donations);
            editor.putInt("user_donations", userData.donations);
            dataChanged = true;
            Log.d(TAG, "Synced donations: " + localDonations + " -> " + userData.donations);
        }
        
        if (userData.name != null && !userData.name.equals(prefs.getString("user_name", ""))) {
            editor.putString("user_name", userData.name);
            dataChanged = true;
        }
        
        if (userData.bloodType != null && !"Unknown".equals(userData.bloodType)) {
            String localBloodType = prefs.getString("blood_type", "");
            if (!userData.bloodType.equals(localBloodType)) {
                editor.putString("blood_type", userData.bloodType);
                dataChanged = true;
            }
        }
        
        if (userData.phone != null && !userData.phone.isEmpty()) {
            editor.putString("user_phone", userData.phone);
        }
        
        if (userData.location != null && !userData.location.isEmpty()) {
            editor.putString("user_location", userData.location);
        }
        
        // Sync per-user preferences (updated by admin)
        syncFromPerUserPrefs(context, email, editor);
        
        if (dataChanged) {
            editor.apply();
            recordSyncTimestamp(context);
        }
        
        return dataChanged;
    }
    
    /**
     * Sync badges and other data from per-user SharedPreferences.
     * This data is updated when admin marks donations as complete.
     */
    private static void syncFromPerUserPrefs(Context context, String email, 
            SharedPreferences.Editor mainEditor) {
        String sanitizedEmail = email.replace("@", "_at_").replace(".", "_dot_");
        SharedPreferences donorPrefs = context.getSharedPreferences(
                "user_data_" + sanitizedEmail, Context.MODE_PRIVATE);
        
        if (!donorPrefs.getBoolean("data_updated", false)) {
            return;
        }
        
        // Sync badges
        String[] badges = {
            "badge_first_drop", "badge_regular_donor", "badge_lifesaver",
            "badge_hero_status", "badge_marathon_donor", "badge_blood_legend",
            "badge_platinum_donor"
        };
        
        for (String badge : badges) {
            if (donorPrefs.getBoolean(badge, false)) {
                mainEditor.putBoolean(badge, true);
            }
        }
    }
    
    /**
     * Record the last sync timestamp for debugging/monitoring.
     */
    private static void recordSyncTimestamp(Context context) {
        SharedPreferences syncPrefs = context.getSharedPreferences(SYNC_PREFS, Context.MODE_PRIVATE);
        syncPrefs.edit()
            .putLong("last_sync_time", System.currentTimeMillis())
            .apply();
    }
    
    /**
     * Get the last sync time.
     */
    public static long getLastSyncTime(Context context) {
        SharedPreferences syncPrefs = context.getSharedPreferences(SYNC_PREFS, Context.MODE_PRIVATE);
        return syncPrefs.getLong("last_sync_time", 0);
    }
    
    /**
     * Force a full sync of user data.
     * Use this when user explicitly requests a refresh.
     */
    public static void forceSync(Context context) {
        syncUserData(context);
    }
    
    /**
     * Push local changes to central storage.
     * Called when user updates their profile locally.
     */
    public static void pushLocalChanges(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String email = prefs.getString("user_email", "");
        
        if (email.isEmpty()) {
            return;
        }
        
        String name = prefs.getString("user_name", "");
        String bloodType = prefs.getString("blood_type", "");
        String phone = prefs.getString("user_phone", "");
        String location = prefs.getString("user_location", "");
        
        // Update central storage
        UserStorage.saveUser(context, name, email, bloodType, phone, location);
        
        Log.d(TAG, "Pushed local changes for: " + email);
    }
    
    /**
     * Sync appointment data for a specific user.
     * Returns the list of user's appointments with fresh status.
     */
    public static java.util.List<UserStorage.AppointmentData> syncUserAppointments(
            Context context, String email) {
        java.util.List<UserStorage.AppointmentData> all = UserStorage.getAllAppointments(context);
        java.util.List<UserStorage.AppointmentData> userAppointments = new java.util.ArrayList<>();
        
        for (UserStorage.AppointmentData appt : all) {
            if (appt.userEmail.equals(email)) {
                userAppointments.add(appt);
            }
        }
        
        return userAppointments;
    }
    
    /**
     * Check if there are any pending sync operations.
     */
    public static boolean hasPendingSync(Context context) {
        SharedPreferences syncPrefs = context.getSharedPreferences(SYNC_PREFS, Context.MODE_PRIVATE);
        return syncPrefs.getBoolean("pending_sync", false);
    }
    
    /**
     * Mark a sync operation as pending (for offline-first capability).
     */
    public static void markSyncPending(Context context, String operation) {
        SharedPreferences syncPrefs = context.getSharedPreferences(SYNC_PREFS, Context.MODE_PRIVATE);
        String queue = syncPrefs.getString("sync_queue", "");
        queue += operation + ";";
        syncPrefs.edit()
            .putBoolean("pending_sync", true)
            .putString("sync_queue", queue)
            .apply();
    }
    
    /**
     * Process any pending sync operations (for offline-first capability).
     */
    public static void processPendingSync(Context context) {
        SharedPreferences syncPrefs = context.getSharedPreferences(SYNC_PREFS, Context.MODE_PRIVATE);
        String queue = syncPrefs.getString("sync_queue", "");
        
        if (queue.isEmpty()) {
            syncPrefs.edit().putBoolean("pending_sync", false).apply();
            return;
        }
        
        // Process queue (in a real app, this would handle network operations)
        String[] operations = queue.split(";");
        for (String op : operations) {
            if (!op.isEmpty()) {
                Log.d(TAG, "Processing pending operation: " + op);
            }
        }
        
        // Clear queue
        syncPrefs.edit()
            .putBoolean("pending_sync", false)
            .putString("sync_queue", "")
            .apply();
    }
}
