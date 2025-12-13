package com.example.bloodhero;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.example.bloodhero.models.User;
import com.example.bloodhero.utils.UserHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {

    private User currentUser;

    private CircleImageView ivProfile;
    private TextView tvGreeting, tvUserName, tvBloodType, tvUnitsDonated;
    private TextView tvTotalDonations, tvTotalPoints, tvLivesSaved;
    private MaterialButton btnScheduleAppointment;
    private BottomNavigationView bottomNav;
    
    private View menuSchedule, menuManageAppointments, menuBloodGuide, menuHistory, menuUrgentRequests, menuRewards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        currentUser = UserHelper.getCurrentUser(this);

        initViews();
        loadUserData();
        setupMenuItems();
        setupBottomNavigation();
        setupListeners();
    }

    private void initViews() {
        ivProfile = findViewById(R.id.ivProfile);
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
        if (currentUser == null) {
            tvUserName.setText("Hero");
            tvBloodType.setText("--");
            return;
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
        
        // Load user data from SQLite
        String userName = currentUser.getName() != null ? currentUser.getName() : "Hero";
        tvUserName.setText(userName);
        
        String bloodType = currentUser.getBloodType() != null ? currentUser.getBloodType() : "--";
        tvBloodType.setText(bloodType);
        
        // Load donation stats from SQLite
        int totalDonations = currentUser.getTotalDonations();
        int totalPoints = currentUser.getTotalPoints();
        int livesSaved = currentUser.getLivesSaved();
        
        tvUnitsDonated.setText(String.valueOf(totalDonations));
        tvTotalDonations.setText(String.valueOf(totalDonations));
        tvTotalPoints.setText(String.valueOf(totalPoints));
        tvLivesSaved.setText(String.valueOf(livesSaved));
        
        // Load profile image
        loadProfileImage();
    }
    
    private void loadProfileImage() {
        if (currentUser != null && currentUser.getProfileImageUrl() != null) {
            String photoPath = currentUser.getProfileImageUrl();
            File photoFile = new File(photoPath);
            if (photoFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
                if (bitmap != null) {
                    ivProfile.setImageBitmap(bitmap);
                }
            }
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
        // Reload user from database to get latest data
        currentUser = UserHelper.getCurrentUser(this);
        bottomNav.setSelectedItemId(R.id.nav_home);
        loadUserData();
    }
}