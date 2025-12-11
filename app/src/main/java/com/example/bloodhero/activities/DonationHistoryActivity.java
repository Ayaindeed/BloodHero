package com.example.bloodhero.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodhero.R;
import com.example.bloodhero.adapters.DonationAdapter;
import com.example.bloodhero.models.Donation;

import java.util.ArrayList;
import java.util.List;

public class DonationHistoryActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BloodHeroPrefs";

    private ImageButton btnBack;
    private TextView tvTotalDonations, tvLivesSaved;
    private RecyclerView rvDonations;
    private LinearLayout emptyState;

    private DonationAdapter donationAdapter;
    private List<Donation> donationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_history);

        initViews();
        loadUserStats();
        loadDonations();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTotalDonations = findViewById(R.id.tvTotalDonations);
        tvLivesSaved = findViewById(R.id.tvLivesSaved);
        rvDonations = findViewById(R.id.rvDonations);
        emptyState = findViewById(R.id.emptyState);
    }

    private void loadUserStats() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int totalDonations = prefs.getInt("total_donations", 0);
        int livesSaved = totalDonations * 3; // Each donation can save up to 3 lives

        tvTotalDonations.setText(String.valueOf(totalDonations));
        tvLivesSaved.setText(String.valueOf(livesSaved));
    }

    private void loadDonations() {
        donationList = new ArrayList<>();

        // Donations will be loaded from database when user completes donations
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int totalDonations = prefs.getInt("total_donations", 0);
        String bloodType = prefs.getString("blood_type", "A+");

        // In production, donations would be loaded from BloodHeroDatabaseHelper
        // For now, show empty state - users will see their actual donations
        if (false && totalDonations > 0) {
            // Placeholder for database-loaded donations
            donationList.add(new Donation(
                    "1", "user1", "camp1",
                    "Centre de Transfusion Sanguine",
                    "CHU Ibn Sina, Rabat",
                    "2025-01-10",
                    bloodType,
                    100,
                    "COMPLETED"
            ));

            if (totalDonations >= 2) {
                donationList.add(new Donation(
                        "2", "user1", "camp2",
                        "Hôpital Cheikh Khalifa",
                        "Casablanca",
                        "2024-10-15",
                        bloodType,
                        100,
                        "COMPLETED"
                ));
            }

            if (totalDonations >= 3) {
                donationList.add(new Donation(
                        "3", "user1", "camp3",
                        "Université Mohammed V",
                        "Rabat",
                        "2024-07-20",
                        bloodType,
                        100,
                        "COMPLETED"
                ));
            }
        }

        if (donationList.isEmpty()) {
            rvDonations.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvDonations.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);

            donationAdapter = new DonationAdapter(donationList, donation -> {
                // Show donation details
                Toast.makeText(this, 
                        "Donation at " + donation.getCampaignName() + "\n" +
                        "Points earned: " + donation.getPointsEarned(),
                        Toast.LENGTH_SHORT).show();
            });

            rvDonations.setLayoutManager(new LinearLayoutManager(this));
            rvDonations.setAdapter(donationAdapter);
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
    }
}