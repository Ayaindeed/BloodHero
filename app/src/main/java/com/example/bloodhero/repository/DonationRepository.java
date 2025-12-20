package com.example.bloodhero.repository;

import android.content.Context;

import com.example.bloodhero.database.BloodHeroDatabaseHelper;
import com.example.bloodhero.models.Donation;

import java.util.List;

/**
 * Repository for Donation data operations
 * Provides clean interface to SQLite database
 */
public class DonationRepository {
    private final BloodHeroDatabaseHelper dbHelper;
    private static DonationRepository instance;

    private DonationRepository(Context context) {
        dbHelper = BloodHeroDatabaseHelper.getInstance(context);
    }

    public static synchronized DonationRepository getInstance(Context context) {
        if (instance == null) {
            instance = new DonationRepository(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Save a completed donation
     */
    public boolean saveDonation(Donation donation) {
        return dbHelper.insertDonation(donation) != -1;
    }

    /**
     * Get all donations for a specific user
     */
    public List<Donation> getDonationsByUserId(String userId) {
        return dbHelper.getDonationsByUserId(userId);
    }

    /**
     * Get all donations (admin view)
     */
    public List<Donation> getAllDonations() {
        return dbHelper.getAllDonations();
    }

    /**
     * Get donation count for a user
     */
    public int getDonationCount(String userId) {
        return dbHelper.getTotalDonationsCount(userId);
    }

    /**
     * Delete a donation
     */
    public boolean deleteDonation(String donationId) {
        return dbHelper.deleteDonation(donationId) > 0;
    }
}
