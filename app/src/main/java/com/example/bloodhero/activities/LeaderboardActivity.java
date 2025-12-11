package com.example.bloodhero.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodhero.R;
import com.example.bloodhero.adapters.LeaderboardAdapter;
import com.example.bloodhero.models.LeaderboardEntry;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BloodHeroPrefs";

    private ImageButton btnBack;
    private TextView tvFirstName, tvFirstPoints;
    private TextView tvSecondName, tvSecondPoints;
    private TextView tvThirdName, tvThirdPoints;
    private TextView tvYourRank, tvYourName, tvYourDonations, tvYourPoints;
    private RecyclerView rvLeaderboard;

    private LeaderboardAdapter leaderboardAdapter;
    private List<LeaderboardEntry> leaderboardList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        initViews();
        loadLeaderboard();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvFirstName = findViewById(R.id.tvFirstName);
        tvFirstPoints = findViewById(R.id.tvFirstPoints);
        tvSecondName = findViewById(R.id.tvSecondName);
        tvSecondPoints = findViewById(R.id.tvSecondPoints);
        tvThirdName = findViewById(R.id.tvThirdName);
        tvThirdPoints = findViewById(R.id.tvThirdPoints);
        tvYourRank = findViewById(R.id.tvYourRank);
        tvYourName = findViewById(R.id.tvYourName);
        tvYourDonations = findViewById(R.id.tvYourDonations);
        tvYourPoints = findViewById(R.id.tvYourPoints);
        rvLeaderboard = findViewById(R.id.rvLeaderboard);
    }

    private void loadLeaderboard() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userName = prefs.getString("user_name", "You");
        int userPoints = prefs.getInt("total_points", 0);
        int userDonations = prefs.getInt("total_donations", 0);

        // Leaderboard will be populated from database when users register
        leaderboardList = new ArrayList<>();

        // Set podium display - empty until users donate
        tvFirstName.setText("---");
        tvFirstPoints.setText("0 pts");
        tvSecondName.setText("---");
        tvSecondPoints.setText("0 pts");
        tvThirdName.setText("---");
        tvThirdPoints.setText("0 pts");

        // Show current user's rank and stats
        int userRank = userPoints > 0 ? 1 : 0;
        tvYourRank.setText(userRank > 0 ? "#" + userRank : "--");
        tvYourName.setText(userName);
        tvYourDonations.setText(userDonations + " donations");
        tvYourPoints.setText(String.format("%,d pts", userPoints));

        // If user has donations, add them to leaderboard
        if (userPoints > 0) {
            LeaderboardEntry userEntry = new LeaderboardEntry(1, "current_user", userName, null, userPoints, userDonations);
            userEntry.setCurrentUser(true);
            leaderboardList.add(userEntry);
            
            // Update podium with user if they're in top 3
            tvFirstName.setText(userName);
            tvFirstPoints.setText(String.format("%,d pts", userPoints));
        }

        leaderboardAdapter = new LeaderboardAdapter(leaderboardList);
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        rvLeaderboard.setAdapter(leaderboardAdapter);
    }

    private int calculateUserRank(int userPoints) {
        // Simple ranking logic based on sample data
        if (userPoints >= 2500) return 1;
        if (userPoints >= 2100) return 2;
        if (userPoints >= 1800) return 3;
        if (userPoints >= 1650) return 4;
        if (userPoints >= 1500) return 5;
        if (userPoints >= 1400) return 6;
        if (userPoints >= 1300) return 7;
        if (userPoints >= 1200) return 8;
        if (userPoints >= 1100) return 9;
        if (userPoints >= 1000) return 10;
        if (userPoints >= 500) return 15;
        if (userPoints >= 100) return 25;
        return 50;
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
    }
}