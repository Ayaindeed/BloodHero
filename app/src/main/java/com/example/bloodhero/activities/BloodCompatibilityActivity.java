package com.example.bloodhero.activities;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.bloodhero.R;
import com.example.bloodhero.models.User;
import com.example.bloodhero.utils.UserHelper;
import com.example.bloodhero.views.FlowingBloodCompatibilityView;
import com.google.android.material.button.MaterialButton;

public class BloodCompatibilityActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private FlowingBloodCompatibilityView flowingCompatibilityView;

    // Blood type buttons
    private MaterialButton btnBloodAPos, btnBloodANeg, btnBloodBPos, btnBloodBNeg;
    private MaterialButton btnBloodABPos, btnBloodABNeg, btnBloodOPos, btnBloodONeg;

    private String currentBloodType;
    private MaterialButton selectedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_compatibility_flowing);

        loadUserBloodType();
        initViews();
        setupClickListeners();
        updateCompatibilityDisplay(currentBloodType);
    }

    private void loadUserBloodType() {
        User user = UserHelper.getCurrentUser(this);
        if (user != null && user.getBloodType() != null && !user.getBloodType().isEmpty()) {
            currentBloodType = user.getBloodType();
        } else {
            currentBloodType = "O+";
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        flowingCompatibilityView = findViewById(R.id.flowingCompatibilityView);

        // Blood type buttons
        btnBloodAPos = findViewById(R.id.btnBloodAPos);
        btnBloodANeg = findViewById(R.id.btnBloodANeg);
        btnBloodBPos = findViewById(R.id.btnBloodBPos);
        btnBloodBNeg = findViewById(R.id.btnBloodBNeg);
        btnBloodABPos = findViewById(R.id.btnBloodABPos);
        btnBloodABNeg = findViewById(R.id.btnBloodABNeg);
        btnBloodOPos = findViewById(R.id.btnBloodOPos);
        btnBloodONeg = findViewById(R.id.btnBloodONeg);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnBloodAPos.setOnClickListener(v -> selectBloodType("A+", btnBloodAPos));
        btnBloodANeg.setOnClickListener(v -> selectBloodType("A-", btnBloodANeg));
        btnBloodBPos.setOnClickListener(v -> selectBloodType("B+", btnBloodBPos));
        btnBloodBNeg.setOnClickListener(v -> selectBloodType("B-", btnBloodBNeg));
        btnBloodABPos.setOnClickListener(v -> selectBloodType("AB+", btnBloodABPos));
        btnBloodABNeg.setOnClickListener(v -> selectBloodType("AB-", btnBloodABNeg));
        btnBloodOPos.setOnClickListener(v -> selectBloodType("O+", btnBloodOPos));
        btnBloodONeg.setOnClickListener(v -> selectBloodType("O-", btnBloodONeg));
    }

    private void selectBloodType(String bloodType, MaterialButton button) {
        currentBloodType = bloodType;
        
        // Reset previous selection
        if (selectedButton != null) {
            resetButtonStyle(selectedButton);
        }
        
        // Highlight new selection
        selectedButton = button;
        highlightSelectedButton(button);
        
        // Update flowing view
        updateCompatibilityDisplay(bloodType);
    }

    private void updateCompatibilityDisplay(String bloodType) {
        currentBloodType = bloodType;
        
        // Update the flowing compatibility view
        flowingCompatibilityView.setBloodType(bloodType);
        
        // Highlight the button for user's blood type on first load
        if (selectedButton == null) {
            MaterialButton button = getButtonForBloodType(bloodType);
            if (button != null) {
                selectedButton = button;
                highlightSelectedButton(button);
            }
        }
    }

    private MaterialButton getButtonForBloodType(String bloodType) {
        switch (bloodType) {
            case "A+": return btnBloodAPos;
            case "A-": return btnBloodANeg;
            case "B+": return btnBloodBPos;
            case "B-": return btnBloodBNeg;
            case "AB+": return btnBloodABPos;
            case "AB-": return btnBloodABNeg;
            case "O+": return btnBloodOPos;
            case "O-": return btnBloodONeg;
            default: return null;
        }
    }

    private void resetButtonStyle(MaterialButton button) {
        if (button == null) return;
        button.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.surface)));
        button.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        button.setStrokeColor(ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.divider)));
    }

    private void highlightSelectedButton(MaterialButton button) {
        if (button == null) return;
        button.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.primary)));
        button.setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }
}
