package com.example.bloodhero.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodhero.R;
import com.example.bloodhero.adapters.BloodRequestAdapter;
import com.example.bloodhero.models.BloodRequest;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class BloodRequestsActivity extends AppCompatActivity implements BloodRequestAdapter.OnRequestClickListener {

    private static final String PREFS_NAME = "BloodHeroPrefs";

    private ImageButton btnBack;
    private TextView tvUserBloodType, tvCompatibilityInfo;
    private Chip chipShowAll;
    private RecyclerView rvRequests;
    private LinearLayout emptyState;

    private BloodRequestAdapter requestAdapter;
    private List<BloodRequest> allRequests;
    private List<BloodRequest> filteredRequests;
    private String userBloodType;
    private boolean showAllRequests = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_requests);

        initViews();
        loadUserBloodType();
        loadRequests();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvUserBloodType = findViewById(R.id.tvUserBloodType);
        tvCompatibilityInfo = findViewById(R.id.tvCompatibilityInfo);
        chipShowAll = findViewById(R.id.chipShowAll);
        rvRequests = findViewById(R.id.rvRequests);
        emptyState = findViewById(R.id.emptyState);
    }

    private void loadUserBloodType() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userBloodType = prefs.getString("blood_type", "O+");
        tvUserBloodType.setText(userBloodType);
    }

    private void loadRequests() {
        allRequests = new ArrayList<>();

        // Simulated urgent blood requests for Morocco
        allRequests.add(new BloodRequest(
                "1", "Ahmed B.", "O-", "CHU Ibn Sina",
                "Rabat", "critical", 3, "+212 537 67 10 10", "30 min ago"
        ));

        allRequests.add(new BloodRequest(
                "2", "Fatima Z.", "A+", "CHU Ibn Rochd",
                "Casablanca", "urgent", 2, "+212 522 48 20 20", "1h ago"
        ));

        allRequests.add(new BloodRequest(
                "3", "Mohammed K.", "B-", "Hopital Avicenne",
                "Marrakech", "critical", 4, "+212 524 30 40 50", "2h ago"
        ));

        allRequests.add(new BloodRequest(
                "4", "Aicha M.", "AB+", "CHU Hassan II",
                "Fes", "normal", 1, "+212 535 61 91 91", "3h ago"
        ));

        allRequests.add(new BloodRequest(
                "5", "Youssef L.", "O+", "Clinique Atlas",
                "Tangier", "urgent", 2, "+212 539 94 05 05", "4h ago"
        ));

        allRequests.add(new BloodRequest(
                "6", "Khadija R.", "A-", "Hopital Mohammed V",
                "Meknes", "normal", 1, "+212 535 52 00 00", "5h ago"
        ));

        allRequests.add(new BloodRequest(
                "7", "Omar S.", "B+", "CHU Mohammed VI",
                "Oujda", "critical", 5, "+212 536 68 48 48", "6h ago"
        ));

        filterRequests();
    }

    private void filterRequests() {
        filteredRequests = new ArrayList<>();

        if (showAllRequests) {
            filteredRequests.addAll(allRequests);
            tvCompatibilityInfo.setText("Showing all requests");
        } else {
            for (BloodRequest request : allRequests) {
                if (canDonate(userBloodType, request.getBloodType())) {
                    filteredRequests.add(request);
                }
            }
            tvCompatibilityInfo.setText("Showing compatible requests (" + filteredRequests.size() + ")");
        }

        if (filteredRequests.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvRequests.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvRequests.setVisibility(View.VISIBLE);
        }

        requestAdapter = new BloodRequestAdapter(filteredRequests, userBloodType, this);
        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        rvRequests.setAdapter(requestAdapter);
    }

    private boolean canDonate(String donorType, String recipientType) {
        if (donorType == null || donorType.isEmpty()) return false;
        
        // O- can donate to anyone
        if (donorType.equals("O-")) return true;
        
        // O+ can donate to all positive
        if (donorType.equals("O+")) {
            return recipientType.endsWith("+");
        }
        
        // A- can donate to A+, A-, AB+, AB-
        if (donorType.equals("A-")) {
            return recipientType.startsWith("A") || recipientType.startsWith("AB");
        }
        
        // A+ can donate to A+, AB+
        if (donorType.equals("A+")) {
            return recipientType.equals("A+") || recipientType.equals("AB+");
        }
        
        // B- can donate to B+, B-, AB+, AB-
        if (donorType.equals("B-")) {
            return recipientType.startsWith("B") || recipientType.startsWith("AB");
        }
        
        // B+ can donate to B+, AB+
        if (donorType.equals("B+")) {
            return recipientType.equals("B+") || recipientType.equals("AB+");
        }
        
        // AB- can donate to AB+, AB-
        if (donorType.equals("AB-")) {
            return recipientType.startsWith("AB");
        }
        
        // AB+ can only donate to AB+
        if (donorType.equals("AB+")) {
            return recipientType.equals("AB+");
        }
        
        return false;
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        chipShowAll.setOnClickListener(v -> {
            showAllRequests = !showAllRequests;
            chipShowAll.setText(showAllRequests ? "Show Compatible" : "Show All");
            filterRequests();
        });
    }

    @Override
    public void onRespondClick(BloodRequest request) {
        new AlertDialog.Builder(this)
                .setTitle("Respond to Request")
                .setMessage("You're volunteering to donate " + request.getBloodType() + 
                        " blood at " + request.getHospital() + ", " + request.getCity() + 
                        ".\n\nPatient: " + request.getPatientName() + 
                        "\nUnits needed: " + request.getUnitsNeeded() +
                        "\n\nDo you want to proceed?")
                .setPositiveButton("Yes, I'll Help", (dialog, which) -> {
                    // Save response
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    prefs.edit()
                            .putBoolean("responded_to_request", true)
                            .putString("responded_request_id", request.getId())
                            .apply();
                    
                    Toast.makeText(this, "Thank you! The hospital will be notified.", Toast.LENGTH_LONG).show();
                    
                    // Show contact info
                    new AlertDialog.Builder(this)
                            .setTitle("Next Steps")
                            .setMessage("Please contact the hospital to confirm your visit:\n\n" +
                                    "Hospital: " + request.getHospital() + "\n" +
                                    "City: " + request.getCity() + "\n" +
                                    "Phone: " + request.getContactPhone() + "\n\n" +
                                    "Bring your ID and arrive within 24 hours if possible.")
                            .setPositiveButton("Call Now", (d, w) -> {
                                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                                callIntent.setData(Uri.parse("tel:" + request.getContactPhone()));
                                startActivity(callIntent);
                            })
                            .setNegativeButton("Later", null)
                            .show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onCallClick(BloodRequest request) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + request.getContactPhone()));
        startActivity(callIntent);
    }
}
