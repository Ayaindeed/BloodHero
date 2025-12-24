package com.example.bloodhero;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bloodhero.models.User;
import com.example.bloodhero.repository.UserRepository;
import com.example.bloodhero.utils.EmailHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Random;

/**
 * Forgot Password Activity - Handles password reset via email verification
 * Supports email verification code and security question fallback
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";
    private static final String PREFS_NAME = "BloodHeroPrefs";
    private static final String RESET_PREFS = "BloodHeroPasswordReset";
    private static final long CODE_EXPIRATION_MS = 15 * 60 * 1000; // 15 minutes
    
    private UserRepository userRepository;
    private EmailHelper emailHelper;

    // Step 1: Email entry
    private LinearLayout layoutEmailStep;
    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private MaterialButton btnSendCode;
    
    // Step 2: Verification code
    private LinearLayout layoutCodeStep;
    private TextView tvCodeSentTo;
    private TextInputLayout tilCode;
    private TextInputEditText etCode;
    private MaterialButton btnVerifyCode;
    private TextView tvResendCode;
    private TextView tvCodeTimer;
    
    // Step 3: New password
    private LinearLayout layoutPasswordStep;
    private TextInputLayout tilNewPassword, tilConfirmPassword;
    private TextInputEditText etNewPassword, etConfirmPassword;
    private MaterialButton btnResetPassword;
    
    // Security question fallback
    private LinearLayout layoutSecurityQuestion;
    private TextView tvSecurityQuestion;
    private TextInputLayout tilSecurityAnswer;
    private TextInputEditText etSecurityAnswer;
    private MaterialButton btnVerifyAnswer;
    
    private ImageButton btnBack;
    private TextView tvUseSecurityQuestion, tvUseEmailCode;
    
    private String currentEmail;
    private String generatedCode;
    private long codeGeneratedTime;
    private int resendAttempts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        userRepository = UserRepository.getInstance(this);
        emailHelper = new EmailHelper(this);
        
        initViews();
        setupListeners();
        showStep(1);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (emailHelper != null) {
            emailHelper.shutdown();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        
        // Step 1
        layoutEmailStep = findViewById(R.id.layoutEmailStep);
        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);
        btnSendCode = findViewById(R.id.btnSendCode);
        
        // Step 2
        layoutCodeStep = findViewById(R.id.layoutCodeStep);
        tvCodeSentTo = findViewById(R.id.tvCodeSentTo);
        tilCode = findViewById(R.id.tilCode);
        etCode = findViewById(R.id.etCode);
        btnVerifyCode = findViewById(R.id.btnVerifyCode);
        tvResendCode = findViewById(R.id.tvResendCode);
        tvCodeTimer = findViewById(R.id.tvCodeTimer);
        
        // Step 3
        layoutPasswordStep = findViewById(R.id.layoutPasswordStep);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        
        // Security question fallback
        layoutSecurityQuestion = findViewById(R.id.layoutSecurityQuestion);
        tvSecurityQuestion = findViewById(R.id.tvSecurityQuestion);
        tilSecurityAnswer = findViewById(R.id.tilSecurityAnswer);
        etSecurityAnswer = findViewById(R.id.etSecurityAnswer);
        btnVerifyAnswer = findViewById(R.id.btnVerifyAnswer);
        
        tvUseSecurityQuestion = findViewById(R.id.tvUseSecurityQuestion);
        tvUseEmailCode = findViewById(R.id.tvUseEmailCode);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
        
        btnSendCode.setOnClickListener(v -> validateAndSendCode());
        
        btnVerifyCode.setOnClickListener(v -> verifyCode());
        
        tvResendCode.setOnClickListener(v -> resendCode());
        
        btnResetPassword.setOnClickListener(v -> resetPassword());
        
        btnVerifyAnswer.setOnClickListener(v -> verifySecurityAnswer());
        
        tvUseSecurityQuestion.setOnClickListener(v -> {
            if (currentEmail != null) {
                showSecurityQuestionStep();
            } else {
                Toast.makeText(this, "Please enter your email first", Toast.LENGTH_SHORT).show();
            }
        });
        
        tvUseEmailCode.setOnClickListener(v -> showStep(2));
    }

    private void showStep(int step) {
        layoutEmailStep.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        layoutCodeStep.setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        layoutPasswordStep.setVisibility(step == 3 ? View.VISIBLE : View.GONE);
        layoutSecurityQuestion.setVisibility(View.GONE);
    }

    private void showSecurityQuestionStep() {
        layoutEmailStep.setVisibility(View.GONE);
        layoutCodeStep.setVisibility(View.GONE);
        layoutPasswordStep.setVisibility(View.GONE);
        layoutSecurityQuestion.setVisibility(View.VISIBLE);
        
        // Load security question for user
        String question = getSecurityQuestion(currentEmail);
        tvSecurityQuestion.setText(question);
    }

    private void validateAndSendCode() {
        tilEmail.setError(null);
        
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            return;
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email");
            return;
        }
        
        // Check if user exists in SQLite
        User user = userRepository.getUserByEmail(email);
        if (user == null) {
            // Don't reveal if user exists for security
            // Still show "sent" message
        }
        
        currentEmail = email;
        sendVerificationCode(email);
    }

    private void sendVerificationCode(String email) {
        btnSendCode.setEnabled(false);
        btnSendCode.setText("Sending...");
        
        // Generate 6-digit code
        generatedCode = generateVerificationCode();
        codeGeneratedTime = System.currentTimeMillis();
        
        // Store the code temporarily (in production, this would be server-side)
        SharedPreferences resetPrefs = getSharedPreferences(RESET_PREFS, MODE_PRIVATE);
        resetPrefs.edit()
            .putString("reset_code", generatedCode)
            .putString("reset_email", email)
            .putLong("reset_time", codeGeneratedTime)
            .apply();
        
        // Get user info for email
        User user = userRepository.getUserByEmail(email);
        String userName = (user != null && !TextUtils.isEmpty(user.getName())) 
            ? user.getName() 
            : "User";
        
        // Show demo code immediately (for development/testing)
        String maskedEmail = maskEmail(email);
        
        // Proceed to next step after short delay (demo mode)
        btnSendCode.postDelayed(() -> {
            btnSendCode.setEnabled(true);
            btnSendCode.setText("Send Code");
            
            tvCodeSentTo.setText("Enter the 6-digit code sent to\n" + maskedEmail);
            showStep(2);
            
            // Show code in toast for demo (remove in production)
            Toast.makeText(this, "Demo Code: " + generatedCode, Toast.LENGTH_LONG).show();
            
            startCodeTimer();
        }, 1000);
        
        // Also try to send real email in background (won't block UI)
        emailHelper.sendVerificationCode(email, userName, generatedCode, new EmailHelper.EmailCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, "Email sent successfully: " + message);
                runOnUiThread(() -> {
                    Toast.makeText(ForgotPasswordActivity.this, "Email sent to " + maskedEmail, Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "Failed to send email: " + errorMessage);
                // Don't show error to user since demo mode is working
            }
        });
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return email;
        
        String name = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        
        if (name.length() <= 2) {
            return name + domain;
        }
        
        return name.charAt(0) + "***" + name.charAt(name.length() - 1) + domain;
    }

    private void startCodeTimer() {
        // 15 minute countdown
        final long expirationTime = codeGeneratedTime + CODE_EXPIRATION_MS;
        
        Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                long remaining = expirationTime - System.currentTimeMillis();
                
                if (remaining <= 0) {
                    tvCodeTimer.setText("Code expired");
                    tvCodeTimer.setTextColor(getColor(R.color.error));
                    generatedCode = null;
                } else {
                    int minutes = (int) (remaining / 60000);
                    int seconds = (int) ((remaining % 60000) / 1000);
                    tvCodeTimer.setText(String.format("Code expires in %d:%02d", minutes, seconds));
                    tvCodeTimer.postDelayed(this, 1000);
                }
            }
        };
        tvCodeTimer.post(timerRunnable);
    }

    private void verifyCode() {
        tilCode.setError(null);
        
        String enteredCode = etCode.getText() != null ? etCode.getText().toString().trim() : "";
        
        if (TextUtils.isEmpty(enteredCode)) {
            tilCode.setError("Please enter the verification code");
            return;
        }
        
        if (enteredCode.length() != 6) {
            tilCode.setError("Code must be 6 digits");
            return;
        }
        
        // Check if code expired
        if (System.currentTimeMillis() - codeGeneratedTime > CODE_EXPIRATION_MS) {
            tilCode.setError("Code has expired. Please request a new one.");
            return;
        }
        
        // Verify code
        if (enteredCode.equals(generatedCode)) {
            showStep(3);
        } else {
            tilCode.setError("Invalid code. Please try again.");
        }
    }

    private void resendCode() {
        resendAttempts++;
        
        if (resendAttempts > 3) {
            Toast.makeText(this, "Too many attempts. Please try again later.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        sendVerificationCode(currentEmail);
    }

    private void resetPassword() {
        tilNewPassword.setError(null);
        tilConfirmPassword.setError(null);
        
        String newPassword = etNewPassword.getText() != null ? etNewPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";
        
        boolean isValid = true;
        
        if (TextUtils.isEmpty(newPassword)) {
            tilNewPassword.setError("Password is required");
            isValid = false;
        } else if (newPassword.length() < 6) {
            tilNewPassword.setError("Password must be at least 6 characters");
            isValid = false;
        } else if (!isStrongPassword(newPassword)) {
            tilNewPassword.setError("Password must contain letters and numbers");
            isValid = false;
        }
        
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Please confirm your password");
            isValid = false;
        } else if (!newPassword.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }
        
        if (isValid) {
            performPasswordReset(newPassword);
        }
    }

    private boolean isStrongPassword(String password) {
        boolean hasLetter = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        
        return hasLetter && hasDigit;
    }

    private void performPasswordReset(String newPassword) {
        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("Resetting...");
        
        btnResetPassword.postDelayed(() -> {
            // Save new password in SQLite
            User user = userRepository.getUserByEmail(currentEmail);
            if (user != null) {
                userRepository.updatePassword(user.getId(), newPassword);
            }
            
            // Clear reset data
            getSharedPreferences(RESET_PREFS, MODE_PRIVATE).edit().clear().apply();
            
            Toast.makeText(this, "Password reset successfully!", Toast.LENGTH_SHORT).show();
            
            // Return to login
            finish();
        }, 1500);
    }

    private String getSecurityQuestion(String email) {
        // Get user's security question from database
        User user = userRepository.getUserByEmail(email);
        if (user != null && !TextUtils.isEmpty(user.getSecurityQuestion())) {
            return user.getSecurityQuestion();
        }
        // Default fallback
        return "What is your mother's maiden name?";
    }

    private void verifySecurityAnswer() {
        tilSecurityAnswer.setError(null);
        
        String answer = etSecurityAnswer.getText() != null ? etSecurityAnswer.getText().toString().trim().toLowerCase() : "";
        
        if (TextUtils.isEmpty(answer)) {
            tilSecurityAnswer.setError("Please enter your answer");
            return;
        }
        
        // Get stored answer from database
        User user = userRepository.getUserByEmail(currentEmail);
        if (user == null || TextUtils.isEmpty(user.getSecurityAnswer())) {
            tilSecurityAnswer.setError("No security answer found for this account");
            return;
        }
        
        String storedAnswer = user.getSecurityAnswer().toLowerCase();
        
        if (answer.equals(storedAnswer)) {
            showStep(3);
        } else {
            tilSecurityAnswer.setError("Incorrect answer. Please try again.");
        }
    }
}
