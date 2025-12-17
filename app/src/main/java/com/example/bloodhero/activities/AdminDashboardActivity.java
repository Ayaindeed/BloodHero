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
import com.example.bloodhero.repository.AppointmentRepository;
import com.example.bloodhero.repository.DonationRepository;
import com.example.bloodhero.repository.UserRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BloodHeroPrefs";
    
    private ImageButton btnBack, btnSettings, btnLogout;
    private CardView cardAppointments, cardUsers, cardDonations, cardBadges, cardAnalytics;
    private TextView tvTotalUsers, tvPendingAppointments, tvTotalDonations, tvLivesSaved;
    private FloatingActionButton fabQRScanner;
    
    private UserRepository userRepository;
    private AppointmentRepository appointmentRepository;
    private DonationRepository donationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        userRepository = UserRepository.getInstance(this);
        appointmentRepository = AppointmentRepository.getInstance(this);
        donationRepository = DonationRepository.getInstance(this);
        
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
        btnBack = findViewById(R.id.btnBack);
        btnLogout = findViewById(R.id.btnLogout);
        btnSettings = findViewById(R.id.btnSettings);
        cardAppointments = findViewById(R.id.cardAppointments);
        cardUsers = findViewById(R.id.cardUsers);
        cardDonations = findViewById(R.id.cardDonations);
        cardBadges = findViewById(R.id.cardBadges);
        cardAnalytics = findViewById(R.id.cardAnalytics);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvPendingAppointments = findViewById(R.id.tvPendingAppointments);
        tvTotalDonations = findViewById(R.id.tvTotalDonations);
        tvLivesSaved = findViewById(R.id.tvLivesSaved);
        fabQRScanner = findViewById(R.id.fabQRScanner);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
        
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
        
        fabQRScanner.setOnClickListener(v -> {
            startActivity(new Intent(this, QRScannerActivity.class));
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

        cardAnalytics.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminAnalyticsActivity.class));
        });
    }

    private void loadStats() {
        // Force fresh data from database by getting new instances
        List<com.example.bloodhero.models.User> allUsers = userRepository.getAllUsers();
        
        // Get actual registered users count from SQLite (exclude admin)
        int userCount = 0;
        for (com.example.bloodhero.models.User user : allUsers) {
            // Count only non-admin users (admin email is admin@bloodhero.com)
            if (user.getEmail() != null && !user.getEmail().equalsIgnoreCase("admin@bloodhero.com")) {
                userCount++;
            }
        }
        tvTotalUsers.setText(String.valueOf(userCount));
        
        // Get real pending appointments count (SCHEDULED + CONFIRMED status)
        int scheduledCount = appointmentRepository.getAppointmentsByStatus("SCHEDULED").size();
        int confirmedCount = appointmentRepository.getAppointmentsByStatus("CONFIRMED").size();
        int pendingCount = scheduledCount + confirmedCount;
        tvPendingAppointments.setText(String.valueOf(pendingCount));
        
        // Get real donations count from SQLite
        // Sum up all users' donation counts
        int donationsCount = 0;
        for (com.example.bloodhero.models.User user : userRepository.getAllUsers()) {
            donationsCount += user.getTotalDonations();
        }
        tvTotalDonations.setText(String.valueOf(donationsCount));
        
        // Get total badges count (simplified - showing donation count for now)
        tvLivesSaved.setText(String.valueOf(donationsCount));
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
