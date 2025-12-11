package com.example.bloodhero;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 2500;
    private static final String PREFS_NAME = "BloodHeroPrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_PROFILE_COMPLETE = "profile_complete";

    private ImageView ivLogo;
    private TextView tvAppName;
    private TextView tvTagline;
    private LinearLayout loadingContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Handle the splash screen transition
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Make status bar transparent
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        initViews();
        startAnimations();
        navigateAfterDelay();
    }

    private void initViews() {
        ivLogo = findViewById(R.id.ivLogo);
        tvAppName = findViewById(R.id.tvAppName);
        tvTagline = findViewById(R.id.tvTagline);
        loadingContainer = findViewById(R.id.loadingContainer);

        // Initially hide elements for animation
        ivLogo.setAlpha(0f);
        tvAppName.setAlpha(0f);
        tvTagline.setAlpha(0f);
        loadingContainer.setAlpha(0f);
    }

    private void startAnimations() {
        // Logo animation - scale and fade in
        ivLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Pulse animation for logo
        ivLogo.postDelayed(() -> {
            ScaleAnimation pulse = new ScaleAnimation(
                    1f, 1.1f, 1f, 1.1f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            pulse.setDuration(600);
            pulse.setRepeatMode(Animation.REVERSE);
            pulse.setRepeatCount(Animation.INFINITE);
            ivLogo.startAnimation(pulse);
        }, 800);

        // App name animation - fade in with slide up
        tvAppName.postDelayed(() -> {
            tvAppName.animate()
                    .alpha(1f)
                    .translationY(0)
                    .setDuration(500)
                    .start();
        }, 400);

        // Tagline animation
        tvTagline.postDelayed(() -> {
            tvTagline.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .start();
        }, 600);

        // Loading container animation
        loadingContainer.postDelayed(() -> {
            loadingContainer.animate()
                    .alpha(1f)
                    .setDuration(400)
                    .start();
        }, 1000);
    }

    private void navigateAfterDelay() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
            boolean profileComplete = prefs.getBoolean(KEY_PROFILE_COMPLETE, false);

            Intent intent;
            if (isLoggedIn) {
                if (profileComplete) {
                    intent = new Intent(SplashActivity.this, HomeActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, ProfileSetupActivity.class);
                }
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_DELAY);
    }
}