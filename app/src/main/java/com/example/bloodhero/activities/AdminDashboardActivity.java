package com.example.bloodhero.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BloodHeroPrefs";
    
    private ImageButton btnBack, btnSettings, btnLogout;
    private CardView cardAppointments, cardUsers, cardDonations, cardBadges, cardAnalytics;
    private TextView tvTotalUsers, tvPendingAppointments, tvTotalDonations, tvLivesSaved;
    private FloatingActionButton fabQRScanner;
    private PieChart miniPieChart;
    
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
        setupMiniChart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh stats when returning to dashboard
        loadStats();
        setupMiniChart();
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
        miniPieChart = findViewById(R.id.miniPieChart);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
        
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
        
        fabQRScanner.setOnClickListener(v -> {
            // Show options: QR Scanner or Bed Management
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Choose Action")
                    .setItems(new String[]{"Scan QR Code", "Manage Beds"}, (dialog, which) -> {
                        if (which == 0) {
                            startActivity(new Intent(this, QRScannerActivity.class));
                        } else {
                            startActivity(new Intent(this, BedManagementActivity.class));
                        }
                    })
                    .show();
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

    private void setupMiniChart() {
        // Get actual blood type distribution from registered users
        List<com.example.bloodhero.models.User> allUsers = userRepository.getAllUsers();
        Map<String, Integer> bloodTypeStats = new HashMap<>();
        
        for (com.example.bloodhero.models.User user : allUsers) {
            if (user.getBloodType() != null && !user.getBloodType().isEmpty()) {
                bloodTypeStats.put(user.getBloodType(), 
                    bloodTypeStats.getOrDefault(user.getBloodType(), 0) + 1);
            }
        }
        
        ArrayList<PieEntry> entries = new ArrayList<>();
        
        // Add data from actual users or use mock data if empty
        if (bloodTypeStats.isEmpty() || bloodTypeStats.values().stream().mapToInt(Integer::intValue).sum() == 0) {
            // Mock data for demonstration
            entries.add(new PieEntry(35f, "O+"));
            entries.add(new PieEntry(25f, "A+"));
            entries.add(new PieEntry(15f, "B+"));
            entries.add(new PieEntry(10f, "AB+"));
            entries.add(new PieEntry(8f, "O-"));
            entries.add(new PieEntry(4f, "A-"));
            entries.add(new PieEntry(2f, "B-"));
            entries.add(new PieEntry(1f, "AB-"));
        } else {
            for (Map.Entry<String, Integer> entry : bloodTypeStats.entrySet()) {
                if (entry.getValue() > 0) {
                    entries.add(new PieEntry(entry.getValue(), entry.getKey()));
                }
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{
                Color.parseColor("#E53935"), // O+
                Color.parseColor("#D32F2F"), // A+
                Color.parseColor("#C62828"), // B+
                Color.parseColor("#B71C1C"), // AB+
                Color.parseColor("#EF5350"), // O-
                Color.parseColor("#E57373"), // A-
                Color.parseColor("#EF9A9A"), // B-
                Color.parseColor("#FFCDD2")  // AB-
        });
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setDrawValues(false); // Hide values for compact view

        PieData data = new PieData(dataSet);
        miniPieChart.setData(data);
        miniPieChart.setUsePercentValues(false);
        miniPieChart.getDescription().setEnabled(false);
        miniPieChart.setDrawHoleEnabled(true);
        miniPieChart.setHoleRadius(35f);
        miniPieChart.setTransparentCircleRadius(40f);
        miniPieChart.setEntryLabelTextSize(9f);
        miniPieChart.setEntryLabelColor(Color.DKGRAY);
        miniPieChart.getLegend().setEnabled(false); // Hide legend for compact view
        miniPieChart.setRotationEnabled(false);
        miniPieChart.setHighlightPerTapEnabled(true);
        
        miniPieChart.animateY(800);
        miniPieChart.invalidate();
    }
}
