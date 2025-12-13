package com.example.bloodhero;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bloodhero.models.User;
import com.example.bloodhero.repository.UserRepository;
import com.example.bloodhero.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private static final String ADMIN_EMAIL = "admin@contact.me";
    private static final String ADMIN_PASSWORD = "admin@@@";

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoogle;
    private TextView tvForgotPassword, tvSignUp;

    private UserRepository userRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userRepository = UserRepository.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

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
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
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
        // Show loading state
        btnLogin.setEnabled(false);
        btnLogin.setText("Signing in...");

        btnLogin.postDelayed(() -> {
            // Check for admin login
            if (email.equals(ADMIN_EMAIL)) {
                if (!password.equals(ADMIN_PASSWORD)) {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Sign In");
                    Toast.makeText(this, "Invalid admin password", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Admin login successful
                sessionManager.createLoginSession("admin", true);
                navigateToAdminDashboard();
                return;
            }

            // Regular user login - check SQLite database
            User user = userRepository.loginUser(email, password);
            
            if (user == null) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Sign In");
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Login successful - create session
            sessionManager.createLoginSession(user.getId(), false);

            // Check if profile is complete
            boolean hasProfile = user.getBloodType() != null 
                    && !user.getBloodType().isEmpty() 
                    && !"Unknown".equals(user.getBloodType());

            // Navigate based on profile completion
            Intent intent;
            if (hasProfile) {
                intent = new Intent(LoginActivity.this, HomeActivity.class);
            } else {
                intent = new Intent(LoginActivity.this, ProfileSetupActivity.class);
            }
            
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 1500);
    }

    private void navigateToAdminDashboard() {
        Intent intent = new Intent(LoginActivity.this, com.example.bloodhero.activities.AdminDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}