package com.example.bloodhero;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bloodhero.utils.UserStorage;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BloodHeroPrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";

    private TextInputLayout tilName, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnRegister, btnGoogle, btnFacebook;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        
        btnRegister = findViewById(R.id.btnRegister);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());

        tvLogin.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        btnGoogle.setOnClickListener(v -> {
            Toast.makeText(this, "Google Sign-Up coming soon!", Toast.LENGTH_SHORT).show();
        });

        btnFacebook.setOnClickListener(v -> {
            Toast.makeText(this, "Facebook Sign-Up coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void attemptRegister() {
        // Clear previous errors
        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        boolean isValid = true;

        // Validate name
        if (TextUtils.isEmpty(name)) {
            tilName.setError("Name is required");
            isValid = false;
        } else if (name.length() < 2) {
            tilName.setError("Name must be at least 2 characters");
            isValid = false;
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email");
            isValid = false;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Please confirm your password");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        if (isValid) {
            performRegistration(name, email, password);
        }
    }

    private void performRegistration(String name, String email, String password) {
        // Show loading state
        btnRegister.setEnabled(false);
        btnRegister.setText("Creating Account...");

        // Simulate registration - In production, connect to Firebase Auth
        btnRegister.postDelayed(() -> {
            // Clear any previous user data first (important for fresh account)
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear(); // Clear all previous data
            
            // Set new user data with fresh values
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.putString(KEY_USER_EMAIL, email);
            editor.putString(KEY_USER_NAME, name);
            editor.putInt("total_points", 0);
            editor.putInt("user_points", 0);
            editor.putInt("total_donations", 0);
            editor.putInt("user_donations", 0);
            editor.putBoolean("profile_complete", false);
            editor.putBoolean("is_admin", false);
            // Clear all badges
            editor.putBoolean("badge_first_drop", false);
            editor.putBoolean("badge_regular_donor", false);
            editor.putBoolean("badge_lifesaver", false);
            editor.putBoolean("badge_hero_status", false);
            editor.putBoolean("badge_marathon_donor", false);
            editor.putBoolean("badge_blood_legend", false);
            editor.putBoolean("badge_platinum_donor", false);
            editor.apply();

            // Save to UserStorage for admin sync (new user with 0 points/donations)
            UserStorage.saveUser(this, name, email, null);

            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();

            // Navigate to Profile Setup
            Intent intent = new Intent(RegisterActivity.this, ProfileSetupActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 1500);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}