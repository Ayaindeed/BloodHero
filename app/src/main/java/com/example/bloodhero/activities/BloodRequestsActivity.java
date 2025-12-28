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
        // Create enhanced confirmation dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_blood_request_response, null);
        AlertDialog confirmDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        
        if (confirmDialog.getWindow() != null) {
            confirmDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        // Setup dialog views
        TextView tvBloodType = dialogView.findViewById(R.id.tvBloodType);
        TextView tvPatientName = dialogView.findViewById(R.id.tvPatientName);
        TextView tvHospital = dialogView.findViewById(R.id.tvHospital);
        TextView tvCity = dialogView.findViewById(R.id.tvCity);
        TextView tvUnits = dialogView.findViewById(R.id.tvUnits);
        com.google.android.material.button.MaterialButton btnConfirm = dialogView.findViewById(R.id.btnConfirm);
        com.google.android.material.button.MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        
        tvBloodType.setText(request.getBloodType());
        tvPatientName.setText(request.getPatientName());
        tvHospital.setText(request.getHospital());
        tvCity.setText(request.getCity());
        tvUnits.setText(request.getUnitsNeeded() + " units needed");
        
        btnConfirm.setOnClickListener(v -> {
            confirmDialog.dismiss();
            
            // Save response
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit()
                    .putBoolean("responded_to_request", true)
                    .putString("responded_request_id", request.getId())
                    .apply();
            
            // Create appointment for this urgent request
            createUrgentAppointment(request);
            
            Toast.makeText(this, "Thank you! The hospital will be notified.", Toast.LENGTH_LONG).show();
            
            // Show enhanced next steps dialog
            showNextStepsDialog(request);
        });
        
        btnCancel.setOnClickListener(v -> confirmDialog.dismiss());
        
        confirmDialog.show();
    }
    
    private void createUrgentAppointment(BloodRequest request) {
        // Import required classes at the top if not already imported
        com.example.bloodhero.repository.AppointmentRepository appointmentRepo = 
                com.example.bloodhero.repository.AppointmentRepository.getInstance(this);
        com.example.bloodhero.utils.UserHelper userHelper = new com.example.bloodhero.utils.UserHelper();
        com.example.bloodhero.models.User currentUser = userHelper.getCurrentUser(this);
        
        if (currentUser != null) {
            // Create appointment for urgent blood request
            String appointmentId = "urgent_" + request.getId() + "_" + System.currentTimeMillis();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            String today = sdf.format(new java.util.Date());
            
            com.example.bloodhero.models.Appointment appointment = new com.example.bloodhero.models.Appointment(
                    appointmentId,
                    currentUser.getId(),
                    "urgent_" + request.getId(),
                    "Urgent Blood Request - " + request.getPatientName(),
                    request.getHospital() + ", " + request.getCity(),
                    today,
                    "As soon as possible",
                    com.example.bloodhero.models.Appointment.Status.CONFIRMED
            );
            
            appointmentRepo.createAppointment(appointment);
        }
    }
    
    private void showNextStepsDialog(BloodRequest request) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_next_steps, null);
        AlertDialog nextStepsDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        
        if (nextStepsDialog.getWindow() != null) {
            nextStepsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        TextView tvHospitalInfo = dialogView.findViewById(R.id.tvHospitalInfo);
        TextView tvCityInfo = dialogView.findViewById(R.id.tvCityInfo);
        TextView tvPhoneInfo = dialogView.findViewById(R.id.tvPhoneInfo);
        com.google.android.material.button.MaterialButton btnCallNow = dialogView.findViewById(R.id.btnCallNow);
        com.google.android.material.button.MaterialButton btnLater = dialogView.findViewById(R.id.btnLater);
        
        tvHospitalInfo.setText(request.getHospital());
        tvCityInfo.setText(request.getCity());
        tvPhoneInfo.setText(request.getContactPhone());
        
        btnCallNow.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + request.getContactPhone()));
            startActivity(callIntent);
            nextStepsDialog.dismiss();
        });
        
        btnLater.setOnClickListener(v -> nextStepsDialog.dismiss());
        
        nextStepsDialog.show();
    }

    @Override
    public void onCallClick(BloodRequest request) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + request.getContactPhone()));
        startActivity(callIntent);
    }
}
