package com.example.bloodhero;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.bloodhero.utils.UserStorage;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ProfileSetupActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BloodHeroPrefs";
    private static final String KEY_PROFILE_COMPLETE = "profile_complete";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_LOCATION = "user_location";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_DOB = "user_dob";
    private static final String KEY_USER_GENDER = "user_gender";
    private static final String KEY_USER_WEIGHT = "user_weight";
    private static final String KEY_USER_BLOOD_TYPE = "blood_type";

    private TextInputLayout tilName, tilPhone, tilLocation, tilEmail, tilDateOfBirth, tilWeight;
    private TextInputEditText etName, etPhone, etLocation, etEmail, etDateOfBirth, etWeight;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private MaterialButton btnSaveProfile, btnSkip;
    private ImageView ivProfilePhoto, ivAddPhoto;
    
    private MaterialButton[] bloodTypeButtons;
    private String selectedBloodType = null;
    private MaterialButton selectedButton = null;
    private Calendar selectedDateOfBirth = Calendar.getInstance();

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
        tilEmail = findViewById(R.id.tilEmail);
        tilDateOfBirth = findViewById(R.id.tilDateOfBirth);
        tilWeight = findViewById(R.id.tilWeight);
        
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etLocation = findViewById(R.id.etLocation);
        etEmail = findViewById(R.id.etEmail);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        etWeight = findViewById(R.id.etWeight);
        
        rgGender = findViewById(R.id.rgGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        
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
        String email = prefs.getString(KEY_USER_EMAIL, "");
        
        // Load from local prefs first
        String name = prefs.getString(KEY_USER_NAME, "");
        String phone = prefs.getString("user_phone", "");
        String location = prefs.getString("user_location", "");
        String bloodType = prefs.getString(KEY_USER_BLOOD_TYPE, "");
        
        // Sync from UserStorage for phone/location/bloodType (may have been updated elsewhere)
        if (!email.isEmpty()) {
            com.example.bloodhero.utils.UserStorage.UserData userData = 
                    com.example.bloodhero.utils.UserStorage.getUserByEmail(this, email);
            if (userData != null) {
                if (name.isEmpty() && userData.name != null) name = userData.name;
                if (phone.isEmpty() && userData.phone != null) phone = userData.phone;
                if (location.isEmpty() && userData.location != null) location = userData.location;
                if ((bloodType.isEmpty() || "Unknown".equals(bloodType)) && userData.bloodType != null) {
                    bloodType = userData.bloodType;
                }
            }
        }
        
        // Populate fields
        if (!TextUtils.isEmpty(name)) etName.setText(name);
        if (!TextUtils.isEmpty(phone)) etPhone.setText(phone);
        if (!TextUtils.isEmpty(location)) etLocation.setText(location);
        
        // Pre-select blood type button
        if (!TextUtils.isEmpty(bloodType) && !"Unknown".equals(bloodType)) {
            String[] bloodTypes = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
            for (int i = 0; i < bloodTypes.length; i++) {
                if (bloodTypes[i].equals(bloodType)) {
                    selectBloodType(bloodTypeButtons[i], bloodType);
                    break;
                }
            }
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

        // Date of Birth picker
        etDateOfBirth.setOnClickListener(v -> showDatePickerDialog());

        btnSaveProfile.setOnClickListener(v -> saveProfile());

        btnSkip.setOnClickListener(v -> navigateToHome());
    }

    private void showDatePickerDialog() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR) - 25; // Default to 25 years old
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDateOfBirth.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    etDateOfBirth.setText(sdf.format(selectedDateOfBirth.getTime()));
                },
                year, month, day
        );
        
        // Set max date to 18 years ago (minimum age for blood donation)
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -18);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        
        // Set min date to 65 years ago (maximum age for blood donation)
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -65);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        
        datePickerDialog.show();
    }

    private void saveProfile() {
        // Clear errors
        tilName.setError(null);
        tilPhone.setError(null);
        tilLocation.setError(null);
        tilWeight.setError(null);

        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String dob = etDateOfBirth.getText() != null ? etDateOfBirth.getText().toString().trim() : "";
        String weightStr = etWeight.getText() != null ? etWeight.getText().toString().trim() : "";
        
        String gender = "";
        if (rbMale.isChecked()) gender = "Male";
        else if (rbFemale.isChecked()) gender = "Female";

        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            tilName.setError("Name is required");
            isValid = false;
        }

        if (selectedBloodType == null) {
            Toast.makeText(this, "Please select your blood type", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        // Validate weight if provided
        if (!TextUtils.isEmpty(weightStr)) {
            try {
                double weight = Double.parseDouble(weightStr);
                if (weight < 50) {
                    tilWeight.setError("Minimum weight is 50kg for blood donation");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilWeight.setError("Invalid weight");
                isValid = false;
            }
        }

        if (isValid) {
            // Save profile data
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_USER_NAME, name);
            editor.putString(KEY_USER_PHONE, phone);
            editor.putString(KEY_USER_LOCATION, location);
            editor.putString(KEY_USER_EMAIL, email);
            editor.putString(KEY_USER_DOB, dob);
            editor.putString(KEY_USER_GENDER, gender);
            editor.putString(KEY_USER_WEIGHT, weightStr);
            editor.putString(KEY_USER_BLOOD_TYPE, selectedBloodType);
            editor.putBoolean(KEY_PROFILE_COMPLETE, true);
            editor.apply();

            // Sync user data to UserStorage for admin dashboard (with full profile)
            UserStorage.saveUser(this, name, email, selectedBloodType, phone, location);

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