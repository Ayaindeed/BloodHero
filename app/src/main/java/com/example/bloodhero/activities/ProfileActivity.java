package com.example.bloodhero.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    private LinearLayout menuEditProfile, menuNotifications, menuHelp;
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
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        String name = prefs.getString("user_name", "User");
        String email = prefs.getString("user_email", "user@email.com");
        String bloodType = prefs.getString("blood_type", "A+");
        int donations = prefs.getInt("total_donations", 0);
        int points = prefs.getInt("total_points", 0);
        
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
            Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show();
        });

        menuEditProfile.setOnClickListener(v -> {
            // Navigate to profile setup for editing
            Intent intent = new Intent(this, ProfileSetupActivity.class);
            intent.putExtra("is_editing", true);
            startActivity(intent);
        });

        menuNotifications.setOnClickListener(v -> {
            Toast.makeText(this, "Notification settings coming soon", Toast.LENGTH_SHORT).show();
        });

        menuHelp.setOnClickListener(v -> {
            Toast.makeText(this, "Help & Support coming soon", Toast.LENGTH_SHORT).show();
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