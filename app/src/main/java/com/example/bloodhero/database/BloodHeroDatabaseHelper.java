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
    private static final int DATABASE_VERSION = 9; // Added second security question and answer columns

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_DONATIONS = "donations";
    private static final String TABLE_CAMPAIGNS = "campaigns";
    private static final String TABLE_APPOINTMENTS = "appointments";
    private static final String TABLE_BADGES = "badges";
    private static final String TABLE_USER_BADGES = "user_badges";
    private static final String TABLE_BLOOD_REQUESTS = "blood_requests";
    private static final String TABLE_REWARDS = "rewards";
    private static final String TABLE_USER_REWARDS = "user_rewards";
    private static final String TABLE_LEADERBOARD_CACHE = "leaderboard_cache";

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
    private static final String COLUMN_USER_DATE_OF_BIRTH = "date_of_birth";
    private static final String COLUMN_USER_GENDER = "gender";
    private static final String COLUMN_USER_WEIGHT = "weight";
    private static final String COLUMN_USER_TOTAL_DONATIONS = "total_donations";
    private static final String COLUMN_USER_TOTAL_POINTS = "total_points";
    private static final String COLUMN_USER_PROFILE_IMAGE = "profile_image_url";
    private static final String COLUMN_USER_LAST_DONATION = "last_donation_date";
    private static final String COLUMN_USER_SECURITY_QUESTION = "security_question";
    private static final String COLUMN_USER_SECURITY_ANSWER = "security_answer";
    private static final String COLUMN_USER_SECURITY_QUESTION_2 = "security_question_2";
    private static final String COLUMN_USER_SECURITY_ANSWER_2 = "security_answer_2";

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
    private static final String COLUMN_APPOINTMENT_VERIFICATION_CODE = "verification_code";
    private static final String COLUMN_APPOINTMENT_BED_NUMBER = "bed_number";
    private static final String COLUMN_APPOINTMENT_CHECKED_IN_AT = "checked_in_at";

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

    // Blood Requests Table Columns
    private static final String COLUMN_BLOOD_REQUEST_PATIENT_NAME = "patient_name";
    private static final String COLUMN_BLOOD_REQUEST_BLOOD_TYPE = "blood_type";
    private static final String COLUMN_BLOOD_REQUEST_HOSPITAL = "hospital";
    private static final String COLUMN_BLOOD_REQUEST_CITY = "city";
    private static final String COLUMN_BLOOD_REQUEST_URGENCY = "urgency_level";
    private static final String COLUMN_BLOOD_REQUEST_UNITS = "units_needed";
    private static final String COLUMN_BLOOD_REQUEST_CONTACT = "contact_phone";
    private static final String COLUMN_BLOOD_REQUEST_POSTED_TIME = "posted_time";
    private static final String COLUMN_BLOOD_REQUEST_IS_ACTIVE = "is_active";

    // Rewards Table Columns
    private static final String COLUMN_REWARD_NAME = "name";
    private static final String COLUMN_REWARD_DESCRIPTION = "description";
    private static final String COLUMN_REWARD_PARTNER = "partner_name";
    private static final String COLUMN_REWARD_POINTS_COST = "points_cost";
    private static final String COLUMN_REWARD_ICON_RES = "icon_res_id";
    private static final String COLUMN_REWARD_CATEGORY = "category";
    private static final String COLUMN_REWARD_EXPIRY_DATE = "expiry_date";

    // User Rewards (Redemptions) Table Columns
    private static final String COLUMN_USER_REWARD_USER_ID = "user_id";
    private static final String COLUMN_USER_REWARD_REWARD_ID = "reward_id";
    private static final String COLUMN_USER_REWARD_REDEEMED_AT = "redeemed_at";
    private static final String COLUMN_USER_REWARD_POINTS_SPENT = "points_spent";

    // Leaderboard Cache Table Columns
    private static final String COLUMN_LEADERBOARD_USER_ID = "user_id";
    private static final String COLUMN_LEADERBOARD_RANK = "rank";
    private static final String COLUMN_LEADERBOARD_USERNAME = "user_name";
    private static final String COLUMN_LEADERBOARD_BLOOD_TYPE = "blood_type";
    private static final String COLUMN_LEADERBOARD_TOTAL_DONATIONS = "total_donations";
    private static final String COLUMN_LEADERBOARD_TOTAL_POINTS = "total_points";
    private static final String COLUMN_LEADERBOARD_LAST_UPDATED = "last_updated";

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
                COLUMN_USER_DATE_OF_BIRTH + " TEXT, " +
                COLUMN_USER_GENDER + " TEXT, " +
                COLUMN_USER_WEIGHT + " REAL, " +
                COLUMN_USER_TOTAL_DONATIONS + " INTEGER DEFAULT 0, " +
                COLUMN_USER_TOTAL_POINTS + " INTEGER DEFAULT 0, " +
                COLUMN_USER_PROFILE_IMAGE + " TEXT, " +
                COLUMN_USER_LAST_DONATION + " INTEGER DEFAULT 0, " +
                COLUMN_USER_SECURITY_QUESTION + " TEXT, " +
                COLUMN_USER_SECURITY_ANSWER + " TEXT, " +
                COLUMN_USER_SECURITY_QUESTION_2 + " TEXT, " +
                COLUMN_USER_SECURITY_ANSWER_2 + " TEXT, " +
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
                COLUMN_APPOINTMENT_VERIFICATION_CODE + " TEXT, " +
                COLUMN_APPOINTMENT_BED_NUMBER + " INTEGER, " +
                COLUMN_APPOINTMENT_CHECKED_IN_AT + " INTEGER, " +
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

        // Create Blood Requests Table
        String createBloodRequestsTable = "CREATE TABLE " + TABLE_BLOOD_REQUESTS + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_BLOOD_REQUEST_PATIENT_NAME + " TEXT NOT NULL, " +
                COLUMN_BLOOD_REQUEST_BLOOD_TYPE + " TEXT NOT NULL, " +
                COLUMN_BLOOD_REQUEST_HOSPITAL + " TEXT, " +
                COLUMN_BLOOD_REQUEST_CITY + " TEXT, " +
                COLUMN_BLOOD_REQUEST_URGENCY + " TEXT, " +
                COLUMN_BLOOD_REQUEST_UNITS + " INTEGER, " +
                COLUMN_BLOOD_REQUEST_CONTACT + " TEXT, " +
                COLUMN_BLOOD_REQUEST_POSTED_TIME + " TEXT, " +
                COLUMN_BLOOD_REQUEST_IS_ACTIVE + " INTEGER DEFAULT 1" +
                ")";
        db.execSQL(createBloodRequestsTable);

        // Create Rewards Table
        String createRewardsTable = "CREATE TABLE " + TABLE_REWARDS + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_REWARD_NAME + " TEXT NOT NULL, " +
                COLUMN_REWARD_DESCRIPTION + " TEXT, " +
                COLUMN_REWARD_PARTNER + " TEXT, " +
                COLUMN_REWARD_POINTS_COST + " INTEGER, " +
                COLUMN_REWARD_ICON_RES + " INTEGER, " +
                COLUMN_REWARD_CATEGORY + " TEXT, " +
                COLUMN_REWARD_EXPIRY_DATE + " TEXT" +
                ")";
        db.execSQL(createRewardsTable);

        // Create User Rewards (Redemptions) Table
        String createUserRewardsTable = "CREATE TABLE " + TABLE_USER_REWARDS + " (" +
                COLUMN_USER_REWARD_USER_ID + " TEXT NOT NULL, " +
                COLUMN_USER_REWARD_REWARD_ID + " TEXT NOT NULL, " +
                COLUMN_USER_REWARD_REDEEMED_AT + " TEXT, " +
                COLUMN_USER_REWARD_POINTS_SPENT + " INTEGER, " +
                "PRIMARY KEY (" + COLUMN_USER_REWARD_USER_ID + ", " + COLUMN_USER_REWARD_REWARD_ID + "), " +
                "FOREIGN KEY (" + COLUMN_USER_REWARD_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "), " +
                "FOREIGN KEY (" + COLUMN_USER_REWARD_REWARD_ID + ") REFERENCES " + TABLE_REWARDS + "(" + COLUMN_ID + ")" +
                ")";
        db.execSQL(createUserRewardsTable);

        // Create Leaderboard Cache Table
        String createLeaderboardCacheTable = "CREATE TABLE " + TABLE_LEADERBOARD_CACHE + " (" +
                COLUMN_LEADERBOARD_USER_ID + " TEXT PRIMARY KEY, " +
                COLUMN_LEADERBOARD_RANK + " INTEGER, " +
                COLUMN_LEADERBOARD_USERNAME + " TEXT, " +
                COLUMN_LEADERBOARD_BLOOD_TYPE + " TEXT, " +
                COLUMN_LEADERBOARD_TOTAL_DONATIONS + " INTEGER, " +
                COLUMN_LEADERBOARD_TOTAL_POINTS + " INTEGER, " +
                COLUMN_LEADERBOARD_LAST_UPDATED + " INTEGER, " +
                "FOREIGN KEY (" + COLUMN_LEADERBOARD_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ")" +
                ")";
        db.execSQL(createLeaderboardCacheTable);

        // Create indexes for better query performance
        db.execSQL("CREATE INDEX idx_donations_user ON " + TABLE_DONATIONS + "(" + COLUMN_DONATION_USER_ID + ")");
        db.execSQL("CREATE INDEX idx_appointments_user ON " + TABLE_APPOINTMENTS + "(" + COLUMN_APPOINTMENT_USER_ID + ")");
        db.execSQL("CREATE INDEX idx_user_badges_user ON " + TABLE_USER_BADGES + "(" + COLUMN_USER_BADGE_USER_ID + ")");
        db.execSQL("CREATE INDEX idx_blood_requests_active ON " + TABLE_BLOOD_REQUESTS + "(" + COLUMN_BLOOD_REQUEST_IS_ACTIVE + ")");
        db.execSQL("CREATE INDEX idx_user_rewards_user ON " + TABLE_USER_REWARDS + "(" + COLUMN_USER_REWARD_USER_ID + ")");
        db.execSQL("CREATE INDEX idx_leaderboard_rank ON " + TABLE_LEADERBOARD_CACHE + "(" + COLUMN_LEADERBOARD_RANK + ")");
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
        
        if (oldVersion < 4 && newVersion >= 4) {
            // Add verification_code and bed_number columns to appointments table
            try {
                db.execSQL("ALTER TABLE " + TABLE_APPOINTMENTS + " ADD COLUMN " + COLUMN_APPOINTMENT_VERIFICATION_CODE + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_APPOINTMENTS + " ADD COLUMN " + COLUMN_APPOINTMENT_BED_NUMBER + " INTEGER");
            } catch (Exception e) {
                // Columns might already exist, ignore
                e.printStackTrace();
            }
        }
        
        if (oldVersion < 5 && newVersion >= 5) {
            // Add date_of_birth, gender, and weight columns to users table
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_USER_DATE_OF_BIRTH + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_USER_GENDER + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_USER_WEIGHT + " REAL");
            } catch (Exception e) {
                // Columns might already exist, ignore
                e.printStackTrace();
            }
        }
        
        if (oldVersion < 6 && newVersion >= 6) {
            // Create new tables for blood requests, rewards, and leaderboard cache
            try {
                // Create Blood Requests Table
                db.execSQL("CREATE TABLE " + TABLE_BLOOD_REQUESTS + " (" +
                        COLUMN_ID + " TEXT PRIMARY KEY, " +
                        COLUMN_BLOOD_REQUEST_PATIENT_NAME + " TEXT NOT NULL, " +
                        COLUMN_BLOOD_REQUEST_BLOOD_TYPE + " TEXT NOT NULL, " +
                        COLUMN_BLOOD_REQUEST_HOSPITAL + " TEXT, " +
                        COLUMN_BLOOD_REQUEST_CITY + " TEXT, " +
                        COLUMN_BLOOD_REQUEST_URGENCY + " TEXT, " +
                        COLUMN_BLOOD_REQUEST_UNITS + " INTEGER, " +
                        COLUMN_BLOOD_REQUEST_CONTACT + " TEXT, " +
                        COLUMN_BLOOD_REQUEST_POSTED_TIME + " TEXT, " +
                        COLUMN_BLOOD_REQUEST_IS_ACTIVE + " INTEGER DEFAULT 1)");
                
                // Create Rewards Table
                db.execSQL("CREATE TABLE " + TABLE_REWARDS + " (" +
                        COLUMN_ID + " TEXT PRIMARY KEY, " +
                        COLUMN_REWARD_NAME + " TEXT NOT NULL, " +
                        COLUMN_REWARD_DESCRIPTION + " TEXT, " +
                        COLUMN_REWARD_PARTNER + " TEXT, " +
                        COLUMN_REWARD_POINTS_COST + " INTEGER, " +
                        COLUMN_REWARD_ICON_RES + " INTEGER, " +
                        COLUMN_REWARD_CATEGORY + " TEXT, " +
                        COLUMN_REWARD_EXPIRY_DATE + " TEXT)");
                
                // Create User Rewards Table
                db.execSQL("CREATE TABLE " + TABLE_USER_REWARDS + " (" +
                        COLUMN_USER_REWARD_USER_ID + " TEXT NOT NULL, " +
                        COLUMN_USER_REWARD_REWARD_ID + " TEXT NOT NULL, " +
                        COLUMN_USER_REWARD_REDEEMED_AT + " TEXT, " +
                        COLUMN_USER_REWARD_POINTS_SPENT + " INTEGER, " +
                        "PRIMARY KEY (" + COLUMN_USER_REWARD_USER_ID + ", " + COLUMN_USER_REWARD_REWARD_ID + "))");
                
                // Create Leaderboard Cache Table
                db.execSQL("CREATE TABLE " + TABLE_LEADERBOARD_CACHE + " (" +
                        COLUMN_LEADERBOARD_USER_ID + " TEXT PRIMARY KEY, " +
                        COLUMN_LEADERBOARD_RANK + " INTEGER, " +
                        COLUMN_LEADERBOARD_USERNAME + " TEXT, " +
                        COLUMN_LEADERBOARD_BLOOD_TYPE + " TEXT, " +
                        COLUMN_LEADERBOARD_TOTAL_DONATIONS + " INTEGER, " +
                        COLUMN_LEADERBOARD_TOTAL_POINTS + " INTEGER, " +
                        COLUMN_LEADERBOARD_LAST_UPDATED + " INTEGER)");
                
                // Create indexes
                db.execSQL("CREATE INDEX idx_blood_requests_active ON " + TABLE_BLOOD_REQUESTS + "(" + COLUMN_BLOOD_REQUEST_IS_ACTIVE + ")");
                db.execSQL("CREATE INDEX idx_user_rewards_user ON " + TABLE_USER_REWARDS + "(" + COLUMN_USER_REWARD_USER_ID + ")");
                db.execSQL("CREATE INDEX idx_leaderboard_rank ON " + TABLE_LEADERBOARD_CACHE + "(" + COLUMN_LEADERBOARD_RANK + ")");
            } catch (Exception e) {
                // Tables might already exist, ignore
                e.printStackTrace();
            }
        }
        
        if (oldVersion < 7 && newVersion >= 7) {
            // Add checked_in_at column to appointments table
            try {
                db.execSQL("ALTER TABLE " + TABLE_APPOINTMENTS + " ADD COLUMN " + COLUMN_APPOINTMENT_CHECKED_IN_AT + " INTEGER");
            } catch (Exception e) {
                // Column might already exist, ignore
                e.printStackTrace();
            }
        }
        
        if (oldVersion < 8 && newVersion >= 8) {
            // Add security question and answer columns to users table
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_USER_SECURITY_QUESTION + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_USER_SECURITY_ANSWER + " TEXT");
            } catch (Exception e) {
                // Columns might already exist, ignore
                e.printStackTrace();
            }
        }
        
        if (oldVersion < 9 && newVersion >= 9) {
            // Add second security question and answer columns to users table
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_USER_SECURITY_QUESTION_2 + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_USER_SECURITY_ANSWER_2 + " TEXT");
            } catch (Exception e) {
                // Columns might already exist, ignore
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
        values.put(COLUMN_USER_DATE_OF_BIRTH, user.getDateOfBirth());
        values.put(COLUMN_USER_GENDER, user.getGender());
        values.put(COLUMN_USER_WEIGHT, user.getWeight());
        values.put(COLUMN_USER_TOTAL_DONATIONS, user.getTotalDonations());
        values.put(COLUMN_USER_TOTAL_POINTS, user.getTotalPoints());
        values.put(COLUMN_USER_PROFILE_IMAGE, user.getProfileImageUrl());
        values.put(COLUMN_USER_LAST_DONATION, user.getLastDonationDate());
        values.put(COLUMN_USER_SECURITY_QUESTION, user.getSecurityQuestion());
        values.put(COLUMN_USER_SECURITY_ANSWER, user.getSecurityAnswer());
        values.put(COLUMN_USER_SECURITY_QUESTION_2, user.getSecurityQuestion2());
        values.put(COLUMN_USER_SECURITY_ANSWER_2, user.getSecurityAnswer2());
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
        values.put(COLUMN_USER_EMAIL, user.getEmail());
        values.put(COLUMN_USER_NAME, user.getName());
        values.put(COLUMN_USER_BLOOD_TYPE, user.getBloodType());
        values.put(COLUMN_USER_LOCATION, user.getLocation());
        values.put(COLUMN_USER_PHONE, user.getPhoneNumber());
        values.put(COLUMN_USER_DATE_OF_BIRTH, user.getDateOfBirth());
        values.put(COLUMN_USER_GENDER, user.getGender());
        values.put(COLUMN_USER_WEIGHT, user.getWeight());
        values.put(COLUMN_USER_TOTAL_DONATIONS, user.getTotalDonations());
        values.put(COLUMN_USER_TOTAL_POINTS, user.getTotalPoints());
        values.put(COLUMN_USER_PROFILE_IMAGE, user.getProfileImageUrl());
        values.put(COLUMN_USER_LAST_DONATION, user.getLastDonationDate());
        values.put(COLUMN_USER_SECURITY_QUESTION, user.getSecurityQuestion());
        values.put(COLUMN_USER_SECURITY_ANSWER, user.getSecurityAnswer());
        values.put(COLUMN_USER_SECURITY_QUESTION_2, user.getSecurityQuestion2());
        values.put(COLUMN_USER_SECURITY_ANSWER_2, user.getSecurityAnswer2());

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
        
        // Handle nullable fields for dateOfBirth, gender, weight
        int dobIndex = cursor.getColumnIndex(COLUMN_USER_DATE_OF_BIRTH);
        if (dobIndex >= 0 && !cursor.isNull(dobIndex)) {
            user.setDateOfBirth(cursor.getString(dobIndex));
        }
        
        int genderIndex = cursor.getColumnIndex(COLUMN_USER_GENDER);
        if (genderIndex >= 0 && !cursor.isNull(genderIndex)) {
            user.setGender(cursor.getString(genderIndex));
        }
        
        int weightIndex = cursor.getColumnIndex(COLUMN_USER_WEIGHT);
        if (weightIndex >= 0 && !cursor.isNull(weightIndex)) {
            user.setWeight(cursor.getDouble(weightIndex));
        }
        
        user.setTotalDonations(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_TOTAL_DONATIONS)));
        user.setTotalPoints(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_TOTAL_POINTS)));
        user.setProfileImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PROFILE_IMAGE)));
        user.setLastDonationDate(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_LAST_DONATION)));
        
        // Handle security question and answer
        int securityQuestionIndex = cursor.getColumnIndex(COLUMN_USER_SECURITY_QUESTION);
        if (securityQuestionIndex >= 0 && !cursor.isNull(securityQuestionIndex)) {
            user.setSecurityQuestion(cursor.getString(securityQuestionIndex));
        }
        
        int securityAnswerIndex = cursor.getColumnIndex(COLUMN_USER_SECURITY_ANSWER);
        if (securityAnswerIndex >= 0 && !cursor.isNull(securityAnswerIndex)) {
            user.setSecurityAnswer(cursor.getString(securityAnswerIndex));
        }
        
        // Handle second security question and answer
        int securityQuestion2Index = cursor.getColumnIndex(COLUMN_USER_SECURITY_QUESTION_2);
        if (securityQuestion2Index >= 0 && !cursor.isNull(securityQuestion2Index)) {
            user.setSecurityQuestion2(cursor.getString(securityQuestion2Index));
        }
        
        int securityAnswer2Index = cursor.getColumnIndex(COLUMN_USER_SECURITY_ANSWER_2);
        if (securityAnswer2Index >= 0 && !cursor.isNull(securityAnswer2Index)) {
            user.setSecurityAnswer2(cursor.getString(securityAnswer2Index));
        }
        
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
     * Get all donations (admin view)
     */
    public List<Donation> getAllDonations() {
        List<Donation> donations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_DONATIONS, null, null, null,
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
        values.put(COLUMN_APPOINTMENT_VERIFICATION_CODE, appointment.getVerificationCode());
        if (appointment.getBedNumber() != null) {
            values.put(COLUMN_APPOINTMENT_BED_NUMBER, appointment.getBedNumber());
        }
        if (appointment.getCheckedInAt() != null) {
            values.put(COLUMN_APPOINTMENT_CHECKED_IN_AT, appointment.getCheckedInAt());
        }
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
        
        // Set checked_in_at timestamp when status changes to CHECKED_IN
        if (status == Appointment.Status.CHECKED_IN) {
            values.put(COLUMN_APPOINTMENT_CHECKED_IN_AT, System.currentTimeMillis());
        }

        return db.update(TABLE_APPOINTMENTS, values, COLUMN_ID + "=?", new String[]{appointmentId});
    }

    /**
     * Assign appointment to bed and set status to IN_PROGRESS
     */
    public int assignAppointmentToBed(String appointmentId, int bedNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_APPOINTMENT_STATUS, Appointment.Status.IN_PROGRESS.name());
        values.put(COLUMN_APPOINTMENT_BED_NUMBER, bedNumber);

        return db.update(TABLE_APPOINTMENTS, values, COLUMN_ID + "=?", new String[]{appointmentId});
    }

    /**
     * Set appointment to pending verification with code
     */
    public int setAppointmentPendingVerification(String appointmentId, String verificationCode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_APPOINTMENT_STATUS, Appointment.Status.PENDING_VERIFICATION.name());
        values.put(COLUMN_APPOINTMENT_VERIFICATION_CODE, verificationCode);

        return db.update(TABLE_APPOINTMENTS, values, COLUMN_ID + "=?", new String[]{appointmentId});
    }

    /**
     * Get appointment by ID
     */
    public Appointment getAppointmentById(String appointmentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_APPOINTMENTS, null, COLUMN_ID + "=?",
                new String[]{appointmentId}, null, null, null);

        Appointment appointment = null;
        if (cursor != null && cursor.moveToFirst()) {
            appointment = cursorToAppointment(cursor);
            cursor.close();
        }
        return appointment;
    }

    /**
     * Get appointments by status
     */
    public List<Appointment> getAppointmentsByStatus(Appointment.Status status) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_APPOINTMENTS, null,
                COLUMN_APPOINTMENT_STATUS + "=?", new String[]{status.name()},
                null, null, COLUMN_APPOINTMENT_DATE + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                appointments.add(cursorToAppointment(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return appointments;
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
        
        // Set verification code if present
        int codeIdx = cursor.getColumnIndex(COLUMN_APPOINTMENT_VERIFICATION_CODE);
        if (codeIdx != -1 && !cursor.isNull(codeIdx)) {
            appointment.setVerificationCode(cursor.getString(codeIdx));
        }
        
        // Set bed number if present
        int bedIdx = cursor.getColumnIndex(COLUMN_APPOINTMENT_BED_NUMBER);
        if (bedIdx != -1 && !cursor.isNull(bedIdx)) {
            appointment.setBedNumber(cursor.getInt(bedIdx));
        }
        
        // Set checked_in_at timestamp if present
        int checkedInIdx = cursor.getColumnIndex(COLUMN_APPOINTMENT_CHECKED_IN_AT);
        if (checkedInIdx != -1 && !cursor.isNull(checkedInIdx)) {
            appointment.setCheckedInAt(cursor.getLong(checkedInIdx));
        }
        
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

    // ==================== BLOOD REQUEST CRUD OPERATIONS ====================

    public long insertBloodRequest(com.example.bloodhero.models.BloodRequest request) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, request.getId());
        values.put(COLUMN_BLOOD_REQUEST_PATIENT_NAME, request.getPatientName());
        values.put(COLUMN_BLOOD_REQUEST_BLOOD_TYPE, request.getBloodType());
        values.put(COLUMN_BLOOD_REQUEST_HOSPITAL, request.getHospital());
        values.put(COLUMN_BLOOD_REQUEST_CITY, request.getCity());
        values.put(COLUMN_BLOOD_REQUEST_URGENCY, request.getUrgencyLevel());
        values.put(COLUMN_BLOOD_REQUEST_UNITS, request.getUnitsNeeded());
        values.put(COLUMN_BLOOD_REQUEST_CONTACT, request.getContactPhone());
        values.put(COLUMN_BLOOD_REQUEST_POSTED_TIME, request.getPostedTime());
        values.put(COLUMN_BLOOD_REQUEST_IS_ACTIVE, request.isActive() ? 1 : 0);
        return db.insert(TABLE_BLOOD_REQUESTS, null, values);
    }

    public List<com.example.bloodhero.models.BloodRequest> getAllBloodRequests() {
        List<com.example.bloodhero.models.BloodRequest> requests = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BLOOD_REQUESTS, null, null, null, null, null,
                COLUMN_BLOOD_REQUEST_POSTED_TIME + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                requests.add(new com.example.bloodhero.models.BloodRequest(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BLOOD_REQUEST_PATIENT_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BLOOD_REQUEST_BLOOD_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BLOOD_REQUEST_HOSPITAL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BLOOD_REQUEST_CITY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BLOOD_REQUEST_URGENCY)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BLOOD_REQUEST_UNITS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BLOOD_REQUEST_CONTACT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BLOOD_REQUEST_POSTED_TIME))
                ));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return requests;
    }

    public List<com.example.bloodhero.models.BloodRequest> getActiveBloodRequests() {
        List<com.example.bloodhero.models.BloodRequest> requests = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BLOOD_REQUESTS, null,
                COLUMN_BLOOD_REQUEST_IS_ACTIVE + "=1", null, null, null,
                COLUMN_BLOOD_REQUEST_POSTED_TIME + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                requests.add(new com.example.bloodhero.models.BloodRequest(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BLOOD_REQUEST_PATIENT_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BLOOD_REQUEST_BLOOD_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BLOOD_REQUEST_HOSPITAL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BLOOD_REQUEST_CITY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BLOOD_REQUEST_URGENCY)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BLOOD_REQUEST_UNITS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BLOOD_REQUEST_CONTACT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BLOOD_REQUEST_POSTED_TIME))
                ));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return requests;
    }

    public int updateBloodRequestStatus(String requestId, boolean isActive) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BLOOD_REQUEST_IS_ACTIVE, isActive ? 1 : 0);
        return db.update(TABLE_BLOOD_REQUESTS, values, COLUMN_ID + "=?", new String[]{requestId});
    }

    // ==================== REWARD CRUD OPERATIONS ====================

    public long insertReward(com.example.bloodhero.models.Reward reward) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, reward.getId());
        values.put(COLUMN_REWARD_NAME, reward.getName());
        values.put(COLUMN_REWARD_DESCRIPTION, reward.getDescription());
        values.put(COLUMN_REWARD_PARTNER, reward.getPartnerName());
        values.put(COLUMN_REWARD_POINTS_COST, reward.getPointsCost());
        values.put(COLUMN_REWARD_ICON_RES, reward.getIconResId());
        values.put(COLUMN_REWARD_CATEGORY, reward.getCategory());
        values.put(COLUMN_REWARD_EXPIRY_DATE, reward.getExpiryDate());
        return db.insert(TABLE_REWARDS, null, values);
    }

    public List<com.example.bloodhero.models.Reward> getAllRewards() {
        List<com.example.bloodhero.models.Reward> rewards = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_REWARDS, null, null, null, null, null,
                COLUMN_REWARD_POINTS_COST + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                rewards.add(new com.example.bloodhero.models.Reward(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REWARD_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REWARD_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REWARD_PARTNER)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REWARD_POINTS_COST)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REWARD_ICON_RES)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REWARD_CATEGORY))
                ));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return rewards;
    }

    public long redeemReward(String userId, String rewardId, int pointsSpent) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_REWARD_USER_ID, userId);
        values.put(COLUMN_USER_REWARD_REWARD_ID, rewardId);
        values.put(COLUMN_USER_REWARD_REDEEMED_AT, String.valueOf(System.currentTimeMillis()));
        values.put(COLUMN_USER_REWARD_POINTS_SPENT, pointsSpent);
        return db.insert(TABLE_USER_REWARDS, null, values);
    }

    public boolean hasRedeemedReward(String userId, String rewardId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER_REWARDS, null,
                COLUMN_USER_REWARD_USER_ID + "=? AND " + COLUMN_USER_REWARD_REWARD_ID + "=?",
                new String[]{userId, rewardId}, null, null, null);
        boolean redeemed = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return redeemed;
    }

    public List<com.example.bloodhero.models.Reward> getRedeemedRewards(String userId) {
        List<com.example.bloodhero.models.Reward> rewards = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT r.* FROM " + TABLE_REWARDS + " r " +
                "INNER JOIN " + TABLE_USER_REWARDS + " ur ON r." + COLUMN_ID + " = ur." + COLUMN_USER_REWARD_REWARD_ID +
                " WHERE ur." + COLUMN_USER_REWARD_USER_ID + "=? " +
                "ORDER BY ur." + COLUMN_USER_REWARD_REDEEMED_AT + " DESC";
        Cursor cursor = db.rawQuery(query, new String[]{userId});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                com.example.bloodhero.models.Reward reward = new com.example.bloodhero.models.Reward(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REWARD_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REWARD_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REWARD_PARTNER)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REWARD_POINTS_COST)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REWARD_ICON_RES)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REWARD_CATEGORY))
                );
                reward.setRedeemed(true);
                rewards.add(reward);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return rewards;
    }

    // ==================== LEADERBOARD CRUD OPERATIONS ====================

    public void updateLeaderboardCache() {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Clear old cache
        db.delete(TABLE_LEADERBOARD_CACHE, null, null);
        
        // Rebuild from users table
        String query = "SELECT " + COLUMN_ID + ", " + COLUMN_USER_NAME + ", " +
                COLUMN_USER_BLOOD_TYPE + ", " + COLUMN_USER_TOTAL_DONATIONS + ", " +
                COLUMN_USER_TOTAL_POINTS +
                " FROM " + TABLE_USERS +
                " ORDER BY " + COLUMN_USER_TOTAL_POINTS + " DESC, " +
                COLUMN_USER_TOTAL_DONATIONS + " DESC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            int rank = 1;
            do {
                ContentValues values = new ContentValues();
                values.put(COLUMN_LEADERBOARD_USER_ID, cursor.getString(0));
                values.put(COLUMN_LEADERBOARD_RANK, rank++);
                values.put(COLUMN_LEADERBOARD_USERNAME, cursor.getString(1));
                values.put(COLUMN_LEADERBOARD_BLOOD_TYPE, cursor.getString(2));
                values.put(COLUMN_LEADERBOARD_TOTAL_DONATIONS, cursor.getInt(3));
                values.put(COLUMN_LEADERBOARD_TOTAL_POINTS, cursor.getInt(4));
                values.put(COLUMN_LEADERBOARD_LAST_UPDATED, System.currentTimeMillis());
                db.insert(TABLE_LEADERBOARD_CACHE, null, values);
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    public List<com.example.bloodhero.models.LeaderboardEntry> getLeaderboard(int limit) {
        List<com.example.bloodhero.models.LeaderboardEntry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_LEADERBOARD_CACHE, null, null, null, null, null,
                COLUMN_LEADERBOARD_RANK + " ASC", String.valueOf(limit));

        if (cursor != null && cursor.moveToFirst()) {
            do {
                entries.add(new com.example.bloodhero.models.LeaderboardEntry(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LEADERBOARD_RANK)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LEADERBOARD_USERNAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LEADERBOARD_USERNAME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LEADERBOARD_TOTAL_POINTS)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LEADERBOARD_TOTAL_DONATIONS))
                ));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return entries;
    }

    public int getUserRank(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_LEADERBOARD_CACHE,
                new String[]{COLUMN_LEADERBOARD_RANK},
                COLUMN_LEADERBOARD_USER_ID + "=?",
                new String[]{userId}, null, null, null);
        int rank = -1;
        if (cursor != null && cursor.moveToFirst()) {
            rank = cursor.getInt(0);
            cursor.close();
        }
        return rank;
    }
}