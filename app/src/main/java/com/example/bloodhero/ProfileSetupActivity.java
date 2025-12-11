package com.example.bloodhero;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ProfileSetupActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BloodHeroPrefs";
    private static final String KEY_PROFILE_COMPLETE = "profile_complete";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_LOCATION = "user_location";
    private static final String KEY_USER_BLOOD_TYPE = "blood_type";

    private TextInputLayout tilName, tilPhone, tilLocation;
    private TextInputEditText etName, etPhone, etLocation;
    private MaterialButton btnSaveProfile, btnSkip;
    private ImageView ivProfilePhoto, ivAddPhoto;
    
    private MaterialButton[] bloodTypeButtons;
    private String selectedBloodType = null;
    private MaterialButton selectedButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        initViews();
        loadExistingData();
        setupBloodTypeButtons();
        setupListeners();
    }

    private void initViews() {
        tilName = findViewById(R.id.tilName);
        tilPhone = findViewById(R.id.tilPhone);
        tilLocation = findViewById(R.id.tilLocation);
        
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etLocation = findViewById(R.id.etLocation);
        
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnSkip = findViewById(R.id.btnSkip);
        
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        ivAddPhoto = findViewById(R.id.ivAddPhoto);
        
        // Blood type buttons
        bloodTypeButtons = new MaterialButton[]{
                findViewById(R.id.btnBloodAPos),
                findViewById(R.id.btnBloodANeg),
                findViewById(R.id.btnBloodBPos),
                findViewById(R.id.btnBloodBNeg),
                findViewById(R.id.btnBloodABPos),
                findViewById(R.id.btnBloodABNeg),
                findViewById(R.id.btnBloodOPos),
                findViewById(R.id.btnBloodONeg)
        };
    }

    private void loadExistingData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String name = prefs.getString(KEY_USER_NAME, "");
        if (!TextUtils.isEmpty(name)) {
            etName.setText(name);
        }
    }

    private void setupBloodTypeButtons() {
        String[] bloodTypes = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        
        for (int i = 0; i < bloodTypeButtons.length; i++) {
            final String bloodType = bloodTypes[i];
            final MaterialButton button = bloodTypeButtons[i];
            
            button.setOnClickListener(v -> selectBloodType(button, bloodType));
        }
    }

    private void selectBloodType(MaterialButton button, String bloodType) {
        // Reset previous selection
        if (selectedButton != null) {
            selectedButton.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.transparent));
            selectedButton.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            selectedButton.setStrokeColorResource(R.color.divider);
        }
        
        // Set new selection
        selectedButton = button;
        selectedBloodType = bloodType;
        
        button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary));
        button.setTextColor(ContextCompat.getColor(this, R.color.text_on_primary));
        button.setStrokeColorResource(R.color.primary);
    }

    private void setupListeners() {
        ivAddPhoto.setOnClickListener(v -> {
            Toast.makeText(this, "Photo selection coming soon!", Toast.LENGTH_SHORT).show();
        });

        btnSaveProfile.setOnClickListener(v -> saveProfile());

        btnSkip.setOnClickListener(v -> navigateToHome());
    }

    private void saveProfile() {
        // Clear errors
        tilName.setError(null);
        tilPhone.setError(null);
        tilLocation.setError(null);

        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";

        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            tilName.setError("Name is required");
            isValid = false;
        }

        if (selectedBloodType == null) {
            Toast.makeText(this, "Please select your blood type", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (isValid) {
            // Save profile data
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_USER_NAME, name);
            editor.putString(KEY_USER_PHONE, phone);
            editor.putString(KEY_USER_LOCATION, location);
            editor.putString(KEY_USER_BLOOD_TYPE, selectedBloodType);
            editor.putBoolean(KEY_PROFILE_COMPLETE, true);
            editor.apply();

            Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
            navigateToHome();
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(ProfileSetupActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}