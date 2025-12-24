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

public class LoginActivity extends AppCompatActivity {

    private static final String ADMIN_EMAIL = "admin@contact.me";
    private static final String ADMIN_PASSWORD = "admin@@@";

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoogle;
    private TextView tvForgotPassword, tvSignUp;

    private UserRepository userRepository;
    private SessionManager sessionManager;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private boolean isProcessingGoogleSignIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userRepository = UserRepository.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        initViews();
        configureGoogleSignIn();
        setupGoogleSignInLauncher();
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

        btnGoogle.setOnClickListener(v -> signInWithGoogle());
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

    private void configureGoogleSignIn() {
        String clientId = BuildConfig.GOOGLE_CLIENT_ID;
        
        if (clientId == null || clientId.isEmpty() || clientId.equals("YOUR_GOOGLE_CLIENT_ID_HERE")) {
            Log.w("LoginActivity", "Google Client ID not configured. Please add it to local.properties");
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
                    Log.d("LoginActivity", "Launcher callback fired with result code: " + result.getResultCode());
                    if (isProcessingGoogleSignIn) {
                        Log.d("LoginActivity", "Already processing, ignoring duplicate callback");
                        return;
                    }
                    
                    Intent data = result.getData();
                    if (data != null) {
                        try {
                            Log.d("LoginActivity", "Processing Google Sign-In task...");
                            isProcessingGoogleSignIn = true;
                            btnGoogle.setEnabled(false);
                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                            handleGoogleSignInResult(task);
                        } catch (Exception e) {
                            Log.e("LoginActivity", "Exception processing task: ", e);
                            isProcessingGoogleSignIn = false;
                            btnGoogle.setEnabled(true);
                            Toast.makeText(this, "Sign-In error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("LoginActivity", "No intent data, result cancelled or failed");
                        isProcessingGoogleSignIn = false;
                        btnGoogle.setEnabled(true);
                    }
                }
        );
    }

    private void signInWithGoogle() {
        if (googleSignInClient == null) {
            Log.e("LoginActivity", "googleSignInClient is null");
            Toast.makeText(this, "Google Sign-In not configured. Please check local.properties", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d("LoginActivity", "signInWithGoogle called, signing out first to show account picker");
        btnGoogle.setEnabled(false);
        
        // Sign out first to force account picker
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            isProcessingGoogleSignIn = false;
            Toast.makeText(this, "Opening Google Sign-In...", Toast.LENGTH_SHORT).show();
            Intent signInIntent = googleSignInClient.getSignInIntent();
            Log.d("LoginActivity", "Launching Google Sign-In intent");
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            Log.d("LoginActivity", "handleGoogleSignInResult called");
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            
            Log.d("LoginActivity", "Google account retrieved: " + account.getEmail());
            
            // Get user info from Google
            String email = account.getEmail();
            String displayName = account.getDisplayName();
            String googleId = account.getId();
            
            if (email == null) {
                Log.e("LoginActivity", "Email is null from Google account");
                Toast.makeText(this, "Unable to get email from Google account", Toast.LENGTH_SHORT).show();
                isProcessingGoogleSignIn = false;
                btnGoogle.setEnabled(true);
                return;
            }

            Log.d("LoginActivity", "Checking if user exists: " + email);
            // Check if user already exists in database
            User existingUser = userRepository.getUserByEmail(email);
            
            if (existingUser != null) {
                Log.d("LoginActivity", "User found, logging in");
                // User exists - log them in
                sessionManager.createLoginSession(existingUser.getId(), false);
                
                // Check profile completion
                boolean hasProfile = existingUser.getBloodType() != null 
                        && !existingUser.getBloodType().isEmpty() 
                        && !"Unknown".equals(existingUser.getBloodType());
                
                Intent intent;
                if (hasProfile) {
                    intent = new Intent(LoginActivity.this, HomeActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, ProfileSetupActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Log.d("LoginActivity", "New user, creating account");
                // New user - create account
                createGoogleUser(email, displayName, googleId);
            }
            
        } catch (ApiException e) {
            Log.e("LoginActivity", "Google sign in failed with ApiException", e);
            isProcessingGoogleSignIn = false;
            btnGoogle.setEnabled(true);
            Toast.makeText(this, "Google Sign-In failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("LoginActivity", "Google sign in failed with exception", e);
            isProcessingGoogleSignIn = false;
            btnGoogle.setEnabled(true);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void createGoogleUser(String email, String displayName, String googleId) {
        try {
            Log.d("LoginActivity", "Creating new Google user: " + email);
            // Generate temporary password for Google users
            String tempPassword = "google_" + googleId;
            
            // Use display name or email prefix as name
            String userName = displayName;
            if (userName == null || userName.isEmpty()) {
                userName = email.split("@")[0];
            }
            
            Log.d("LoginActivity", "Registering user with name: " + userName);
            // Register new user with UserRepository API
            User newUser = userRepository.registerUser(email, tempPassword, userName, "Unknown");
            
            if (newUser != null) {
                Log.d("LoginActivity", "User registered successfully");
                // Get the created user
                User createdUser = userRepository.getUserByEmail(email);
                if (createdUser != null) {
                    Log.d("LoginActivity", "User found after creation, creating session");
                    sessionManager.createLoginSession(createdUser.getId(), false);
                    
                    // Navigate to profile setup since this is a new Google user
                    Intent intent = new Intent(LoginActivity.this, ProfileSetupActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Log.e("LoginActivity", "User not found after creation");
                    Toast.makeText(this, "Failed to retrieve created account", Toast.LENGTH_SHORT).show();
                    isProcessingGoogleSignIn = false;
                    btnGoogle.setEnabled(true);
                }
            } else {
                Log.e("LoginActivity", "Failed to register user");
                Toast.makeText(this, "Failed to create account with Google", Toast.LENGTH_SHORT).show();
                isProcessingGoogleSignIn = false;
                btnGoogle.setEnabled(true);
            }
        } catch (Exception e) {
            Log.e("LoginActivity", "Error creating Google user", e);
            isProcessingGoogleSignIn = false;
            btnGoogle.setEnabled(true);
            Toast.makeText(this, "Error creating account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}