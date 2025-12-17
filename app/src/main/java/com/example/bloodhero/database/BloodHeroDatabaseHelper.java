package com.example.bloodhero.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.bloodhero.models.Appointment;
import com.example.bloodhero.models.Badge;
import com.example.bloodhero.models.Campaign;
import com.example.bloodhero.models.Donation;
import com.example.bloodhero.models.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SQLite Database Helper for BloodHero App
 * Handles local data persistence for offline access
 */
public class BloodHeroDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bloodhero.db";
    private static final int DATABASE_VERSION = 3; // Incremented for lastDonationDate column

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_DONATIONS = "donations";
    private static final String TABLE_CAMPAIGNS = "campaigns";
    private static final String TABLE_APPOINTMENTS = "appointments";
    private static final String TABLE_BADGES = "badges";
    private static final String TABLE_USER_BADGES = "user_badges";

    // Common Columns
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CREATED_AT = "created_at";

    // Users Table Columns
    private static final String COLUMN_USER_EMAIL = "email";
    private static final String COLUMN_USER_PASSWORD = "password";
    private static final String COLUMN_USER_NAME = "name";
    private static final String COLUMN_USER_BLOOD_TYPE = "blood_type";
    private static final String COLUMN_USER_LOCATION = "location";
    private static final String COLUMN_USER_PHONE = "phone";
    private static final String COLUMN_USER_TOTAL_DONATIONS = "total_donations";
    private static final String COLUMN_USER_TOTAL_POINTS = "total_points";
    private static final String COLUMN_USER_PROFILE_IMAGE = "profile_image_url";
    private static final String COLUMN_USER_LAST_DONATION = "last_donation_date";

    // Donations Table Columns
    private static final String COLUMN_DONATION_USER_ID = "user_id";
    private static final String COLUMN_DONATION_CAMPAIGN_ID = "campaign_id";
    private static final String COLUMN_DONATION_CAMPAIGN_NAME = "campaign_name";
    private static final String COLUMN_DONATION_LOCATION = "location";
    private static final String COLUMN_DONATION_DATE = "date";
    private static final String COLUMN_DONATION_BLOOD_TYPE = "blood_type";
    private static final String COLUMN_DONATION_POINTS = "points_earned";
    private static final String COLUMN_DONATION_STATUS = "status";

    // Campaigns Table Columns
    private static final String COLUMN_CAMPAIGN_NAME = "name";
    private static final String COLUMN_CAMPAIGN_ORGANIZER = "organizer";
    private static final String COLUMN_CAMPAIGN_LOCATION = "location";
    private static final String COLUMN_CAMPAIGN_DATE = "date";
    private static final String COLUMN_CAMPAIGN_TIME = "time";
    private static final String COLUMN_CAMPAIGN_DISTANCE = "distance";
    private static final String COLUMN_CAMPAIGN_BLOOD_TYPES = "blood_types_needed";
    private static final String COLUMN_CAMPAIGN_DESCRIPTION = "description";

    // Appointments Table Columns
    private static final String COLUMN_APPOINTMENT_USER_ID = "user_id";
    private static final String COLUMN_APPOINTMENT_CAMPAIGN_ID = "campaign_id";
    private static final String COLUMN_APPOINTMENT_CAMPAIGN_NAME = "campaign_name";
    private static final String COLUMN_APPOINTMENT_LOCATION = "location";
    private static final String COLUMN_APPOINTMENT_DATE = "date";
    private static final String COLUMN_APPOINTMENT_TIME = "time_slot";
    private static final String COLUMN_APPOINTMENT_STATUS = "status";

    // Badges Table Columns
    private static final String COLUMN_BADGE_NAME = "name";
    private static final String COLUMN_BADGE_DESCRIPTION = "description";
    private static final String COLUMN_BADGE_ICON_RES = "icon_res_id";
    private static final String COLUMN_BADGE_POINTS = "point_value";
    private static final String COLUMN_BADGE_UNLOCK_CRITERIA = "unlock_criteria";

    // User Badges Table Columns
    private static final String COLUMN_USER_BADGE_USER_ID = "user_id";
    private static final String COLUMN_USER_BADGE_BADGE_ID = "badge_id";
    private static final String COLUMN_USER_BADGE_UNLOCKED_AT = "unlocked_at";

    private static BloodHeroDatabaseHelper instance;

    public static synchronized BloodHeroDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new BloodHeroDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private BloodHeroDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users Table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_USER_EMAIL + " TEXT UNIQUE NOT NULL, " +
                COLUMN_USER_PASSWORD + " TEXT NOT NULL, " +
                COLUMN_USER_NAME + " TEXT, " +
                COLUMN_USER_BLOOD_TYPE + " TEXT, " +
                COLUMN_USER_LOCATION + " TEXT, " +
                COLUMN_USER_PHONE + " TEXT, " +
                COLUMN_USER_TOTAL_DONATIONS + " INTEGER DEFAULT 0, " +
                COLUMN_USER_TOTAL_POINTS + " INTEGER DEFAULT 0, " +
                COLUMN_USER_PROFILE_IMAGE + " TEXT, " +
                COLUMN_USER_LAST_DONATION + " INTEGER DEFAULT 0, " +
                COLUMN_CREATED_AT + " TEXT" +
                ")";
        db.execSQL(createUsersTable);

        // Create Donations Table
        String createDonationsTable = "CREATE TABLE " + TABLE_DONATIONS + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_DONATION_USER_ID + " TEXT NOT NULL, " +
                COLUMN_DONATION_CAMPAIGN_ID + " TEXT, " +
                COLUMN_DONATION_CAMPAIGN_NAME + " TEXT, " +
                COLUMN_DONATION_LOCATION + " TEXT, " +
                COLUMN_DONATION_DATE + " TEXT, " +
                COLUMN_DONATION_BLOOD_TYPE + " TEXT, " +
                COLUMN_DONATION_POINTS + " INTEGER DEFAULT 0, " +
                COLUMN_DONATION_STATUS + " TEXT, " +
                "FOREIGN KEY (" + COLUMN_DONATION_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ")" +
                ")";
        db.execSQL(createDonationsTable);

        // Create Campaigns Table
        String createCampaignsTable = "CREATE TABLE " + TABLE_CAMPAIGNS + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_CAMPAIGN_NAME + " TEXT NOT NULL, " +
                COLUMN_CAMPAIGN_ORGANIZER + " TEXT, " +
                COLUMN_CAMPAIGN_LOCATION + " TEXT, " +
                COLUMN_CAMPAIGN_DATE + " TEXT, " +
                COLUMN_CAMPAIGN_TIME + " TEXT, " +
                COLUMN_CAMPAIGN_DISTANCE + " REAL, " +
                COLUMN_CAMPAIGN_BLOOD_TYPES + " TEXT, " +
                COLUMN_CAMPAIGN_DESCRIPTION + " TEXT" +
                ")";
        db.execSQL(createCampaignsTable);

        // Create Appointments Table
        String createAppointmentsTable = "CREATE TABLE " + TABLE_APPOINTMENTS + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_APPOINTMENT_USER_ID + " TEXT NOT NULL, " +
                COLUMN_APPOINTMENT_CAMPAIGN_ID + " TEXT, " +
                COLUMN_APPOINTMENT_CAMPAIGN_NAME + " TEXT, " +
                COLUMN_APPOINTMENT_LOCATION + " TEXT, " +
                COLUMN_APPOINTMENT_DATE + " TEXT, " +
                COLUMN_APPOINTMENT_TIME + " TEXT, " +
                COLUMN_APPOINTMENT_STATUS + " TEXT DEFAULT 'SCHEDULED', " +
                COLUMN_CREATED_AT + " TEXT, " +
                "FOREIGN KEY (" + COLUMN_APPOINTMENT_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ")" +
                ")";
        db.execSQL(createAppointmentsTable);

        // Create Badges Table
        String createBadgesTable = "CREATE TABLE " + TABLE_BADGES + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_BADGE_NAME + " TEXT NOT NULL, " +
                COLUMN_BADGE_DESCRIPTION + " TEXT, " +
                COLUMN_BADGE_ICON_RES + " INTEGER, " +
                COLUMN_BADGE_POINTS + " INTEGER DEFAULT 0, " +
                COLUMN_BADGE_UNLOCK_CRITERIA + " TEXT" +
                ")";
        db.execSQL(createBadgesTable);

        // Create User Badges Junction Table
        String createUserBadgesTable = "CREATE TABLE " + TABLE_USER_BADGES + " (" +
                COLUMN_USER_BADGE_USER_ID + " TEXT NOT NULL, " +
                COLUMN_USER_BADGE_BADGE_ID + " TEXT NOT NULL, " +
                COLUMN_USER_BADGE_UNLOCKED_AT + " TEXT, " +
                "PRIMARY KEY (" + COLUMN_USER_BADGE_USER_ID + ", " + COLUMN_USER_BADGE_BADGE_ID + "), " +
                "FOREIGN KEY (" + COLUMN_USER_BADGE_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "), " +
                "FOREIGN KEY (" + COLUMN_USER_BADGE_BADGE_ID + ") REFERENCES " + TABLE_BADGES + "(" + COLUMN_ID + ")" +
                ")";
        db.execSQL(createUserBadgesTable);

        // Create indexes for better query performance
        db.execSQL("CREATE INDEX idx_donations_user ON " + TABLE_DONATIONS + "(" + COLUMN_DONATION_USER_ID + ")");
        db.execSQL("CREATE INDEX idx_appointments_user ON " + TABLE_APPOINTMENTS + "(" + COLUMN_APPOINTMENT_USER_ID + ")");
        db.execSQL("CREATE INDEX idx_user_badges_user ON " + TABLE_USER_BADGES + "(" + COLUMN_USER_BADGE_USER_ID + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Preserve existing data during upgrade instead of dropping tables
        if (oldVersion < 3 && newVersion >= 3) {
            // Add lastDonationDate column to users table if upgrading from version 2 to 3
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_USER_LAST_DONATION + " INTEGER DEFAULT 0");
            } catch (Exception e) {
                // Column might already exist, ignore
                e.printStackTrace();
            }
        }
        
        // For other upgrades, add similar migration logic instead of dropping tables
        // Only drop and recreate as last resort during development
    }

    // ==================== USER CRUD OPERATIONS ====================

    /**
     * Create a new user
     */
    public long insertUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, user.getId());
        values.put(COLUMN_USER_EMAIL, user.getEmail());
        values.put(COLUMN_USER_PASSWORD, user.getPassword());
        values.put(COLUMN_USER_NAME, user.getName());
        values.put(COLUMN_USER_BLOOD_TYPE, user.getBloodType());
        values.put(COLUMN_USER_LOCATION, user.getLocation());
        values.put(COLUMN_USER_PHONE, user.getPhoneNumber());
        values.put(COLUMN_USER_TOTAL_DONATIONS, user.getTotalDonations());
        values.put(COLUMN_USER_TOTAL_POINTS, user.getTotalPoints());
        values.put(COLUMN_USER_PROFILE_IMAGE, user.getProfileImageUrl());
        values.put(COLUMN_USER_LAST_DONATION, user.getLastDonationDate());
        values.put(COLUMN_CREATED_AT, user.getCreatedAt());

        return db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Read user by ID
     */
    public User getUserById(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COLUMN_ID + "=?",
                new String[]{userId}, null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        return user;
    }

    /**
     * Read user by email
     */
    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COLUMN_USER_EMAIL + "=?",
                new String[]{email}, null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        return user;
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, null, null, null, null, 
                COLUMN_USER_TOTAL_POINTS + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                users.add(cursorToUser(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return users;
    }

    /**
     * Update user
     */
    public int updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, user.getName());
        values.put(COLUMN_USER_BLOOD_TYPE, user.getBloodType());
        values.put(COLUMN_USER_LOCATION, user.getLocation());
        values.put(COLUMN_USER_PHONE, user.getPhoneNumber());
        values.put(COLUMN_USER_TOTAL_DONATIONS, user.getTotalDonations());
        values.put(COLUMN_USER_TOTAL_POINTS, user.getTotalPoints());
        values.put(COLUMN_USER_PROFILE_IMAGE, user.getProfileImageUrl());
        values.put(COLUMN_USER_LAST_DONATION, user.getLastDonationDate());

        return db.update(TABLE_USERS, values, COLUMN_ID + "=?", new String[]{user.getId()});
    }

    /**
     * Delete user
     */
    public int deleteUser(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_USERS, COLUMN_ID + "=?", new String[]{userId});
    }

    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD)));
        user.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)));
        user.setBloodType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_BLOOD_TYPE)));
        user.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_LOCATION)));
        user.setPhoneNumber(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PHONE)));
        user.setTotalDonations(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_TOTAL_DONATIONS)));
        user.setTotalPoints(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_TOTAL_POINTS)));
        user.setProfileImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PROFILE_IMAGE)));
        user.setLastDonationDate(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_LAST_DONATION)));
        user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
        return user;
    }

    // ==================== DONATION CRUD OPERATIONS ====================

    /**
     * Create a new donation record
     */
    public long insertDonation(Donation donation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, donation.getId());
        values.put(COLUMN_DONATION_USER_ID, donation.getUserId());
        values.put(COLUMN_DONATION_CAMPAIGN_ID, donation.getCampaignId());
        values.put(COLUMN_DONATION_CAMPAIGN_NAME, donation.getCampaignName());
        values.put(COLUMN_DONATION_LOCATION, donation.getLocation());
        values.put(COLUMN_DONATION_DATE, donation.getDate());
        values.put(COLUMN_DONATION_BLOOD_TYPE, donation.getBloodType());
        values.put(COLUMN_DONATION_POINTS, donation.getPointsEarned());
        values.put(COLUMN_DONATION_STATUS, donation.getStatus());

        return db.insertWithOnConflict(TABLE_DONATIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Read all donations for a user
     */
    public List<Donation> getDonationsByUserId(String userId) {
        List<Donation> donations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_DONATIONS, null, 
                COLUMN_DONATION_USER_ID + "=?", new String[]{userId},
                null, null, COLUMN_DONATION_DATE + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                donations.add(cursorToDonation(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return donations;
    }

    /**
     * Get total donations count for a user
     */
    public int getTotalDonationsCount(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_DONATIONS + 
                " WHERE " + COLUMN_DONATION_USER_ID + "=? AND " + COLUMN_DONATION_STATUS + "='COMPLETED'",
                new String[]{userId});
        
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    /**
     * Delete a donation
     */
    public int deleteDonation(String donationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_DONATIONS, COLUMN_ID + "=?", new String[]{donationId});
    }

    private Donation cursorToDonation(Cursor cursor) {
        return new Donation(
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DONATION_USER_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DONATION_CAMPAIGN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DONATION_CAMPAIGN_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DONATION_LOCATION)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DONATION_DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DONATION_BLOOD_TYPE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DONATION_POINTS)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DONATION_STATUS))
        );
    }

    // ==================== CAMPAIGN CRUD OPERATIONS ====================

    /**
     * Create a new campaign
     */
    public long insertCampaign(Campaign campaign) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, campaign.getId());
        values.put(COLUMN_CAMPAIGN_NAME, campaign.getName());
        values.put(COLUMN_CAMPAIGN_ORGANIZER, campaign.getOrganizer());
        values.put(COLUMN_CAMPAIGN_LOCATION, campaign.getLocation());
        values.put(COLUMN_CAMPAIGN_DATE, campaign.getDate());
        values.put(COLUMN_CAMPAIGN_TIME, campaign.getTime());
        values.put(COLUMN_CAMPAIGN_DISTANCE, campaign.getDistance());
        values.put(COLUMN_CAMPAIGN_BLOOD_TYPES, String.join(",", campaign.getBloodTypesNeeded()));
        values.put(COLUMN_CAMPAIGN_DESCRIPTION, campaign.getDescription());

        return db.insertWithOnConflict(TABLE_CAMPAIGNS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Read all campaigns
     */
    public List<Campaign> getAllCampaigns() {
        List<Campaign> campaigns = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_CAMPAIGNS, null, null, null, null, null, 
                COLUMN_CAMPAIGN_DISTANCE + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                campaigns.add(cursorToCampaign(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return campaigns;
    }

    /**
     * Search campaigns by name or location
     */
    public List<Campaign> searchCampaigns(String query) {
        List<Campaign> campaigns = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String searchQuery = "%" + query + "%";
        Cursor cursor = db.query(TABLE_CAMPAIGNS, null,
                COLUMN_CAMPAIGN_NAME + " LIKE ? OR " + COLUMN_CAMPAIGN_LOCATION + " LIKE ?",
                new String[]{searchQuery, searchQuery}, null, null, COLUMN_CAMPAIGN_DISTANCE + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                campaigns.add(cursorToCampaign(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return campaigns;
    }

    /**
     * Delete a campaign
     */
    public int deleteCampaign(String campaignId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_CAMPAIGNS, COLUMN_ID + "=?", new String[]{campaignId});
    }

    private Campaign cursorToCampaign(Cursor cursor) {
        String bloodTypesStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_BLOOD_TYPES));
        List<String> bloodTypes = bloodTypesStr != null ? Arrays.asList(bloodTypesStr.split(",")) : new ArrayList<>();

        return new Campaign(
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_ORGANIZER)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_LOCATION)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_TIME)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_DISTANCE)),
                bloodTypes,
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAMPAIGN_DESCRIPTION))
        );
    }

    // ==================== APPOINTMENT CRUD OPERATIONS ====================

    /**
     * Create a new appointment
     */
    public long insertAppointment(Appointment appointment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, appointment.getId());
        values.put(COLUMN_APPOINTMENT_USER_ID, appointment.getUserId());
        values.put(COLUMN_APPOINTMENT_CAMPAIGN_ID, appointment.getCampaignId());
        values.put(COLUMN_APPOINTMENT_CAMPAIGN_NAME, appointment.getCampaignName());
        values.put(COLUMN_APPOINTMENT_LOCATION, appointment.getLocation());
        values.put(COLUMN_APPOINTMENT_DATE, appointment.getDate());
        values.put(COLUMN_APPOINTMENT_TIME, appointment.getTimeSlot());
        values.put(COLUMN_APPOINTMENT_STATUS, appointment.getStatus().name());
        values.put(COLUMN_CREATED_AT, System.currentTimeMillis() + "");

        return db.insertWithOnConflict(TABLE_APPOINTMENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Read all appointments for a user
     */
    public List<Appointment> getAppointmentsByUserId(String userId) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_APPOINTMENTS, null,
                COLUMN_APPOINTMENT_USER_ID + "=?", new String[]{userId},
                null, null, COLUMN_APPOINTMENT_DATE + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                appointments.add(cursorToAppointment(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return appointments;
    }

    /**
     * Get upcoming appointments
     */
    public List<Appointment> getUpcomingAppointments(String userId) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_APPOINTMENTS, null,
                COLUMN_APPOINTMENT_USER_ID + "=? AND " + COLUMN_APPOINTMENT_STATUS + "=?",
                new String[]{userId, "SCHEDULED"}, null, null, COLUMN_APPOINTMENT_DATE + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                appointments.add(cursorToAppointment(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return appointments;
    }

    /**
     * Update appointment status
     */
    public int updateAppointmentStatus(String appointmentId, Appointment.Status status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_APPOINTMENT_STATUS, status.name());

        return db.update(TABLE_APPOINTMENTS, values, COLUMN_ID + "=?", new String[]{appointmentId});
    }

    /**
     * Delete an appointment
     */
    public int deleteAppointment(String appointmentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_APPOINTMENTS, COLUMN_ID + "=?", new String[]{appointmentId});
    }

    private Appointment cursorToAppointment(Cursor cursor) {
        Appointment appointment = new Appointment(
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_USER_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_CAMPAIGN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_CAMPAIGN_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_LOCATION)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_TIME)),
                Appointment.Status.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_STATUS)))
        );
        return appointment;
    }

    // ==================== BADGE OPERATIONS ====================

    /**
     * Insert a badge
     */
    public long insertBadge(Badge badge) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, badge.getId());
        values.put(COLUMN_BADGE_NAME, badge.getName());
        values.put(COLUMN_BADGE_DESCRIPTION, badge.getDescription());
        values.put(COLUMN_BADGE_ICON_RES, badge.getIconResId());
        values.put(COLUMN_BADGE_POINTS, badge.getPointValue());
        values.put(COLUMN_BADGE_UNLOCK_CRITERIA, badge.getUnlockCriteria());

        return db.insertWithOnConflict(TABLE_BADGES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Unlock a badge for a user
     */
    public long unlockBadge(String userId, String badgeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_BADGE_USER_ID, userId);
        values.put(COLUMN_USER_BADGE_BADGE_ID, badgeId);
        values.put(COLUMN_USER_BADGE_UNLOCKED_AT, System.currentTimeMillis() + "");

        return db.insertWithOnConflict(TABLE_USER_BADGES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    /**
     * Check if user has unlocked a badge
     */
    public boolean hasBadge(String userId, String badgeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER_BADGES, null,
                COLUMN_USER_BADGE_USER_ID + "=? AND " + COLUMN_USER_BADGE_BADGE_ID + "=?",
                new String[]{userId, badgeId}, null, null, null);

        boolean hasBadge = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return hasBadge;
    }

    /**
     * Get count of unlocked badges for a user
     */
    public int getUnlockedBadgeCount(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USER_BADGES +
                " WHERE " + COLUMN_USER_BADGE_USER_ID + "=?", new String[]{userId});

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Clear all data (for logout)
     */
    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER_BADGES, null, null);
        db.delete(TABLE_APPOINTMENTS, null, null);
        db.delete(TABLE_DONATIONS, null, null);
        db.delete(TABLE_CAMPAIGNS, null, null);
        db.delete(TABLE_BADGES, null, null);
        db.delete(TABLE_USERS, null, null);
    }

    /**
     * Get total points for a user from donations
     */
    public int getTotalPoints(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_DONATION_POINTS + ") FROM " + TABLE_DONATIONS +
                " WHERE " + COLUMN_DONATION_USER_ID + "=? AND " + COLUMN_DONATION_STATUS + "='COMPLETED'",
                new String[]{userId});

        int total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getInt(0);
            cursor.close();
        }
        return total;
    }
}