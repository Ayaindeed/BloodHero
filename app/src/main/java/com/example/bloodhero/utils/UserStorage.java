package com.example.bloodhero.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple user storage using SharedPreferences to sync registered users
 * In a real app, this would be a database or backend API
 */
public class UserStorage {

    private static final String PREFS_NAME = "BloodHeroUsers";
    private static final String KEY_USERS = "registered_users";

    /**
     * Save a new registered user
     */
    public static void saveUser(Context context, String name, String email, String bloodType) {
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
                    prefs.edit().putString(KEY_USERS, usersArray.toString()).apply();
                    return;
                }
            }
            
            // Add new user
            JSONObject newUser = new JSONObject();
            newUser.put("name", name);
            newUser.put("email", email);
            newUser.put("bloodType", bloodType != null ? bloodType : "Unknown");
            newUser.put("donations", 0);
            newUser.put("verified", false);
            
            usersArray.put(newUser);
            prefs.edit().putString(KEY_USERS, usersArray.toString()).apply();
            
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
                UserData user = new UserData(
                    userObj.optString("name", "Unknown"),
                    userObj.optString("email", ""),
                    userObj.optString("bloodType", "Unknown"),
                    userObj.optInt("donations", 0),
                    userObj.optBoolean("verified", false)
                );
                users.add(user);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return users;
    }

    /**
     * Get user count
     */
    public static int getUserCount(Context context) {
        return getAllUsers(context).size();
    }

    /**
     * Simple user data class
     */
    public static class UserData {
        public String name;
        public String email;
        public String bloodType;
        public int donations;
        public boolean verified;

        public UserData(String name, String email, String bloodType, int donations, boolean verified) {
            this.name = name;
            this.email = email;
            this.bloodType = bloodType;
            this.donations = donations;
            this.verified = verified;
        }
    }
}
