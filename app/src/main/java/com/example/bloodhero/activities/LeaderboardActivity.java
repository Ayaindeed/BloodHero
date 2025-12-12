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
import com.example.bloodhero.utils.UserStorage;

import java.util.ArrayList;
import java.util.Collections;
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
        String userEmail = prefs.getString("user_email", "");

        // Load ALL users from UserStorage and sort by points
        List<UserStorage.UserData> allUsers = UserStorage.getAllUsers(this);
        
        // Sort by points descending
        Collections.sort(allUsers, (a, b) -> Integer.compare(b.points, a.points));
        
        leaderboardList = new ArrayList<>();
        int userRank = 0;
        String userName = "You";
        int userPoints = 0;
        int userDonations = 0;
        
        for (int i = 0; i < allUsers.size(); i++) {
            UserStorage.UserData user = allUsers.get(i);
            // Skip admin users from leaderboard
            if ("admin@contact.me".equals(user.email)) continue;
            
            LeaderboardEntry entry = new LeaderboardEntry(
                leaderboardList.size() + 1, user.id, user.name, user.bloodType, user.points, user.donations);
            
            if (user.email.equals(userEmail)) {
                entry.setCurrentUser(true);
                userRank = leaderboardList.size() + 1;
                userName = user.name;
                userPoints = user.points;
                userDonations = user.donations;
            }
            leaderboardList.add(entry);
        }

        // Set podium display with top 3 users
        tvFirstName.setText("---");
        tvFirstPoints.setText("0 pts");
        tvSecondName.setText("---");
        tvSecondPoints.setText("0 pts");
        tvThirdName.setText("---");
        tvThirdPoints.setText("0 pts");
        
        if (leaderboardList.size() >= 1) {
            tvFirstName.setText(leaderboardList.get(0).getUserName());
            tvFirstPoints.setText(String.format("%,d pts", leaderboardList.get(0).getTotalPoints()));
        }
        if (leaderboardList.size() >= 2) {
            tvSecondName.setText(leaderboardList.get(1).getUserName());
            tvSecondPoints.setText(String.format("%,d pts", leaderboardList.get(1).getTotalPoints()));
        }
        if (leaderboardList.size() >= 3) {
            tvThirdName.setText(leaderboardList.get(2).getUserName());
            tvThirdPoints.setText(String.format("%,d pts", leaderboardList.get(2).getTotalPoints()));
        }

        // Show current user's rank and stats
        tvYourRank.setText(userRank > 0 ? "#" + userRank : "--");
        tvYourName.setText(userName);
        tvYourDonations.setText(userDonations + " donations");
        tvYourPoints.setText(String.format("%,d pts", userPoints));

        leaderboardAdapter = new LeaderboardAdapter(leaderboardList);
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        rvLeaderboard.setAdapter(leaderboardAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadLeaderboard();
    }
}