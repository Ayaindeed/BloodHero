package com.example.bloodhero.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bloodhero.LoginActivity;
import com.example.bloodhero.ProfileSetupActivity;
import com.example.bloodhero.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BloodHeroPrefs";

    private ImageButton btnBack, btnSettings;
    private CircleImageView ivProfilePhoto;
    private TextView tvUserName, tvUserEmail, tvBloodType;
    private TextView tvDonations, tvPoints, tvBadges;
    private LinearLayout menuEditProfile, menuNotifications, menuHelp, menuAdmin;
    private View cardAdmin;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        loadUserData();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSettings = findViewById(R.id.btnSettings);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvBloodType = findViewById(R.id.tvBloodType);
        tvDonations = findViewById(R.id.tvDonations);
        tvPoints = findViewById(R.id.tvPoints);
        tvBadges = findViewById(R.id.tvBadges);
        menuEditProfile = findViewById(R.id.menuEditProfile);
        menuNotifications = findViewById(R.id.menuNotifications);
        menuHelp = findViewById(R.id.menuHelp);
        menuAdmin = findViewById(R.id.menuAdmin);
        cardAdmin = findViewById(R.id.cardAdmin);
        btnLogout = findViewById(R.id.btnLogout);
        
        // Only show admin panel for admin user
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userEmail = prefs.getString("user_email", "");
        if ("admin@contact.me".equals(userEmail)) {
            cardAdmin.setVisibility(View.VISIBLE);
        } else {
            cardAdmin.setVisibility(View.GONE);
        }
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String email = prefs.getString("user_email", "user@email.com");
        
        // Sync from central UserStorage first
        com.example.bloodhero.utils.UserStorage.UserData userData = 
                com.example.bloodhero.utils.UserStorage.getUserByEmail(this, email);
        
        String name, bloodType;
        int donations, points;
        
        if (userData != null) {
            name = userData.name;
            bloodType = userData.bloodType;
            donations = userData.donations;
            points = userData.points;
            // Update local prefs to stay in sync
            prefs.edit()
                .putString("user_name", name)
                .putString("blood_type", bloodType)
                .putInt("total_donations", donations)
                .putInt("user_donations", donations)
                .putInt("total_points", points)
                .putInt("user_points", points)
                .apply();
        } else {
            name = prefs.getString("user_name", "User");
            bloodType = prefs.getString("blood_type", "A+");
            donations = prefs.getInt("total_donations", 0);
            points = prefs.getInt("total_points", 0);
        }
        
        // Calculate badges based on donations
        int badges = calculateUnlockedBadges(donations);

        tvUserName.setText(name);
        tvUserEmail.setText(email);
        tvBloodType.setText(bloodType);
        tvDonations.setText(String.valueOf(donations));
        tvPoints.setText(String.format("%,d", points));
        tvBadges.setText(String.valueOf(badges));
    }

    private int calculateUnlockedBadges(int donations) {
        int badges = 0;
        
        // Profile complete badge
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (prefs.getBoolean("profile_complete", false)) {
            badges++;
        }
        
        // Donation-based badges
        if (donations >= 1) badges++;
        if (donations >= 5) badges++;
        if (donations >= 10) badges++;
        if (donations >= 25) badges++;
        if (donations >= 50) badges++;
        if (donations >= 100) badges++;
        
        return badges;
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        menuEditProfile.setOnClickListener(v -> {
            // Navigate to profile setup for editing
            Intent intent = new Intent(this, ProfileSetupActivity.class);
            intent.putExtra("is_editing", true);
            startActivity(intent);
        });

        menuNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationSettingsActivity.class));
        });

        menuHelp.setOnClickListener(v -> {
            startActivity(new Intent(this, HelpSupportActivity.class));
        });

        menuAdmin.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminDashboardActivity.class));
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        // Clear login state but keep user data
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("is_logged_in", false);
        editor.apply();

        // Navigate to login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data in case profile was edited
        loadUserData();
    }
}