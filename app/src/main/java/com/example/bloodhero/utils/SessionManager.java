package com.example.bloodhero.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager - manages current logged-in user session
 * Uses SharedPreferences only for current session (userId)
 * All user data comes from SQLite
 */
public class SessionManager {
    private static final String PREF_NAME = "BloodHeroSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_IS_ADMIN = "is_admin";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private static SessionManager instance;

    private SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Create login session
     */
    public void createLoginSession(String userId, boolean isAdmin) {
        editor.clear();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putBoolean(KEY_IS_ADMIN, isAdmin);
        editor.apply();
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get current user ID
     */
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        return prefs.getBoolean(KEY_IS_ADMIN, false);
    }

    /**
     * Logout user
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }

    /**
     * Clear session (alias for logout)
     */
    public void clearSession() {
        logout();
    }
}
