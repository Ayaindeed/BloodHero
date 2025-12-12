package com.example.bloodhero.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.bloodhero.R;
import com.example.bloodhero.views.BloodCompatibilityView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BloodCompatibilityActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BloodHeroPrefs";

    // Blood type full names
    private static final Map<String, String> BLOOD_TYPE_NAMES = new HashMap<>();
    private static final Map<String, String> BLOOD_TYPE_DESCRIPTIONS = new HashMap<>();
    
    static {
        BLOOD_TYPE_NAMES.put("O-", "O Negative");
        BLOOD_TYPE_NAMES.put("O+", "O Positive");
        BLOOD_TYPE_NAMES.put("A-", "A Negative");
        BLOOD_TYPE_NAMES.put("A+", "A Positive");
        BLOOD_TYPE_NAMES.put("B-", "B Negative");
        BLOOD_TYPE_NAMES.put("B+", "B Positive");
        BLOOD_TYPE_NAMES.put("AB-", "AB Negative");
        BLOOD_TYPE_NAMES.put("AB+", "AB Positive");
        
        BLOOD_TYPE_DESCRIPTIONS.put("O-", "Universal Donor - Can donate to everyone!");
        BLOOD_TYPE_DESCRIPTIONS.put("O+", "Most common blood type (37% of population)");
        BLOOD_TYPE_DESCRIPTIONS.put("A-", "Rare type - Only 6% of population");
        BLOOD_TYPE_DESCRIPTIONS.put("A+", "Second most common (34% of population)");
        BLOOD_TYPE_DESCRIPTIONS.put("B-", "Very rare - Only 2% of population");
        BLOOD_TYPE_DESCRIPTIONS.put("B+", "Common type (9% of population)");
        BLOOD_TYPE_DESCRIPTIONS.put("AB-", "Rarest type - Less than 1% of population");
        BLOOD_TYPE_DESCRIPTIONS.put("AB+", "Universal Recipient - Can receive from anyone!");
    }

    // Blood compatibility chart
    private static final Map<String, List<String>> CAN_DONATE_TO = new HashMap<>();
    private static final Map<String, List<String>> CAN_RECEIVE_FROM = new HashMap<>();

    static {
        // Who can each blood type DONATE to?
        CAN_DONATE_TO.put("O-", Arrays.asList("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+"));
        CAN_DONATE_TO.put("O+", Arrays.asList("O+", "A+", "B+", "AB+"));
        CAN_DONATE_TO.put("A-", Arrays.asList("A-", "A+", "AB-", "AB+"));
        CAN_DONATE_TO.put("A+", Arrays.asList("A+", "AB+"));
        CAN_DONATE_TO.put("B-", Arrays.asList("B-", "B+", "AB-", "AB+"));
        CAN_DONATE_TO.put("B+", Arrays.asList("B+", "AB+"));
        CAN_DONATE_TO.put("AB-", Arrays.asList("AB-", "AB+"));
        CAN_DONATE_TO.put("AB+", Arrays.asList("AB+"));

        // Who can each blood type RECEIVE from?
        CAN_RECEIVE_FROM.put("O-", Arrays.asList("O-"));
        CAN_RECEIVE_FROM.put("O+", Arrays.asList("O-", "O+"));
        CAN_RECEIVE_FROM.put("A-", Arrays.asList("O-", "A-"));
        CAN_RECEIVE_FROM.put("A+", Arrays.asList("O-", "O+", "A-", "A+"));
        CAN_RECEIVE_FROM.put("B-", Arrays.asList("O-", "B-"));
        CAN_RECEIVE_FROM.put("B+", Arrays.asList("O-", "O+", "B-", "B+"));
        CAN_RECEIVE_FROM.put("AB-", Arrays.asList("O-", "A-", "B-", "AB-"));
        CAN_RECEIVE_FROM.put("AB+", Arrays.asList("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+"));
    }

    private ImageButton btnBack;
    private TextView tvBloodBagType;
    private TextView tvUserBloodTypeDisplay, tvBloodTypeName, tvBloodTypeDesc;
    private TextView tvCanDonateTo, tvCanReceiveFrom;
    private TextView tvDonateToList, tvReceiveFromList;
    private CardView cardSpecialInfo;
    private TextView tvSpecialTitle, tvSpecialDesc;
    private BloodCompatibilityView compatibilityView;
    private LinearLayout bloodBagContainer;

    // Blood type icon views
    private View bloodTypeApos, bloodTypeAneg, bloodTypeBpos, bloodTypeBneg;
    private View bloodTypeOpos, bloodTypeOneg, bloodTypeABpos, bloodTypeABneg;

    private String userBloodType;
    private Handler animationHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_compatibility);

        loadUserBloodType();
        initViews();
        setupClickListeners();
        setupBloodTypeIcons();
        setupCompatibilityDisplay();
        setupConnectionLines();
    }

    private void loadUserBloodType() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userBloodType = prefs.getString("blood_type", "O+");
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvBloodBagType = findViewById(R.id.tvBloodBagType);
        tvUserBloodTypeDisplay = findViewById(R.id.tvUserBloodTypeDisplay);
        tvBloodTypeName = findViewById(R.id.tvBloodTypeName);
        tvBloodTypeDesc = findViewById(R.id.tvBloodTypeDesc);
        tvCanDonateTo = findViewById(R.id.tvCanDonateTo);
        tvCanReceiveFrom = findViewById(R.id.tvCanReceiveFrom);
        tvDonateToList = findViewById(R.id.tvDonateToList);
        tvReceiveFromList = findViewById(R.id.tvReceiveFromList);
        cardSpecialInfo = findViewById(R.id.cardSpecialInfo);
        tvSpecialTitle = findViewById(R.id.tvSpecialTitle);
        tvSpecialDesc = findViewById(R.id.tvSpecialDesc);
        compatibilityView = findViewById(R.id.compatibilityView);
        bloodBagContainer = findViewById(R.id.bloodBagContainer);

        // Blood type icons
        bloodTypeApos = findViewById(R.id.bloodTypeApos);
        bloodTypeAneg = findViewById(R.id.bloodTypeAneg);
        bloodTypeBpos = findViewById(R.id.bloodTypeBpos);
        bloodTypeBneg = findViewById(R.id.bloodTypeBneg);
        bloodTypeOpos = findViewById(R.id.bloodTypeOpos);
        bloodTypeOneg = findViewById(R.id.bloodTypeOneg);
        bloodTypeABpos = findViewById(R.id.bloodTypeABpos);
        bloodTypeABneg = findViewById(R.id.bloodTypeABneg);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupBloodTypeIcons() {
        // Set blood type labels
        setBloodTypeLabel(bloodTypeApos, "A+");
        setBloodTypeLabel(bloodTypeAneg, "A-");
        setBloodTypeLabel(bloodTypeBpos, "B+");
        setBloodTypeLabel(bloodTypeBneg, "B-");
        setBloodTypeLabel(bloodTypeOpos, "O+");
        setBloodTypeLabel(bloodTypeOneg, "O-");
        setBloodTypeLabel(bloodTypeABpos, "AB+");
        setBloodTypeLabel(bloodTypeABneg, "AB-");
    }

    private void setBloodTypeLabel(View container, String bloodType) {
        if (container == null) return;
        TextView tvLabel = container.findViewById(R.id.tvBloodTypeLabel);
        ImageView ivIcon = container.findViewById(R.id.ivPersonIcon);
        
        if (tvLabel != null) {
            tvLabel.setText(bloodType);
        }

        // Highlight if user can donate to this type
        List<String> canDonateTo = CAN_DONATE_TO.getOrDefault(userBloodType, new ArrayList<>());
        boolean canDonate = canDonateTo.contains(bloodType);

        if (ivIcon != null) {
            if (canDonate) {
                ivIcon.setColorFilter(Color.parseColor("#4CAF50")); // Green for compatible
            } else {
                ivIcon.setColorFilter(Color.parseColor("#666666")); // Gray for incompatible
            }
        }
        if (tvLabel != null) {
            if (canDonate) {
                tvLabel.setTextColor(Color.parseColor("#4CAF50"));
            } else {
                tvLabel.setTextColor(Color.parseColor("#666666"));
            }
        }
    }

    private void setupCompatibilityDisplay() {
        // Set blood bag type
        tvBloodBagType.setText(userBloodType);
        tvUserBloodTypeDisplay.setText(userBloodType);
        tvBloodTypeName.setText(BLOOD_TYPE_NAMES.getOrDefault(userBloodType, userBloodType));
        tvBloodTypeDesc.setText(BLOOD_TYPE_DESCRIPTIONS.getOrDefault(userBloodType, ""));

        // Get compatibility lists
        List<String> canDonateTo = CAN_DONATE_TO.getOrDefault(userBloodType, new ArrayList<>());
        List<String> canReceiveFrom = CAN_RECEIVE_FROM.getOrDefault(userBloodType, new ArrayList<>());

        // Update stats
        tvCanDonateTo.setText(String.valueOf(canDonateTo.size()));
        tvCanReceiveFrom.setText(String.valueOf(canReceiveFrom.size()));

        // Update lists
        tvDonateToList.setText(String.join(", ", canDonateTo));
        tvReceiveFromList.setText(String.join(", ", canReceiveFrom));

        // Show special info for universal donor/recipient
        if (userBloodType.equals("O-")) {
            cardSpecialInfo.setVisibility(View.VISIBLE);
            tvSpecialTitle.setText("🌟 Universal Donor");
            tvSpecialDesc.setText("Your blood type can be given to anyone! You are incredibly valuable to blood banks.");
        } else if (userBloodType.equals("AB+")) {
            cardSpecialInfo.setVisibility(View.VISIBLE);
            tvSpecialTitle.setText("🌟 Universal Recipient");
            tvSpecialDesc.setText("You can receive blood from anyone! This is very helpful in emergencies.");
        } else {
            cardSpecialInfo.setVisibility(View.GONE);
        }
    }

    private void setupConnectionLines() {
        // Wait for layout to be complete before calculating positions
        compatibilityView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        compatibilityView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        drawConnectionLines();
                    }
                });
    }

    private void drawConnectionLines() {
        List<BloodCompatibilityView.ConnectionLine> lines = new ArrayList<>();
        List<String> canDonateTo = CAN_DONATE_TO.getOrDefault(userBloodType, new ArrayList<>());

        // Get blood bag center position
        int[] bagLocation = new int[2];
        bloodBagContainer.getLocationInWindow(bagLocation);
        int[] viewLocation = new int[2];
        compatibilityView.getLocationInWindow(viewLocation);

        float bagCenterX = bagLocation[0] - viewLocation[0] + bloodBagContainer.getWidth() / 2f;
        float bagBottomY = bagLocation[1] - viewLocation[1] + bloodBagContainer.getHeight();

        // Create lines to each compatible blood type
        addConnectionLine(lines, bloodTypeApos, "A+", canDonateTo, bagCenterX, bagBottomY, viewLocation);
        addConnectionLine(lines, bloodTypeAneg, "A-", canDonateTo, bagCenterX, bagBottomY, viewLocation);
        addConnectionLine(lines, bloodTypeBpos, "B+", canDonateTo, bagCenterX, bagBottomY, viewLocation);
        addConnectionLine(lines, bloodTypeBneg, "B-", canDonateTo, bagCenterX, bagBottomY, viewLocation);
        addConnectionLine(lines, bloodTypeOpos, "O+", canDonateTo, bagCenterX, bagBottomY, viewLocation);
        addConnectionLine(lines, bloodTypeOneg, "O-", canDonateTo, bagCenterX, bagBottomY, viewLocation);
        addConnectionLine(lines, bloodTypeABpos, "AB+", canDonateTo, bagCenterX, bagBottomY, viewLocation);
        addConnectionLine(lines, bloodTypeABneg, "AB-", canDonateTo, bagCenterX, bagBottomY, viewLocation);

        compatibilityView.setConnectionLines(lines);

        // Animate after a short delay
        animationHandler.postDelayed(() -> compatibilityView.animateLines(), 500);
    }

    private void addConnectionLine(List<BloodCompatibilityView.ConnectionLine> lines,
                                   View targetView, String bloodType, List<String> canDonateTo,
                                   float startX, float startY, int[] viewLocation) {
        if (targetView == null) return;

        int[] targetLocation = new int[2];
        targetView.getLocationInWindow(targetLocation);

        float endX = targetLocation[0] - viewLocation[0] + targetView.getWidth() / 2f;
        float endY = targetLocation[1] - viewLocation[1];

        boolean isActive = canDonateTo.contains(bloodType);
        int lineColor = isActive ? Color.parseColor("#4CAF50") : Color.parseColor("#333333");
        int glowColor = isActive ? Color.parseColor("#404CAF50") : Color.parseColor("#20333333");

        lines.add(new BloodCompatibilityView.ConnectionLine(
                startX, startY, endX, endY, lineColor, glowColor, isActive, bloodType
        ));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        animationHandler.removeCallbacksAndMessages(null);
    }
}
