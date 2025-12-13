package com.example.bloodhero.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodhero.R;
import com.example.bloodhero.adapters.RewardAdapter;
import com.example.bloodhero.models.Reward;
import com.example.bloodhero.models.User;
import com.example.bloodhero.repository.UserRepository;
import com.example.bloodhero.utils.UserHelper;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RewardsActivity extends AppCompatActivity implements RewardAdapter.OnRewardClickListener {

    private static final String PREFS_NAME = "BloodHeroPrefs";

    private ImageButton btnBack;
    private TextView tvPointsBalance;
    private TabLayout tabLayout;
    private RecyclerView rvRewards;

    private RewardAdapter rewardAdapter;
    private List<Reward> allRewards;
    private List<Reward> filteredRewards;
    private int userPoints;
    
    private UserRepository userRepository;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        userRepository = UserRepository.getInstance(this);
        currentUser = UserHelper.getCurrentUser(this);
        
        initViews();
        loadUserPoints();
        setupRewards();
        setupTabs();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvPointsBalance = findViewById(R.id.tvPointsBalance);
        tabLayout = findViewById(R.id.tabLayout);
        rvRewards = findViewById(R.id.rvRewards);
    }

    private void loadUserPoints() {
        if (currentUser != null) {
            userPoints = currentUser.getPoints();
        } else {
            userPoints = 0;
        }
        
        tvPointsBalance.setText(String.format(Locale.getDefault(), "%,d", userPoints));
    }

    private void setupRewards() {
        allRewards = new ArrayList<>();

        // Food & Beverages
        allRewards.add(new Reward("1", "Free Coffee", 
                "Enjoy a free coffee at any participating cafe",
                "Cafe Marrakech", 50, R.drawable.ic_gift, "food"));
        
        allRewards.add(new Reward("2", "Restaurant Voucher", 
                "10% off at selected restaurants",
                "La Sqala", 100, R.drawable.ic_gift, "food"));
        
        allRewards.add(new Reward("3", "Juice Bar Treat", 
                "Free fresh juice of your choice",
                "Fresh Morocco", 40, R.drawable.ic_gift, "food"));

        // Shopping
        allRewards.add(new Reward("4", "Supermarket Voucher", 
                "50 DH shopping voucher",
                "Marjane", 200, R.drawable.ic_gift, "shopping"));
        
        allRewards.add(new Reward("5", "Clothing Discount", 
                "15% off your next purchase",
                "Morocco Mall", 150, R.drawable.ic_gift, "shopping"));

        // Health & Wellness
        allRewards.add(new Reward("6", "Gym Day Pass", 
                "Free day pass at partner gyms",
                "City Club", 100, R.drawable.ic_gift, "health"));
        
        allRewards.add(new Reward("7", "Pharmacy Discount", 
                "10% off vitamins and supplements",
                "Pharmacie Centrale", 75, R.drawable.ic_gift, "health"));
        
        allRewards.add(new Reward("8", "Health Checkup", 
                "Free basic health screening",
                "Clinique Atlas", 300, R.drawable.ic_gift, "health"));

        // Entertainment
        allRewards.add(new Reward("9", "Cinema Ticket", 
                "Free movie ticket",
                "Megarama", 120, R.drawable.ic_gift, "entertainment"));
        
        allRewards.add(new Reward("10", "Streaming Voucher", 
                "1 month free streaming subscription",
                "Partner Streaming", 250, R.drawable.ic_gift, "entertainment"));

        // Check redeemed status
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        for (Reward reward : allRewards) {
            if (prefs.getBoolean("reward_redeemed_" + reward.getId(), false)) {
                reward.setRedeemed(true);
            }
        }

        filteredRewards = new ArrayList<>(allRewards);
        rewardAdapter = new RewardAdapter(filteredRewards, userPoints, this);
        rvRewards.setLayoutManager(new LinearLayoutManager(this));
        rvRewards.setAdapter(rewardAdapter);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("All"));
        tabLayout.addTab(tabLayout.newTab().setText("Food"));
        tabLayout.addTab(tabLayout.newTab().setText("Shopping"));
        tabLayout.addTab(tabLayout.newTab().setText("Health"));
        tabLayout.addTab(tabLayout.newTab().setText("Entertainment"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterRewards(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void filterRewards(int tabPosition) {
        filteredRewards.clear();

        if (tabPosition == 0) {
            filteredRewards.addAll(allRewards);
        } else {
            String category = "";
            switch (tabPosition) {
                case 1: category = "food"; break;
                case 2: category = "shopping"; break;
                case 3: category = "health"; break;
                case 4: category = "entertainment"; break;
            }
            for (Reward reward : allRewards) {
                if (reward.getCategory().equals(category)) {
                    filteredRewards.add(reward);
                }
            }
        }

        rewardAdapter.notifyDataSetChanged();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onRedeemClick(Reward reward, int position) {
        if (userPoints < reward.getPointsCost()) {
            Toast.makeText(this, "Not enough points!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Redeem Reward")
                .setMessage("Redeem \"" + reward.getName() + "\" for " + reward.getPointsCost() + " points?\n\nPartner: " + reward.getPartnerName())
                .setPositiveButton("Redeem", (dialog, which) -> {
                    // Deduct points
                    userPoints -= reward.getPointsCost();
                    
                    // Save to SharedPreferences (for local reward tracking)
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("reward_redeemed_" + reward.getId(), true);
                    
                    // Update user points in SQLite
                    if (currentUser != null) {
                        userRepository.updatePoints(currentUser.getId(), userPoints);
                    }
                    
                    // Set expiry date (30 days from now)
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_MONTH, 30);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String expiryDate = sdf.format(calendar.getTime());
                    editor.putString("reward_expiry_" + reward.getId(), expiryDate);
                    editor.apply();
                    
                    // Update UI
                    reward.setRedeemed(true);
                    reward.setExpiryDate(expiryDate);
                    tvPointsBalance.setText(String.format(Locale.getDefault(), "%,d", userPoints));
                    rewardAdapter.setUserPoints(userPoints);
                    
                    // Show success
                    showRedemptionSuccess(reward, expiryDate);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showRedemptionSuccess(Reward reward, String expiryDate) {
        new AlertDialog.Builder(this)
                .setTitle("Reward Redeemed!")
                .setMessage("Congratulations! You've redeemed:\n\n" + 
                        reward.getName() + "\n" +
                        "Partner: " + reward.getPartnerName() + "\n\n" +
                        "Valid until: " + expiryDate + "\n\n" +
                        "Show this confirmation at the partner location.")
                .setPositiveButton("OK", null)
                .show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh points when returning to this screen
        loadUserPoints();
        if (rewardAdapter != null) {
            rewardAdapter.setUserPoints(userPoints);
        }
    }
}
