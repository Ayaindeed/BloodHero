package com.example.bloodhero.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bloodhero.LoginActivity;
import com.example.bloodhero.ProfileSetupActivity;
import com.example.bloodhero.R;
import com.example.bloodhero.models.User;
import com.example.bloodhero.repository.UserRepository;
import com.example.bloodhero.utils.SessionManager;
import com.example.bloodhero.utils.EnhancedDialogHelper;
import com.example.bloodhero.utils.UserHelper;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private UserRepository userRepository;
    private SessionManager sessionManager;
    private User currentUser;

    private ImageButton btnBack, btnSettings;
    private CircleImageView ivProfilePhoto;
    private TextView tvUserName, tvUserEmail, tvBloodType;
    private TextView tvDonations, tvPoints, tvBadges;
    private LinearLayout menuEditProfile, menuAppointments, menuNotifications, menuLocation, menuHelp, menuAdmin;
    private View cardAdmin;
    private LinearLayout btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userRepository = UserRepository.getInstance(this);
        sessionManager = SessionManager.getInstance(this);
        currentUser = UserHelper.getCurrentUser(this);

        initViews();
        loadUserData();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSettings = findViewById(R.id.btnSettings);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvBloodType = findViewById(R.id.tvBloodType);
        tvDonations = findViewById(R.id.tvDonations);
        tvPoints = findViewById(R.id.tvPoints);
        tvBadges = findViewById(R.id.tvBadges);
        menuEditProfile = findViewById(R.id.menuEditProfile);
        menuAppointments = findViewById(R.id.menuAppointments);
        menuNotifications = findViewById(R.id.menuNotifications);
        menuLocation = findViewById(R.id.menuLocation);
        menuHelp = findViewById(R.id.menuHelp);
        menuAdmin = findViewById(R.id.menuAdmin);
        cardAdmin = findViewById(R.id.cardAdmin);
        btnLogout = findViewById(R.id.btnLogout);
        
        // Only show admin panel for admin user
        if (sessionManager.isAdmin()) {
            cardAdmin.setVisibility(View.VISIBLE);
        } else {
            cardAdmin.setVisibility(View.GONE);
        }
    }

    private void loadUserData() {
        if (currentUser == null) {
            currentUser = UserHelper.getCurrentUser(this);
        }
        
        if (currentUser != null) {
            String name = currentUser.getName() != null ? currentUser.getName() : "User";
            String email = currentUser.getEmail() != null ? currentUser.getEmail() : "";
            String bloodType = currentUser.getBloodType() != null ? currentUser.getBloodType() : "Unknown";
            int donations = currentUser.getTotalDonations();
            int points = currentUser.getTotalPoints();
            int badges = calculateUnlockedBadges(donations);

            tvUserName.setText(name);
            tvUserEmail.setText(email);
            tvBloodType.setText(bloodType);
            tvDonations.setText(String.valueOf(donations));
            tvPoints.setText(String.format("%,d", points));
            tvBadges.setText(String.valueOf(badges));
            
            // Load profile image
            loadProfileImage();
        }
    }
    
    private void loadProfileImage() {
        if (currentUser != null && currentUser.getProfileImageUrl() != null) {
            String photoPath = currentUser.getProfileImageUrl();
            File photoFile = new File(photoPath);
            if (photoFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
                if (bitmap != null) {
                    ivProfilePhoto.setImageBitmap(bitmap);
                }
            }
        }
    }

    private int calculateUnlockedBadges(int donations) {
        int badges = 0;
        
        // Profile complete badge
        if (currentUser != null && currentUser.getBloodType() != null && !currentUser.getBloodType().equals("Unknown")) {
            badges++;
        }
        
        // Donation-based badges
        if (donations >= 1) badges++;
        if (donations >= 5) badges++;
        if (donations >= 10) badges++;
        if (donations >= 25) badges++;
        if (donations >= 50) badges++;
        if (donations >= 100) badges++;
        
        return badges;
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        menuEditProfile.setOnClickListener(v -> {
            // Navigate to profile setup for editing
            Intent intent = new Intent(this, ProfileSetupActivity.class);
            intent.putExtra("is_editing", true);
            startActivity(intent);
        });

        menuAppointments.setOnClickListener(v -> {
            startActivity(new Intent(this, MyAppointmentsActivity.class));
        });

        menuNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationSettingsActivity.class));
        });

        menuLocation.setOnClickListener(v -> {
            startActivity(new Intent(this, CampaignsActivity.class));
        });

        menuHelp.setOnClickListener(v -> {
            startActivity(new Intent(this, HelpSupportActivity.class));
        });

        menuAdmin.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminDashboardActivity.class));
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        EnhancedDialogHelper.showCriticalActionDialog(
                this,
                "Logout",
                "Are you sure you want to logout?",
                "Logout",
                this::performLogout
        );
    }

    private void performLogout() {
        // Use SessionManager to logout
        sessionManager.logout();

        // Navigate to login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload user from database in case profile was edited
        currentUser = UserHelper.getCurrentUser(this);
        loadUserData();
    }
}