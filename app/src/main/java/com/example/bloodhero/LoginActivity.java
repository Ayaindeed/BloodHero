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
}