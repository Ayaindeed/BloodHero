package com.example.bloodhero.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodhero.R;
import com.example.bloodhero.adapters.BadgeAdapter;
import com.example.bloodhero.models.Badge;
import com.example.bloodhero.models.User;
import com.example.bloodhero.utils.UserHelper;

import java.util.ArrayList;
import java.util.List;

public class AchievementsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BloodHeroPrefs";

    private ImageButton btnBack, btnShare;
    private TextView tvTotalPoints, tvBadgesUnlocked, tvBadgesRemaining;
    private RecyclerView rvBadges;

    private BadgeAdapter badgeAdapter;
    private List<Badge> badgeList;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        currentUser = UserHelper.getCurrentUser(this);
        
        initViews();
        loadUserStats();
        loadBadges();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        tvTotalPoints = findViewById(R.id.tvTotalPoints);
        tvBadgesUnlocked = findViewById(R.id.tvBadgesUnlocked);
        tvBadgesRemaining = findViewById(R.id.tvBadgesRemaining);
        rvBadges = findViewById(R.id.rvBadges);
    }

    private void loadUserStats() {
        int totalPoints = currentUser != null ? currentUser.getPoints() : 0;
        tvTotalPoints.setText(String.format("%,d", totalPoints));
    }

    private void loadBadges() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        int totalDonations = currentUser != null ? currentUser.getTotalDonations() : 0;
        boolean hasProfileComplete = prefs.getBoolean("profile_complete", false);

        badgeList = new ArrayList<>();

        // Define all badges
        badgeList.add(new Badge(
                "1", "First Drop",
                "Complete your first blood donation",
                R.drawable.ic_blood_drop,
                50,
                "Complete 1 donation",
                totalDonations >= 1
        ));

        badgeList.add(new Badge(
                "2", "Profile Pro",
                "Complete your donor profile",
                R.drawable.ic_profile,
                25,
                "Complete profile setup",
                hasProfileComplete
        ));

        badgeList.add(new Badge(
                "3", "Regular Donor",
                "Donate blood 5 times",
                R.drawable.ic_heart,
                100,
                "Complete 5 donations",
                totalDonations >= 5
        ));

        badgeList.add(new Badge(
                "4", "Hero Status",
                "Donate blood 10 times",
                R.drawable.ic_star,
                200,
                "Complete 10 donations",
                totalDonations >= 10
        ));

        badgeList.add(new Badge(
                "5", "Lifesaver",
                "Save 15 lives through donations",
                R.drawable.ic_verified,
                250,
                "Complete 5 donations",
                totalDonations >= 5
        ));

        badgeList.add(new Badge(
                "6", "Champion",
                "Reach the top 10 on leaderboard",
                R.drawable.ic_leaderboard,
                300,
                "Rank in top 10",
                false
        ));

        badgeList.add(new Badge(
                "7", "Marathon Donor",
                "Donate blood 25 times",
                R.drawable.ic_achievement,
                500,
                "Complete 25 donations",
                totalDonations >= 25
        ));

        badgeList.add(new Badge(
                "8", "Blood Legend",
                "Donate blood 50 times",
                R.drawable.ic_star,
                1000,
                "Complete 50 donations",
                totalDonations >= 50
        ));

        badgeList.add(new Badge(
                "9", "Early Bird",
                "Book an appointment before 9 AM",
                R.drawable.ic_time,
                75,
                "Book early morning appointment",
                false
        ));

        badgeList.add(new Badge(
                "10", "Campaign Supporter",
                "Participate in 3 different campaigns",
                R.drawable.ic_campaigns,
                150,
                "Join 3 campaigns",
                false
        ));

        badgeList.add(new Badge(
                "11", "Community Hero",
                "Refer 5 friends to donate",
                R.drawable.ic_add_circle,
                200,
                "Refer 5 friends",
                false
        ));

        badgeList.add(new Badge(
                "12", "Platinum Donor",
                "Donate blood 100 times",
                R.drawable.ic_verified,
                2000,
                "Complete 100 donations",
                totalDonations >= 100
        ));

        // Count unlocked badges
        int unlockedCount = 0;
        for (Badge badge : badgeList) {
            if (badge.isUnlocked()) {
                unlockedCount++;
            }
        }

        tvBadgesUnlocked.setText(String.valueOf(unlockedCount));
        tvBadgesRemaining.setText(String.valueOf(badgeList.size() - unlockedCount));

        // Setup adapter
        badgeAdapter = new BadgeAdapter(badgeList, badge -> {
            String status = badge.isUnlocked() ? "Unlocked!" : "Locked - " + badge.getUnlockCriteria();
            Toast.makeText(this, 
                    badge.getName() + "\n" + badge.getDescription() + "\n" + status,
                    Toast.LENGTH_SHORT).show();
        });

        rvBadges.setLayoutManager(new GridLayoutManager(this, 3));
        rvBadges.setAdapter(badgeAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
        
        btnShare.setOnClickListener(v -> shareAchievements());
    }
    
    private void shareAchievements() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int totalDonations = prefs.getInt("total_donations", 0);
        int totalPoints = prefs.getInt("total_points", 0);
        String userName = prefs.getString("user_name", "A Blood Donor");
        
        // Count unlocked badges
        int unlockedBadges = 0;
        StringBuilder badgeNames = new StringBuilder();
        for (Badge badge : badgeList) {
            if (badge.isUnlocked()) {
                unlockedBadges++;
                if (badgeNames.length() > 0) badgeNames.append(", ");
                badgeNames.append(badge.getName());
            }
        }
        
        String shareText = "I'm a proud blood donor on BloodHero!\n\n" +
                "My Stats:\n" +
                "- Donations: " + totalDonations + "\n" +
                "- Points: " + totalPoints + "\n" +
                "- Badges Unlocked: " + unlockedBadges + "/" + badgeList.size() + "\n";
        
        if (unlockedBadges > 0) {
            shareText += "\nMy Badges: " + badgeNames.toString() + "\n";
        }
        
        shareText += "\nJoin me in saving lives! Download BloodHero and become a hero today.\n" +
                "#BloodHero #DonateBlood #SaveLives";
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My BloodHero Achievements");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        
        startActivity(Intent.createChooser(shareIntent, "Share your achievements"));
    }
}