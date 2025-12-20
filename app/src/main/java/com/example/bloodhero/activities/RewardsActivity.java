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
import com.example.bloodhero.database.BloodHeroDatabaseHelper;
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
    private BloodHeroDatabaseHelper dbHelper;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        userRepository = UserRepository.getInstance(this);
        dbHelper = BloodHeroDatabaseHelper.getInstance(this);
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

        // Healthy Food & Organic Products
        allRewards.add(new Reward("1", "Organic Fruits Voucher", 
                "100 DH voucher for fresh organic fruits and vegetables",
                "Marjane Bio", 150, R.drawable.ic_gift, "organic"));
        
        allRewards.add(new Reward("2", "Fresh Juice Pack", 
                "5 fresh-pressed juice bottles (Orange, Carrot, Beetroot)",
                "Jus Atlas", 80, R.drawable.ic_gift, "organic"));
        
        allRewards.add(new Reward("3", "Protein Pack", 
                "Organic chicken breast, eggs & dairy bundle",
                "Aswak Assalam Bio", 200, R.drawable.ic_gift, "organic"));
        
        allRewards.add(new Reward("4", "Superfoods Bundle", 
                "Quinoa, chia seeds, almonds, dates variety pack",
                "Carrefour Bio", 120, R.drawable.ic_gift, "organic"));

        // Health & Wellness
        allRewards.add(new Reward("5", "Multivitamin Package", 
                "1-month supply of quality multivitamins and iron supplements",
                "Pharmacie du Centre", 100, R.drawable.ic_gift, "health"));
        
        allRewards.add(new Reward("6", "Fitness Club Pass", 
                "1-week gym membership with free training session",
                "City Club Casablanca", 180, R.drawable.ic_gift, "health"));
        
        allRewards.add(new Reward("7", "Health Screening", 
                "Free blood test: CBC, glucose, cholesterol panel",
                "Laboratoire Pasteur", 250, R.drawable.ic_gift, "health"));
        
        allRewards.add(new Reward("8", "Yoga Classes Pack", 
                "4 yoga or pilates sessions",
                "Zen Studio Rabat", 140, R.drawable.ic_gift, "health"));

        // Natural & Wellness Products
        allRewards.add(new Reward("9", "Natural Honey Set", 
                "Pure Moroccan honey collection (Euphorbia, Thyme, Carob)",
                "Miel du Maroc", 90, R.drawable.ic_gift, "wellness"));
        
        allRewards.add(new Reward("10", "Herbal Tea Collection", 
                "Premium Moroccan herbal teas (Mint, Verbena, Thyme)",
                "La Maison du Thé", 60, R.drawable.ic_gift, "wellness"));
        
        allRewards.add(new Reward("11", "Argan Oil Products", 
                "Organic argan oil beauty and wellness set",
                "Coopérative Féminine", 130, R.drawable.ic_gift, "wellness"));

        // Supermarket Vouchers
        allRewards.add(new Reward("12", "Marjane Voucher", 
                "200 DH shopping voucher for healthy groceries",
                "Marjane Hypermarché", 300, R.drawable.ic_gift, "shopping"));
        
        allRewards.add(new Reward("13", "Carrefour Bio Voucher", 
                "150 DH voucher for organic section",
                "Carrefour Market", 220, R.drawable.ic_gift, "shopping"));
        
        allRewards.add(new Reward("14", "Acima Fresh Voucher", 
                "100 DH voucher for fresh produce section",
                "Acima", 150, R.drawable.ic_gift, "shopping"));

        // Check redeemed status from database
        if (currentUser != null) {
            for (Reward reward : allRewards) {
                if (dbHelper.hasRedeemedReward(currentUser.getId(), reward.getId())) {
                    reward.setRedeemed(true);
                }
            }
        }

        filteredRewards = new ArrayList<>(allRewards);
        rewardAdapter = new RewardAdapter(filteredRewards, userPoints, this);
        rvRewards.setLayoutManager(new LinearLayoutManager(this));
        rvRewards.setAdapter(rewardAdapter);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("All"));
        tabLayout.addTab(tabLayout.newTab().setText("Organic"));
        tabLayout.addTab(tabLayout.newTab().setText("Health"));
        tabLayout.addTab(tabLayout.newTab().setText("Wellness"));
        tabLayout.addTab(tabLayout.newTab().setText("Shopping"));

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
                case 1: category = "organic"; break;
                case 2: category = "health"; break;
                case 3: category = "wellness"; break;
                case 4: category = "shopping"; break;
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
                    if (currentUser == null) {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Deduct points
                    userPoints -= reward.getPointsCost();
                    
                    // Save redemption to database
                    long result = dbHelper.redeemReward(currentUser.getId(), reward.getId(), reward.getPointsCost());
                    
                    if (result > 0) {
                        // Update user points in SQLite
                        userRepository.updatePoints(currentUser.getId(), userPoints);
                        
                        // Set expiry date (30 days from now)
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_MONTH, 30);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        String expiryDate = sdf.format(calendar.getTime());
                        
                        // Update UI
                        reward.setRedeemed(true);
                        reward.setExpiryDate(expiryDate);
                        tvPointsBalance.setText(String.format(Locale.getDefault(), "%,d", userPoints));
                        rewardAdapter.setUserPoints(userPoints);
                        
                        // Show success
                        showRedemptionSuccess(reward, expiryDate);
                    } else {
                        Toast.makeText(this, "Failed to redeem reward", Toast.LENGTH_SHORT).show();
                    }
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
