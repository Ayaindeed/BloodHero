package com.example.bloodhero;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BloodHeroPrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_PROFILE_COMPLETE = "profile_complete";

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoogle;
    private TextView tvForgotPassword, tvSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignUp = findViewById(R.id.tvSignUp);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Password reset functionality coming soon!", Toast.LENGTH_SHORT).show();
        });

        btnGoogle.setOnClickListener(v -> {
            Toast.makeText(this, "Google Sign-In coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void attemptLogin() {
        // Clear previous errors
        tilEmail.setError(null);
        tilPassword.setError(null);

        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        boolean isValid = true;

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

        if (isValid) {
            performLogin(email, password);
        }
    }

    private void performLogin(String email, String password) {
        // Admin credentials check
        final String ADMIN_EMAIL = "admin@contact.me";
        final String ADMIN_PASSWORD = "admin@@@";
        
        // Show loading state
        btnLogin.setEnabled(false);
        btnLogin.setText("Signing in...");

        // Simulate login - In production, connect to Firebase Auth
        btnLogin.postDelayed(() -> {
            // Check for admin login
            if (email.equals(ADMIN_EMAIL)) {
                if (!password.equals(ADMIN_PASSWORD)) {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Sign In");
                    Toast.makeText(this, "Invalid admin password", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // For regular users, accept any valid email/password format
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.putString(KEY_USER_EMAIL, email);
            
            // Mark admin user
            if (email.equals(ADMIN_EMAIL)) {
                editor.putBoolean("is_admin", true);
                editor.putString("user_name", "Admin");
                editor.putBoolean(KEY_PROFILE_COMPLETE, true);
            } else {
                // Sync user data from stored user preferences (updated by admin)
                syncUserDataFromStorage(email, editor);
            }
            editor.apply();

            // Check if admin login
            boolean isAdmin = email.equals(ADMIN_EMAIL);
            
            Intent intent;
            if (isAdmin) {
                // Admin goes directly to Admin Dashboard
                intent = new Intent(LoginActivity.this, com.example.bloodhero.activities.AdminDashboardActivity.class);
            } else {
                // Regular users check if profile is complete
                boolean profileComplete = prefs.getBoolean(KEY_PROFILE_COMPLETE, false);
                if (profileComplete) {
                    intent = new Intent(LoginActivity.this, HomeActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, ProfileSetupActivity.class);
                }
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 1500);
    }
    
    /**
     * Sync user data from storage that may have been updated by admin
     */
    private void syncUserDataFromStorage(String email, SharedPreferences.Editor editor) {
        // Check for per-user stored data (updated by admin when marking donations complete)
        String sanitizedEmail = email.replace("@", "_at_").replace(".", "_dot_");
        SharedPreferences donorPrefs = getSharedPreferences("user_data_" + sanitizedEmail, MODE_PRIVATE);
        
        if (donorPrefs.getBoolean("data_updated", false)) {
            // Sync points and donations from admin-updated storage
            int storedPoints = donorPrefs.getInt("user_points", 0);
            int storedDonations = donorPrefs.getInt("user_donations", 0);
            
            editor.putInt("user_points", storedPoints);
            editor.putInt("user_donations", storedDonations);
            editor.putInt("total_donations", storedDonations);
            
            // Sync badges
            if (donorPrefs.getBoolean("badge_first_drop", false)) {
                editor.putBoolean("badge_first_drop", true);
            }
            if (donorPrefs.getBoolean("badge_regular_donor", false)) {
                editor.putBoolean("badge_regular_donor", true);
            }
            if (donorPrefs.getBoolean("badge_lifesaver", false)) {
                editor.putBoolean("badge_lifesaver", true);
            }
            if (donorPrefs.getBoolean("badge_hero_status", false)) {
                editor.putBoolean("badge_hero_status", true);
            }
            if (donorPrefs.getBoolean("badge_marathon_donor", false)) {
                editor.putBoolean("badge_marathon_donor", true);
            }
            if (donorPrefs.getBoolean("badge_blood_legend", false)) {
                editor.putBoolean("badge_blood_legend", true);
            }
            if (donorPrefs.getBoolean("badge_platinum_donor", false)) {
                editor.putBoolean("badge_platinum_donor", true);
            }
        }
        
        // Also check in central user storage for the latest data
        com.example.bloodhero.utils.UserStorage.UserData userData = 
                com.example.bloodhero.utils.UserStorage.getUserByEmail(this, email);
        if (userData != null) {
            editor.putInt("user_points", userData.points);
            editor.putInt("total_points", userData.points);
            editor.putInt("user_donations", userData.donations);
            editor.putInt("total_donations", userData.donations);
            editor.putString("user_name", userData.name);
            editor.putString("blood_type", userData.bloodType);
            // Sync phone and location
            if (userData.phone != null && !userData.phone.isEmpty()) {
                editor.putString("user_phone", userData.phone);
            }
            if (userData.location != null && !userData.location.isEmpty()) {
                editor.putString("user_location", userData.location);
            }
            editor.putBoolean(KEY_PROFILE_COMPLETE, true);
        }
    }
}