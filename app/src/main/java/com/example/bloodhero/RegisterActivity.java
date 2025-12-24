package com.example.bloodhero;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bloodhero.models.User;
import com.example.bloodhero.repository.UserRepository;
import com.example.bloodhero.utils.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnRegister, btnGoogle, btnFacebook;
    private TextView tvLogin;

    private UserRepository userRepository;
    private SessionManager sessionManager;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private boolean isProcessingGoogleSignIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userRepository = UserRepository.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        initViews();
        configureGoogleSignIn();
        setupGoogleSignInLauncher();
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
            signInWithGoogle();
        });

        btnFacebook.setOnClickListener(v -> {
            Toast.makeText(this, "Facebook Sign-Up coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void configureGoogleSignIn() {
        String clientId = BuildConfig.GOOGLE_CLIENT_ID;

        if (clientId == null || clientId.isEmpty() || clientId.equals("YOUR_GOOGLE_CLIENT_ID_HERE")) {
            Log.w("RegisterActivity", "Google Client ID not configured. Please add it to local.properties");
            btnGoogle.setEnabled(false);
            btnGoogle.setAlpha(0.5f);
            return;
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupGoogleSignInLauncher() {
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d("RegisterActivity", "Launcher callback fired with result code: " + result.getResultCode());
                    if (isProcessingGoogleSignIn) {
                        Log.d("RegisterActivity", "Already processing, ignoring duplicate callback");
                        return;
                    }
                    
                    // Always try to process the task, even if activity result is not OK
                    Intent data = result.getData();
                    if (data != null) {
                        try {
                            Log.d("RegisterActivity", "Processing Google Sign-In task...");
                            isProcessingGoogleSignIn = true;
                            btnGoogle.setEnabled(false);
                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                            handleGoogleSignInResult(task);
                        } catch (Exception e) {
                            Log.e("RegisterActivity", "Exception processing task: ", e);
                            isProcessingGoogleSignIn = false;
                            btnGoogle.setEnabled(true);
                            Toast.makeText(this, "Sign-In error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("RegisterActivity", "No intent data, result cancelled or failed");
                        isProcessingGoogleSignIn = false;
                        btnGoogle.setEnabled(true);
                    }
                }
        );
    }

    private void signInWithGoogle() {
        if (googleSignInClient == null) {
            Log.e("RegisterActivity", "googleSignInClient is null");
            Toast.makeText(this, "Google Sign-In not configured. Please check local.properties", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d("RegisterActivity", "signInWithGoogle called, signing out first to show account picker");
        btnGoogle.setEnabled(false);
        
        // Sign out first to force account picker
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            isProcessingGoogleSignIn = false;
            Toast.makeText(this, "Opening Google Sign-In...", Toast.LENGTH_SHORT).show();
            Intent signInIntent = googleSignInClient.getSignInIntent();
            Log.d("RegisterActivity", "Launching Google Sign-In intent");
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            String email = account.getEmail();
            String displayName = account.getDisplayName();
            String googleId = account.getId();

            if (email == null) {
                Toast.makeText(this, "Unable to get email from Google account", Toast.LENGTH_SHORT).show();
                return;
            }

            User existingUser = userRepository.getUserByEmail(email);

            if (existingUser != null) {
                sessionManager.createLoginSession(existingUser.getId(), false);

                boolean hasProfile = existingUser.getBloodType() != null
                        && !existingUser.getBloodType().isEmpty()
                        && !"Unknown".equals(existingUser.getBloodType());

                Intent intent = hasProfile
                        ? new Intent(RegisterActivity.this, HomeActivity.class)
                        : new Intent(RegisterActivity.this, ProfileSetupActivity.class);

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                createGoogleUser(email, displayName, googleId);
            }

        } catch (ApiException e) {
            Log.e("RegisterActivity", "Google sign in failed", e);
            isProcessingGoogleSignIn = false;
            btnGoogle.setEnabled(true);
            Toast.makeText(this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void createGoogleUser(String email, String displayName, String googleId) {
        try {
            Log.d("RegisterActivity", "Creating new Google user: " + email);
            String tempPassword = "google_" + googleId;

            String userName = displayName;
            if (userName == null || userName.isEmpty()) {
                userName = email.split("@")[0];
            }

            Log.d("RegisterActivity", "Registering user with name: " + userName);
            User newUser = userRepository.registerUser(email, tempPassword, userName, "Unknown");

            if (newUser != null) {
                Log.d("RegisterActivity", "User registered successfully");
                User createdUser = userRepository.getUserByEmail(email);
                if (createdUser != null) {
                    Log.d("RegisterActivity", "User found after creation, creating session");
                    sessionManager.createLoginSession(createdUser.getId(), false);

                    Intent intent = new Intent(RegisterActivity.this, ProfileSetupActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Log.e("RegisterActivity", "User not found after creation");
                    Toast.makeText(this, "Failed to retrieve created account", Toast.LENGTH_SHORT).show();
                    isProcessingGoogleSignIn = false;
                    btnGoogle.setEnabled(true);
                }
            } else {
                Log.e("RegisterActivity", "Failed to register user");
                Toast.makeText(this, "Failed to create account with Google", Toast.LENGTH_SHORT).show();
                isProcessingGoogleSignIn = false;
                btnGoogle.setEnabled(true);
            }
        } catch (Exception e) {
            Log.e("RegisterActivity", "Error creating Google user", e);
            isProcessingGoogleSignIn = false;
            btnGoogle.setEnabled(true);
            Toast.makeText(this, "Error creating account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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

        btnRegister.postDelayed(() -> {
            // Register user in SQLite
            User user = userRepository.registerUser(email, password, name, "Unknown");
            
            if (user == null) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Create Account");
                Toast.makeText(this, "Account already exists with this email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create login session
            sessionManager.createLoginSession(user.getId(), false);

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