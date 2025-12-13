package com.example.bloodhero.utils;

import android.content.Context;

import com.example.bloodhero.models.User;
import com.example.bloodhero.repository.UserRepository;

/**
 * UserHelper - convenient methods to get current logged-in user
 */
public class UserHelper {
    
    /**
     * Get currently logged-in user from SQLite
     */
    public static User getCurrentUser(Context context) {
        SessionManager sessionManager = SessionManager.getInstance(context);
        String userId = sessionManager.getUserId();
        
        if (userId == null) {
            return null;
        }
        
        UserRepository userRepository = UserRepository.getInstance(context);
        return userRepository.getUserById(userId);
    }
    
    /**
     * Check if user is logged in
     */
    public static boolean isLoggedIn(Context context) {
        return SessionManager.getInstance(context).isLoggedIn();
    }
    
    /**
     * Check if current user is admin
     */
    public static boolean isAdmin(Context context) {
        return SessionManager.getInstance(context).isAdmin();
    }
    
    /**
     * Logout current user
     */
    public static void logout(Context context) {
        SessionManager.getInstance(context).logout();
    }
}
