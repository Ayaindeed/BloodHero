package com.example.bloodhero.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.bloodhero.R;

public class NotificationSettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BloodHeroPrefs";
    private static final String KEY_NOTIF_REMINDERS = "notif_reminders";
    private static final String KEY_NOTIF_CAMPAIGNS = "notif_campaigns";
    private static final String KEY_NOTIF_ACHIEVEMENTS = "notif_achievements";
    private static final String KEY_NOTIF_UPDATES = "notif_updates";

    private ImageButton btnBack;
    private SwitchCompat switchReminders, switchCampaigns, switchAchievements, switchUpdates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        initViews();
        loadSettings();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        switchReminders = findViewById(R.id.switchReminders);
        switchCampaigns = findViewById(R.id.switchCampaigns);
        switchAchievements = findViewById(R.id.switchAchievements);
        switchUpdates = findViewById(R.id.switchUpdates);
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        switchReminders.setChecked(prefs.getBoolean(KEY_NOTIF_REMINDERS, true));
        switchCampaigns.setChecked(prefs.getBoolean(KEY_NOTIF_CAMPAIGNS, true));
        switchAchievements.setChecked(prefs.getBoolean(KEY_NOTIF_ACHIEVEMENTS, true));
        switchUpdates.setChecked(prefs.getBoolean(KEY_NOTIF_UPDATES, false));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        switchReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference(KEY_NOTIF_REMINDERS, isChecked);
        });

        switchCampaigns.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference(KEY_NOTIF_CAMPAIGNS, isChecked);
        });

        switchAchievements.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference(KEY_NOTIF_ACHIEVEMENTS, isChecked);
        });

        switchUpdates.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference(KEY_NOTIF_UPDATES, isChecked);
        });
    }

    private void savePreference(String key, boolean value) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(key, value).apply();
    }
}
