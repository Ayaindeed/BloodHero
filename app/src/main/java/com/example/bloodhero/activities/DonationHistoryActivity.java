package com.example.bloodhero.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodhero.R;
import com.example.bloodhero.adapters.DonationAdapter;
import com.example.bloodhero.models.Donation;
import com.example.bloodhero.utils.UserStorage;

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
        String userEmail = prefs.getString("user_email", "");
        
        // Sync from central UserStorage first
        UserStorage.UserData userData = UserStorage.getUserByEmail(this, userEmail);
        int totalDonations;
        
        if (userData != null) {
            totalDonations = userData.donations;
            // Update local prefs to stay in sync
            prefs.edit()
                .putInt("total_donations", totalDonations)
                .putInt("user_donations", totalDonations)
                .apply();
        } else {
            totalDonations = prefs.getInt("total_donations", 0);
        }
        
        // Also count from actual donation records as fallback
        List<UserStorage.DonationData> donations = UserStorage.getDonationsByEmail(this, userEmail);
        if (donations.size() > totalDonations) {
            totalDonations = donations.size();
        }
        
        int livesSaved = totalDonations * 3; // Each donation can save up to 3 lives

        tvTotalDonations.setText(String.valueOf(totalDonations));
        tvLivesSaved.setText(String.valueOf(livesSaved));
    }

    private void loadDonations() {
        donationList = new ArrayList<>();

        // Load real donations from UserStorage
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userEmail = prefs.getString("user_email", "");
        
        List<UserStorage.DonationData> savedDonations = UserStorage.getDonationsByEmail(this, userEmail);
        
        for (UserStorage.DonationData donationData : savedDonations) {
            donationList.add(new Donation(
                    donationData.id,
                    donationData.userEmail,
                    donationData.campaignName,
                    donationData.campaignName,
                    donationData.location,
                    donationData.date,
                    donationData.bloodType,
                    donationData.pointsEarned,
                    "COMPLETED"
            ));
        }

        if (donationList.isEmpty()) {
            rvDonations.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvDonations.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);

            donationAdapter = new DonationAdapter(donationList, donation -> {
                // Open Blood Journey Activity to show donation journey
                Intent intent = new Intent(this, BloodJourneyActivity.class);
                intent.putExtra("donation_id", donation.getId());
                intent.putExtra("campaign_name", donation.getCampaignName());
                intent.putExtra("location", donation.getLocation());
                intent.putExtra("date", donation.getDate());
                intent.putExtra("blood_type", donation.getBloodType());
                intent.putExtra("points", donation.getPointsEarned());
                startActivity(intent);
            });

            rvDonations.setLayoutManager(new LinearLayoutManager(this));
            rvDonations.setAdapter(donationAdapter);
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh stats when returning to this screen
        loadUserStats();
    }
}