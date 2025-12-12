package com.example.bloodhero.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.bloodhero.LoginActivity;
import com.example.bloodhero.R;
import com.example.bloodhero.utils.UserStorage;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BloodHeroPrefs";
    
    private ImageButton btnLogout, btnSettings;
    private CardView cardAppointments, cardUsers, cardDonations, cardBadges, cardCampaigns, cardReports;
    private TextView tvTotalUsers, tvPendingAppointments, tvTotalDonations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        initViews();
        setupListeners();
        loadStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh stats when returning to dashboard
        loadStats();
    }

    private void initViews() {
        btnLogout = findViewById(R.id.btnLogout);
        btnSettings = findViewById(R.id.btnSettings);
        cardAppointments = findViewById(R.id.cardAppointments);
        cardUsers = findViewById(R.id.cardUsers);
        cardDonations = findViewById(R.id.cardDonations);
        cardBadges = findViewById(R.id.cardBadges);
        cardCampaigns = findViewById(R.id.cardCampaigns);
        cardReports = findViewById(R.id.cardReports);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvPendingAppointments = findViewById(R.id.tvPendingAppointments);
        tvTotalDonations = findViewById(R.id.tvTotalDonations);
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
        
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        cardAppointments.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminAppointmentsActivity.class));
        });

        cardUsers.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminUsersActivity.class));
        });

        cardDonations.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminDonationsActivity.class));
        });

        cardBadges.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminBadgesActivity.class));
        });

        cardCampaigns.setOnClickListener(v -> {
            Toast.makeText(this, "Campaigns management coming soon!", Toast.LENGTH_SHORT).show();
        });

        cardReports.setOnClickListener(v -> {
            Toast.makeText(this, "Reports coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadStats() {
        // Get actual registered users count from UserStorage
        int userCount = UserStorage.getUserCount(this);
        // Add 1 for admin account if no users registered yet
        tvTotalUsers.setText(String.valueOf(Math.max(userCount, 1)));
        
        // Mock data for pending and donations (would come from database in real app)
        tvPendingAppointments.setText("0");
        tvTotalDonations.setText("0");
    }
    
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout from admin panel?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void performLogout() {
        // Clear login session
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.putBoolean("is_admin", false);
        editor.remove("user_email");
        editor.apply();
        
        // Navigate to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        // Show logout confirmation when pressing back
        showLogoutConfirmation();
    }
}
