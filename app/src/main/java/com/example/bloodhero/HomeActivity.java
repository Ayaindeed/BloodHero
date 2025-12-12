package com.example.bloodhero;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.bloodhero.activities.AchievementsActivity;
import com.example.bloodhero.activities.BloodCompatibilityActivity;
import com.example.bloodhero.activities.BloodRequestsActivity;
import com.example.bloodhero.activities.CampaignsActivity;
import com.example.bloodhero.activities.DonationHistoryActivity;
import com.example.bloodhero.activities.MyAppointmentsActivity;
import com.example.bloodhero.activities.ProfileActivity;
import com.example.bloodhero.activities.RewardsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BloodHeroPrefs";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_BLOOD_TYPE = "blood_type";
    private static final String KEY_TOTAL_DONATIONS = "total_donations";
    private static final String KEY_TOTAL_POINTS = "total_points";

    private TextView tvGreeting, tvUserName, tvBloodType, tvUnitsDonated;
    private TextView tvTotalDonations, tvTotalPoints, tvLivesSaved;
    private MaterialButton btnScheduleAppointment;
    private BottomNavigationView bottomNav;
    
    private View menuSchedule, menuManageAppointments, menuBloodGuide, menuHistory, menuUrgentRequests, menuRewards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        loadUserData();
        setupMenuItems();
        setupBottomNavigation();
        setupListeners();
    }

    private void initViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        tvUserName = findViewById(R.id.tvUserName);
        tvBloodType = findViewById(R.id.tvBloodType);
        tvUnitsDonated = findViewById(R.id.tvUnitsDonated);
        tvTotalDonations = findViewById(R.id.tvTotalDonations);
        tvTotalPoints = findViewById(R.id.tvTotalPoints);
        tvLivesSaved = findViewById(R.id.tvLivesSaved);
        btnScheduleAppointment = findViewById(R.id.btnScheduleAppointment);
        bottomNav = findViewById(R.id.bottomNav);
        
        menuSchedule = findViewById(R.id.menuSchedule);
        menuManageAppointments = findViewById(R.id.menuManageAppointments);
        menuBloodGuide = findViewById(R.id.menuBloodGuide);
        menuHistory = findViewById(R.id.menuHistory);
        menuUrgentRequests = findViewById(R.id.menuUrgentRequests);
        menuRewards = findViewById(R.id.menuRewards);
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Sync user data from central storage (may have been updated by admin)
        String email = prefs.getString("user_email", "");
        if (!email.isEmpty()) {
            syncUserDataFromStorage(email, prefs);
        }
        
        // Set greeting based on time of day
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour < 12) {
            tvGreeting.setText(R.string.greeting_morning);
        } else if (hour < 17) {
            tvGreeting.setText(R.string.greeting_afternoon);
        } else {
            tvGreeting.setText(R.string.greeting_evening);
        }
        
        // Reload prefs after sync
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Load user name
        String userName = prefs.getString(KEY_USER_NAME, "Hero");
        tvUserName.setText(userName);
        
        // Load blood type
        String bloodType = prefs.getString(KEY_USER_BLOOD_TYPE, "--");
        tvBloodType.setText(bloodType);
        
        // Load donation stats
        int totalDonations = prefs.getInt(KEY_TOTAL_DONATIONS, 0);
        int totalPoints = prefs.getInt(KEY_TOTAL_POINTS, 0);
        int livesSaved = totalDonations * 3; // Each donation saves up to 3 lives
        
        tvUnitsDonated.setText(String.valueOf(totalDonations));
        tvTotalDonations.setText(String.valueOf(totalDonations));
        tvTotalPoints.setText(String.valueOf(totalPoints));
        tvLivesSaved.setText(String.valueOf(livesSaved));
    }
    
    /**
     * Sync user data from central storage
     */
    private void syncUserDataFromStorage(String email, SharedPreferences prefs) {
        com.example.bloodhero.utils.UserStorage.UserData userData = 
                com.example.bloodhero.utils.UserStorage.getUserByEmail(this, email);
        if (userData != null) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(KEY_TOTAL_POINTS, userData.points);
            editor.putInt("user_points", userData.points);
            editor.putInt(KEY_TOTAL_DONATIONS, userData.donations);
            editor.putInt("user_donations", userData.donations);
            if (userData.name != null && !userData.name.isEmpty()) {
                editor.putString(KEY_USER_NAME, userData.name);
            }
            if (userData.bloodType != null && !userData.bloodType.isEmpty()) {
                editor.putString(KEY_USER_BLOOD_TYPE, userData.bloodType);
            }
            editor.apply();
        }
    }

    private void setupMenuItems() {
        // Schedule New Appointment
        setupMenuItem(menuSchedule, R.drawable.ic_calendar, 
                "Schedule New Appointment", 
                "Choose a time, location and donation type");
        
        // Manage Appointments
        setupMenuItem(menuManageAppointments, R.drawable.ic_time,
                "Manage Appointments",
                "Reschedule, cancel and share");
        
        // Blood Compatibility Guide
        setupMenuItem(menuBloodGuide, R.drawable.ic_blood_drop,
                "Blood Compatibility Guide",
                "See who your blood can help");
        
        // Donation History
        setupMenuItem(menuHistory, R.drawable.ic_history,
                "Donation History",
                "View your past donations");
        
        // Urgent Blood Requests
        setupMenuItem(menuUrgentRequests, R.drawable.ic_urgent,
                "Urgent Blood Requests",
                "Help those in critical need");
        
        // Rewards
        setupMenuItem(menuRewards, R.drawable.ic_gift,
                "Rewards",
                "Redeem points for gifts");
    }

    private void setupMenuItem(View menuItem, int iconRes, String title, String subtitle) {
        if (menuItem == null) return;
        
        ImageView icon = menuItem.findViewById(R.id.ivMenuIcon);
        TextView tvTitle = menuItem.findViewById(R.id.tvMenuTitle);
        TextView tvSubtitle = menuItem.findViewById(R.id.tvMenuSubtitle);
        
        if (icon != null) icon.setImageResource(iconRes);
        if (tvTitle != null) tvTitle.setText(title);
        if (tvSubtitle != null) tvSubtitle.setText(subtitle);
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_campaigns) {
                startActivity(new Intent(this, CampaignsActivity.class));
                return true;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(this, DonationHistoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_achievements) {
                startActivity(new Intent(this, AchievementsActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            
            return false;
        });
    }

    private void setupListeners() {
        btnScheduleAppointment.setOnClickListener(v -> {
            startActivity(new Intent(this, CampaignsActivity.class));
        });

        menuSchedule.setOnClickListener(v -> {
            startActivity(new Intent(this, CampaignsActivity.class));
        });

        menuManageAppointments.setOnClickListener(v -> {
            startActivity(new Intent(this, MyAppointmentsActivity.class));
        });

        menuBloodGuide.setOnClickListener(v -> {
            startActivity(new Intent(this, BloodCompatibilityActivity.class));
        });

        menuHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, DonationHistoryActivity.class));
        });

        menuUrgentRequests.setOnClickListener(v -> {
            startActivity(new Intent(this, BloodRequestsActivity.class));
        });

        menuRewards.setOnClickListener(v -> {
            startActivity(new Intent(this, RewardsActivity.class));
        });

        findViewById(R.id.ivProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_home);
        loadUserData();
    }
}