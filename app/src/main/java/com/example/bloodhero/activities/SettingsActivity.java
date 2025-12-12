package com.example.bloodhero.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.bloodhero.R;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BloodHeroPrefs";
    private static final String KEY_THEME_MODE = "theme_mode";
    
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK_RED = 1;

    private ImageButton btnBack;
    private RadioGroup rgTheme;
    private RadioButton rbLightMode, rbDarkRedMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        loadCurrentSettings();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rgTheme = findViewById(R.id.rgTheme);
        rbLightMode = findViewById(R.id.rbLightMode);
        rbDarkRedMode = findViewById(R.id.rbDarkRedMode);
    }

    private void loadCurrentSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int themeMode = prefs.getInt(KEY_THEME_MODE, THEME_LIGHT);
        
        if (themeMode == THEME_DARK_RED) {
            rbDarkRedMode.setChecked(true);
        } else {
            rbLightMode.setChecked(true);
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            
            if (checkedId == R.id.rbDarkRedMode) {
                editor.putInt(KEY_THEME_MODE, THEME_DARK_RED);
                // Apply dark theme
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                editor.putInt(KEY_THEME_MODE, THEME_LIGHT);
                // Apply light theme
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            editor.apply();
        });
    }
    
    public static void applyTheme(SharedPreferences prefs) {
        int themeMode = prefs.getInt(KEY_THEME_MODE, THEME_LIGHT);
        if (themeMode == THEME_DARK_RED) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
