package com.example.bloodhero.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.bloodhero.R;
import com.example.bloodhero.utils.UserStorage;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Shows the blood journey from donation to impact
 * Stages: Donation → Processing → Testing → Storage → Thank You
 */
public class BloodJourneyActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BloodHeroPrefs";
    
    // Donation info
    private String donationId;
    private int currentStage = 5; // Default to completed for completed donations
    
    // Views
    private ImageButton btnBack;
    private Chip chipBloodType, chipLocation, chipTime;
    private TextView tvDonationDate, tvCampaignName, tvLocation;
    private TextView tvLivesSaved, tvPointsEarned, tvUnlockedBadges;
    private MaterialCardView cardBadgesUnlocked;
    private LinearLayout hospitalInfo;
    private TextView tvHospitalName;
    private MaterialButton btnShare, btnViewHistory;
    
    // Stage views
    private View stage1, stage2, stage3, stage4, stage5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_journey);

        // Get donation ID from intent
        donationId = getIntent().getStringExtra("donation_id");
        currentStage = getIntent().getIntExtra("current_stage", 5);

        initViews();
        loadDonationInfo();
        setupStages();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        chipBloodType = findViewById(R.id.chipBloodType);
        chipLocation = findViewById(R.id.chipLocation);
        chipTime = findViewById(R.id.chipTime);
        tvDonationDate = findViewById(R.id.tvDonationDate);
        tvCampaignName = findViewById(R.id.tvCampaignName);
        tvLocation = findViewById(R.id.tvLocation);
        tvLivesSaved = findViewById(R.id.tvLivesSaved);
        tvPointsEarned = findViewById(R.id.tvPointsEarned);
        tvUnlockedBadges = findViewById(R.id.tvUnlockedBadges);
        cardBadgesUnlocked = findViewById(R.id.cardBadgesUnlocked);
        hospitalInfo = findViewById(R.id.hospitalInfo);
        tvHospitalName = findViewById(R.id.tvHospitalName);
        btnShare = findViewById(R.id.btnShare);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        
        stage1 = findViewById(R.id.stage1);
        stage2 = findViewById(R.id.stage2);
        stage3 = findViewById(R.id.stage3);
        stage4 = findViewById(R.id.stage4);
        stage5 = findViewById(R.id.stage5);
    }

    private void loadDonationInfo() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Get user's blood type from intent or prefs
        String bloodType = getIntent().getStringExtra("blood_type");
        if (bloodType == null || bloodType.isEmpty()) {
            bloodType = prefs.getString("blood_type", "A+");
        }
        chipBloodType.setText("🩸 " + bloodType);
        
        // Get points from intent or default
        int points = getIntent().getIntExtra("points", 50);
        
        // Try to get donation details from intent or most recent donation
        String campaignName = getIntent().getStringExtra("campaign_name");
        String location = getIntent().getStringExtra("location");
        String date = getIntent().getStringExtra("date");
        String time = getIntent().getStringExtra("time");
        
        if (campaignName == null) {
            // Load from last completed appointment
            String userEmail = prefs.getString("user_email", "");
            List<UserStorage.AppointmentData> appointments = UserStorage.getAppointmentsByStatus(this, "Completed");
            
            if (!appointments.isEmpty()) {
                UserStorage.AppointmentData lastDonation = appointments.get(appointments.size() - 1);
                campaignName = lastDonation.campaignName;
                location = lastDonation.location;
                date = lastDonation.date;
                time = lastDonation.time;
                donationId = lastDonation.id;
            } else {
                // Fallback to mock data
                campaignName = "Centre de Transfusion Sanguine";
                location = "CHU Ibn Sina, Rabat";
                date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                time = "09:00 AM";
            }
        }
        
        tvCampaignName.setText(campaignName);
        tvLocation.setText(location);
        
        // Format date
        String formattedDate = formatDate(date) + " at " + (time != null ? time : "");
        tvDonationDate.setText(formattedDate);
        
        // Set location chip
        String city = extractCity(location);
        chipLocation.setText("📍 " + city);
        
        // Set time chip
        chipTime.setText(getTimeOfDay(time));
        
        // Calculate lives saved (3 per donation)
        tvLivesSaved.setText("3");
        tvPointsEarned.setText("+" + points + " points earned");
        
        // Check for newly unlocked badges
        checkUnlockedBadges();
    }

    private void checkUnlockedBadges() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int totalDonations = prefs.getInt("user_donations", 0);
        
        StringBuilder badgesText = new StringBuilder();
        int badgePoints = 0;
        
        // Check which badges were just unlocked
        if (totalDonations == 1) {
            badgesText.append("🏆 First Drop (+50 pts)\n");
            badgePoints += 50;
        }
        if (totalDonations == 5) {
            badgesText.append("🏆 Regular Donor (+100 pts)\n");
            badgesText.append("🏆 Lifesaver (+250 pts)\n");
            badgePoints += 350;
        }
        if (totalDonations == 10) {
            badgesText.append("🏆 Hero Status (+200 pts)\n");
            badgePoints += 200;
        }
        if (totalDonations == 25) {
            badgesText.append("🏆 Marathon Donor (+500 pts)\n");
            badgePoints += 500;
        }
        
        if (badgesText.length() > 0) {
            cardBadgesUnlocked.setVisibility(View.VISIBLE);
            tvUnlockedBadges.setText(badgesText.toString().trim());
            
            // Update total points with badge bonus
            int currentPoints = prefs.getInt("user_points", 0);
            prefs.edit().putInt("user_points", currentPoints + badgePoints).apply();
            
            tvPointsEarned.setText("+" + (50 + badgePoints) + " points earned");
        }
    }

    private void setupStages() {
        // Stage 1: The Donation
        setupStage(stage1, 1, "The Donation", "Donation is made", 
                R.drawable.ic_blood_drop, currentStage >= 1, true);
        
        // Stage 2: Processing
        setupStage(stage2, 2, "Processing", "Processing of the donation", 
                R.drawable.ic_time, currentStage >= 2, false);
        
        // Stage 3: Testing
        setupStage(stage3, 3, "Testing", "Checks to ensure donation can be used", 
                R.drawable.ic_verified, currentStage >= 3, false);
        
        // Stage 4: Storage
        setupStage(stage4, 4, "Storage", "Donation is kept before use", 
                R.drawable.ic_campaigns, currentStage >= 4, false);
        
        // Stage 5: Thank You
        setupStage(stage5, 5, "Thank You", "You've helped impact up to 3 lives", 
                R.drawable.ic_heart, currentStage >= 5, false);
        
        // Show hospital info on last stage
        if (currentStage >= 5) {
            hospitalInfo.setVisibility(View.VISIBLE);
            tvHospitalName.setText("Centre Hospitalier Universitaire");
        }
        
        // Hide connector on last stage
        View lastConnector = stage5.findViewById(R.id.lineConnector);
        if (lastConnector != null) {
            lastConnector.setVisibility(View.GONE);
        }
    }

    private void setupStage(View stageView, int stageNumber, String title, String description,
                            int iconRes, boolean isCompleted, boolean showShareButton) {
        
        TextView tvNumber = stageView.findViewById(R.id.tvStageNumber);
        TextView tvTitle = stageView.findViewById(R.id.tvStageTitle);
        TextView tvDesc = stageView.findViewById(R.id.tvStageDescription);
        ImageView ivIcon = stageView.findViewById(R.id.ivIcon);
        ImageView ivCheckmark = stageView.findViewById(R.id.ivCheckmark);
        View iconBg = stageView.findViewById(R.id.iconBackground);
        View lineConnector = stageView.findViewById(R.id.lineConnector);
        MaterialButton btnAction = stageView.findViewById(R.id.btnAction);
        
        tvNumber.setText(stageNumber + ". ");
        tvTitle.setText(title);
        tvDesc.setText(description);
        ivIcon.setImageResource(iconRes);
        
        if (isCompleted) {
            // Completed stage
            iconBg.setBackgroundResource(R.drawable.bg_journey_icon_completed);
            ivIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
            ivCheckmark.setVisibility(View.VISIBLE);
            tvTitle.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            tvDesc.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            
            if (lineConnector != null) {
                lineConnector.setBackgroundColor(ContextCompat.getColor(this, R.color.success));
            }
        } else if (stageNumber == currentStage + 1) {
            // Current/active stage
            iconBg.setBackgroundResource(R.drawable.bg_journey_icon_active);
            ivIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
            tvTitle.setTextColor(ContextCompat.getColor(this, R.color.primary));
        } else {
            // Inactive stage
            iconBg.setBackgroundResource(R.drawable.bg_journey_icon_inactive);
            ivIcon.setColorFilter(ContextCompat.getColor(this, R.color.text_hint));
            tvTitle.setTextColor(ContextCompat.getColor(this, R.color.text_hint));
            tvDesc.setTextColor(ContextCompat.getColor(this, R.color.text_hint));
        }
        
        // Show share button on first stage
        if (showShareButton && isCompleted) {
            btnAction.setVisibility(View.VISIBLE);
            btnAction.setText("Share Donation");
            btnAction.setOnClickListener(v -> shareDonation());
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnShare.setOnClickListener(v -> shareJourney());
        
        btnViewHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, DonationHistoryActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void shareDonation() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userName = prefs.getString("user_name", "A Blood Hero");
        String bloodType = prefs.getString("blood_type", "");
        
        String shareText = "🩸 I just donated blood!\n\n" +
                "Type: " + bloodType + "\n" +
                "Location: " + tvLocation.getText() + "\n\n" +
                "This donation can save up to 3 lives! 💪\n\n" +
                "Join me in becoming a blood hero! #BloodHero #DonateBlood #SaveLives";
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share your donation"));
    }

    private void shareJourney() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int totalDonations = prefs.getInt("user_donations", 0);
        int livesSaved = totalDonations * 3;
        
        String shareText = "🩸 My Blood Journey with BloodHero! 🦸\n\n" +
                "✅ Donated blood\n" +
                "✅ Processed & tested\n" +
                "✅ Ready to save lives!\n\n" +
                "🎯 Total donations: " + totalDonations + "\n" +
                "❤️ Lives impacted: " + livesSaved + "\n\n" +
                "Every drop counts! Join me in saving lives.\n" +
                "#BloodHero #BloodDonation #SaveLives #Morocco";
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share your blood journey"));
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }

    private String extractCity(String location) {
        if (location == null) return "Morocco";
        // Extract city from location string
        if (location.contains(",")) {
            String[] parts = location.split(",");
            return parts[parts.length - 1].trim();
        }
        return location;
    }

    private String getTimeOfDay(String time) {
        if (time == null) return "☀️ Morning";
        try {
            int hour = Integer.parseInt(time.split(":")[0]);
            if (time.contains("PM") && hour != 12) hour += 12;
            
            if (hour < 12) return "☀️ Morning";
            else if (hour < 17) return "🌤️ Afternoon";
            else return "🌙 Evening";
        } catch (Exception e) {
            return "☀️ Morning";
        }
    }
}
