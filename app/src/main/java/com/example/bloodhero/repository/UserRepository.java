package com.example.bloodhero.repository;

import android.content.Context;

import com.example.bloodhero.database.BloodHeroDatabaseHelper;
import com.example.bloodhero.models.User;

import java.util.UUID;

/**
 * Repository for User data operations
 * Provides clean interface to SQLite database
 */
public class UserRepository {
    private final BloodHeroDatabaseHelper dbHelper;
    private static UserRepository instance;

    private UserRepository(Context context) {
        dbHelper = BloodHeroDatabaseHelper.getInstance(context);
    }

    public static synchronized UserRepository getInstance(Context context) {
        if (instance == null) {
            instance = new UserRepository(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Register a new user
     */
    public User registerUser(String email, String password, String name, String bloodType) {
        // Check if user already exists
        User existingUser = dbHelper.getUserByEmail(email);
        if (existingUser != null) {
            return null; // User already exists
        }

        // Create new user
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(email);
        user.setPassword(password);
        user.setName(name);
        user.setBloodType(bloodType);
        user.setCreatedAt(System.currentTimeMillis());
        user.setTotalDonations(0);
        user.setTotalPoints(0);

        long result = dbHelper.insertUser(user);
        return result != -1 ? user : null;
    }

    /**
     * Login user
     */
    public User loginUser(String email, String password) {
        User user = dbHelper.getUserByEmail(email);
        if (user != null && user.getPassword() != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return dbHelper.getUserByEmail(email);
    }

    /**
     * Get user by ID
     */
    public User getUserById(String userId) {
        return dbHelper.getUserById(userId);
    }
    
    /**
     * Get all users
     */
    public java.util.List<User> getAllUsers() {
        return dbHelper.getAllUsers();
    }

    /**
     * Update user profile
     */
    public boolean updateUser(User user) {
        int result = dbHelper.updateUser(user);
        return result > 0;
    }

    /**
     * Update user profile image
     */
    public boolean updateProfileImage(String userId, String imageUrl) {
        User user = dbHelper.getUserById(userId);
        if (user != null) {
            user.setProfileImageUrl(imageUrl);
            return dbHelper.updateUser(user) > 0;
        }
        return false;
    }

    /**
     * Update user password
     */
    public boolean updatePassword(String userId, String newPassword) {
        User user = dbHelper.getUserById(userId);
        if (user != null) {
            user.setPassword(newPassword);
            return dbHelper.updateUser(user) > 0;
        }
        return false;
    }
    
    /**
     * Get user's security question
     */
    public String getSecurityQuestion(String email) {
        User user = getUserByEmail(email);
        if (user != null && user.getSecurityQuestion() != null) {
            return user.getSecurityQuestion();
        }
        return null;
    }
    
    /**
     * Verify user's security answer
     */
    public boolean verifySecurityAnswer(String email, String answer) {
        User user = getUserByEmail(email);
        if (user != null && user.getSecurityAnswer() != null) {
            // Case-insensitive comparison
            return user.getSecurityAnswer().toLowerCase().equals(answer.toLowerCase());
        }
        return false;
    }
    
    /**
     * Update user security question and answer
     */
    public boolean updateSecurityQuestion(String userId, String question, String answer) {
        User user = dbHelper.getUserById(userId);
        if (user != null) {
            user.setSecurityQuestion(question);
            user.setSecurityAnswer(answer);
            return dbHelper.updateUser(user) > 0;
        }
        return false;
    }
    
    /**
     * Update user points
     */
    public boolean updatePoints(String userId, int points) {
        User user = dbHelper.getUserById(userId);
        if (user != null) {
            user.setTotalPoints(points);
            return dbHelper.updateUser(user) > 0;
        }
        return false;
    }
    
    /**
     * Increment donation count and update last donation date
     */
    public boolean incrementDonations(String userId, int points) {
        User user = dbHelper.getUserById(userId);
        if (user != null) {
            user.setTotalDonations(user.getTotalDonations() + 1);
            user.setTotalPoints(user.getTotalPoints() + points);
            user.setLastDonationDate(System.currentTimeMillis()); // Track donation date
            return dbHelper.updateUser(user) > 0;
        }
        return false;
    }

    /**
     * Delete user
     */
    public boolean deleteUser(String userId) {
        return dbHelper.deleteUser(userId) > 0;
    }

    /**
     * Clear all users (for fresh start - admin only)
     */
    public void clearAllUsers() {
        dbHelper.getWritableDatabase().execSQL("DELETE FROM users");
        dbHelper.getWritableDatabase().execSQL("DELETE FROM donations");
        dbHelper.getWritableDatabase().execSQL("DELETE FROM appointments");
        dbHelper.getWritableDatabase().execSQL("DELETE FROM user_badges");
    }
}
