package com.example.bloodhero.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.bloodhero.R;
import com.example.bloodhero.models.Appointment;
import com.example.bloodhero.models.User;
import com.example.bloodhero.repository.AppointmentRepository;
import com.example.bloodhero.repository.UserRepository;
import com.example.bloodhero.utils.VerificationCodeGenerator;
import com.example.bloodhero.utils.EnhancedDialogHelper;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for managing donation beds and tracking donor flow
 */
public class BedManagementActivity extends AppCompatActivity {
    
    private static final int TOTAL_BEDS = 4;
    
    private AppointmentRepository appointmentRepository;
    private UserRepository userRepository;
    
    // UI components
    private ImageButton btnBack;
    private TextView tvWaitingCount;
    private LinearLayout layoutWaitingList;
    private CardView[] bedCards;
    private TextView[] bedStatusTexts;
    private TextView[] bedDonorNames;
    private MaterialButton[] bedActionButtons;
    private MaterialButton[] bedCheckButtons; // Check bed condition buttons
    private android.widget.ImageView[] bedStatusIcons; // Icons to show bed condition
    
    private List<Appointment> waitingAppointments;
    private Appointment[] bedsInUse; // Track which appointment is in each bed
    private String[] bedConditions; // Track bed conditions: "good", "needs_check", "maintenance"
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bed_management);
        
        appointmentRepository = AppointmentRepository.getInstance(this);
        userRepository = UserRepository.getInstance(this);
        
        waitingAppointments = new ArrayList<>();
        bedsInUse = new Appointment[TOTAL_BEDS];
        bedConditions = new String[TOTAL_BEDS];
        // Initialize all beds as "good" condition
        for (int i = 0; i < TOTAL_BEDS; i++) {
            bedConditions[i] = "good";
        }
        
        initViews();
        loadData();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvWaitingCount = findViewById(R.id.tvWaitingCount);
        layoutWaitingList = findViewById(R.id.layoutWaitingList);
        
        bedCards = new CardView[TOTAL_BEDS];
        bedStatusTexts = new TextView[TOTAL_BEDS];
        bedDonorNames = new TextView[TOTAL_BEDS];
        bedActionButtons = new MaterialButton[TOTAL_BEDS];
        bedCheckButtons = new MaterialButton[TOTAL_BEDS];
        bedStatusIcons = new android.widget.ImageView[TOTAL_BEDS];
        
        bedCards[0] = findViewById(R.id.bedCard1);
        bedCards[1] = findViewById(R.id.bedCard2);
        bedCards[2] = findViewById(R.id.bedCard3);
        bedCards[3] = findViewById(R.id.bedCard4);
        
        bedStatusTexts[0] = findViewById(R.id.tvBed1Status);
        bedStatusTexts[1] = findViewById(R.id.tvBed2Status);
        bedStatusTexts[2] = findViewById(R.id.tvBed3Status);
        bedStatusTexts[3] = findViewById(R.id.tvBed4Status);
        
        bedDonorNames[0] = findViewById(R.id.tvBed1Donor);
        bedDonorNames[1] = findViewById(R.id.tvBed2Donor);
        bedDonorNames[2] = findViewById(R.id.tvBed3Donor);
        bedDonorNames[3] = findViewById(R.id.tvBed4Donor);
        
        bedActionButtons[0] = findViewById(R.id.btnBed1Action);
        bedActionButtons[1] = findViewById(R.id.btnBed2Action);
        bedActionButtons[2] = findViewById(R.id.btnBed3Action);
        bedActionButtons[3] = findViewById(R.id.btnBed4Action);
        
        bedCheckButtons[0] = findViewById(R.id.btnBed1Check);
        bedCheckButtons[1] = findViewById(R.id.btnBed2Check);
        bedCheckButtons[2] = findViewById(R.id.btnBed3Check);
        bedCheckButtons[3] = findViewById(R.id.btnBed4Check);
        
        bedStatusIcons[0] = findViewById(R.id.ivBed1StatusIcon);
        bedStatusIcons[1] = findViewById(R.id.ivBed2StatusIcon);
        bedStatusIcons[2] = findViewById(R.id.ivBed3StatusIcon);
        bedStatusIcons[3] = findViewById(R.id.ivBed4StatusIcon);
        
        btnBack.setOnClickListener(v -> finish());
        
        for (int i = 0; i < TOTAL_BEDS; i++) {
            final int bedNumber = i + 1;
            bedActionButtons[i].setOnClickListener(v -> handleBedAction(bedNumber));
            bedCheckButtons[i].setOnClickListener(v -> handleCheckBed(bedNumber));
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
    
    private void loadData() {
        // Load checked-in appointments (waiting for bed)
        List<Appointment> checkedIn = appointmentRepository.getAppointmentsByStatus(Appointment.Status.CHECKED_IN);
        waitingAppointments.clear();
        waitingAppointments.addAll(checkedIn);
        
        // Load in-progress appointments (on beds)
        List<Appointment> inProgress = appointmentRepository.getAppointmentsByStatus(Appointment.Status.IN_PROGRESS);
        
        // Clear beds array
        for (int i = 0; i < TOTAL_BEDS; i++) {
            bedsInUse[i] = null;
        }
        
        // Populate beds with in-progress appointments
        for (Appointment appointment : inProgress) {
            if (appointment.getBedNumber() != null && 
                appointment.getBedNumber() >= 1 && 
                appointment.getBedNumber() <= TOTAL_BEDS) {
                bedsInUse[appointment.getBedNumber() - 1] = appointment;
            }
        }
        
        updateUI();
    }
    
    private void updateUI() {
        // Update waiting list
        tvWaitingCount.setText(String.valueOf(waitingAppointments.size()));
        layoutWaitingList.removeAllViews();
        
        for (Appointment appointment : waitingAppointments) {
            View waitingItem = getLayoutInflater().inflate(R.layout.item_waiting_donor, layoutWaitingList, false);
            TextView tvName = waitingItem.findViewById(R.id.tvDonorName);
            TextView tvTime = waitingItem.findViewById(R.id.tvCheckInTime);
            MaterialButton btnAssign = waitingItem.findViewById(R.id.btnAssignBed);
            
            User donor = userRepository.getUserById(appointment.getUserId());
            if (donor != null) {
                tvName.setText(donor.getName());
                tvTime.setText("Checked in: " + appointment.getTime());
                
                btnAssign.setOnClickListener(v -> showBedSelectionDialog(appointment));
            }
            
            layoutWaitingList.addView(waitingItem);
        }
        
        // Update beds
        for (int i = 0; i < TOTAL_BEDS; i++) {
            if (bedsInUse[i] != null) {
                // Bed occupied
                Appointment appointment = bedsInUse[i];
                User donor = userRepository.getUserById(appointment.getUserId());
                
                bedStatusTexts[i].setText("IN USE");
                bedStatusTexts[i].setTextColor(getResources().getColor(R.color.error));
                bedStatusIcons[i].setImageResource(R.drawable.ic_person);
                bedStatusIcons[i].setColorFilter(getResources().getColor(R.color.error));
                bedDonorNames[i].setText(donor != null ? donor.getName() : "Unknown");
                bedDonorNames[i].setVisibility(View.VISIBLE);
                bedActionButtons[i].setText("Complete Donation");
                bedActionButtons[i].setEnabled(true);
                bedCheckButtons[i].setVisibility(View.GONE); // Hide check button when bed in use
            } else {
                // Bed available - check condition
                if ("maintenance".equals(bedConditions[i])) {
                    bedStatusTexts[i].setText("MAINTENANCE");
                    bedStatusTexts[i].setTextColor(getResources().getColor(R.color.warning));
                    bedStatusIcons[i].setImageResource(R.drawable.ic_warning);
                    bedStatusIcons[i].setColorFilter(getResources().getColor(R.color.warning));
                    bedDonorNames[i].setText("Needs service");
                    bedDonorNames[i].setVisibility(View.VISIBLE);
                    bedDonorNames[i].setTextColor(getResources().getColor(R.color.warning));
                    bedActionButtons[i].setText("Mark as Fixed");
                    bedActionButtons[i].setEnabled(true);
                    bedCheckButtons[i].setVisibility(View.GONE); // Hide check button during maintenance
                    final int bedIndex = i; // Make final copy for lambda
                    final int bedNum = i + 1;
                    bedActionButtons[i].setOnClickListener(v -> {
                        bedConditions[bedIndex] = "good";
                        Toast.makeText(this, "Bed " + bedNum + " marked as ready", Toast.LENGTH_SHORT).show();
                        updateUI();
                    });
                } else if ("needs_check".equals(bedConditions[i])) {
                    bedStatusTexts[i].setText("CHECK NEEDED");
                    bedStatusTexts[i].setTextColor(getResources().getColor(R.color.info));
                    bedStatusIcons[i].setImageResource(R.drawable.ic_info);
                    bedStatusIcons[i].setColorFilter(getResources().getColor(R.color.info));
                    bedDonorNames[i].setText("Inspection required");
                    bedDonorNames[i].setVisibility(View.VISIBLE);
                    bedDonorNames[i].setTextColor(getResources().getColor(R.color.info));
                    bedActionButtons[i].setText("Check Bed");
                    bedActionButtons[i].setEnabled(true);
                    bedCheckButtons[i].setVisibility(View.GONE); // Hide separate check button
                } else {
                    bedStatusTexts[i].setText("AVAILABLE");
                    bedStatusTexts[i].setTextColor(getResources().getColor(R.color.success));
                    bedStatusIcons[i].setImageResource(R.drawable.ic_check_circle);
                    bedStatusIcons[i].setColorFilter(getResources().getColor(R.color.success));
                    bedDonorNames[i].setText("");
                    bedDonorNames[i].setVisibility(View.GONE);
                    bedActionButtons[i].setText("Assign Donor");
                    bedActionButtons[i].setEnabled(!waitingAppointments.isEmpty());
                    bedCheckButtons[i].setVisibility(View.VISIBLE); // Show check button for available beds
                }
            }
        }
    }
    
    private void handleBedAction(int bedNumber) {
        int bedIndex = bedNumber - 1;
        
        if (bedsInUse[bedIndex] != null) {
            // Complete donation
            completeDonation(bedNumber, bedsInUse[bedIndex]);
        } else {
            // Assign donor to bed
            if (!waitingAppointments.isEmpty()) {
                assignDonorToBed(bedNumber, waitingAppointments.get(0));
            }
        }
    }
    
    private void handleCheckBed(int bedNumber) {
        final int bedIndex = bedNumber - 1;
        
        // Show bed inspection dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_bed_condition_check, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);
        
        // Set bed info
        TextView tvBedTitle = dialogView.findViewById(R.id.tvBedTitle);
        TextView tvDonorInfo = dialogView.findViewById(R.id.tvDonorInfo);
        MaterialButton btnBedReady = dialogView.findViewById(R.id.btnBedReady);
        MaterialButton btnNeedsMaintenance = dialogView.findViewById(R.id.btnNeedsMaintenance);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        
        tvBedTitle.setText("Check Bed " + bedNumber + " Condition");
        tvDonorInfo.setText("Inspect the bed and confirm its status");
        
        btnBedReady.setOnClickListener(v -> {
            bedConditions[bedIndex] = "good";
            dialog.dismiss();
            Toast.makeText(this, "Bed " + bedNumber + " confirmed ready for use", Toast.LENGTH_SHORT).show();
            updateUI();
        });
        
        btnNeedsMaintenance.setOnClickListener(v -> {
            bedConditions[bedIndex] = "maintenance";
            dialog.dismiss();
            Toast.makeText(this, "Bed " + bedNumber + " marked for maintenance", Toast.LENGTH_SHORT).show();
            updateUI();
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    private void showBedSelectionDialog(Appointment appointment) {
        List<String> availableBeds = new ArrayList<>();
        List<Integer> availableBedNumbers = new ArrayList<>();
        
        for (int i = 0; i < TOTAL_BEDS; i++) {
            if (bedsInUse[i] == null) {
                availableBeds.add("Bed " + (i + 1));
                availableBedNumbers.add(i + 1);
            }
        }
        
        if (availableBeds.isEmpty()) {
            Toast.makeText(this, "No beds available. Please wait.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Assign to Bed")
                .setItems(availableBeds.toArray(new String[0]), (dialog, which) -> {
                    assignDonorToBed(availableBedNumbers.get(which), appointment);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void assignDonorToBed(int bedNumber, Appointment appointment) {
        // Check bed condition first
        checkBedConditionBeforeAssignment(bedNumber, appointment);
    }
    
    private void checkBedConditionBeforeAssignment(int bedNumber, Appointment appointment) {
        final int bedIndex = bedNumber - 1;
        User donor = userRepository.getUserById(appointment.getUserId());
        String donorName = donor != null ? donor.getName() : "Unknown";
        
        // Create custom dialog with enhanced design
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_bed_condition_check, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);
        
        // Set bed and donor info
        TextView tvBedTitle = dialogView.findViewById(R.id.tvBedTitle);
        TextView tvDonorInfo = dialogView.findViewById(R.id.tvDonorInfo);
        MaterialButton btnBedReady = dialogView.findViewById(R.id.btnBedReady);
        MaterialButton btnNeedsMaintenance = dialogView.findViewById(R.id.btnNeedsMaintenance);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        
        tvBedTitle.setText("Check Bed " + bedNumber + " Condition");
        tvDonorInfo.setText("For donor: " + donorName);
        
        btnBedReady.setOnClickListener(v -> {
            bedConditions[bedIndex] = "good";
            dialog.dismiss();
            proceedWithBedAssignment(bedNumber, appointment);
        });
        
        btnNeedsMaintenance.setOnClickListener(v -> {
            bedConditions[bedIndex] = "maintenance";
            dialog.dismiss();
            Toast.makeText(this, "Bed " + bedNumber + " marked for maintenance. Please select another bed.", Toast.LENGTH_LONG).show();
            findAlternativeBed(appointment);
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    private void findAlternativeBed(Appointment appointment) {
        // Find next available bed in good condition
        for (int i = 0; i < TOTAL_BEDS; i++) {
            if (bedsInUse[i] == null && "good".equals(bedConditions[i])) {
                int bedNumber = i + 1;
                EnhancedDialogHelper.showConfirmationDialog(
                        this,
                        "Alternative Bed Available",
                        "Bed " + bedNumber + " is available and in good condition.\n\nAssign donor to Bed " + bedNumber + "?",
                        "Assign",
                        "Choose Manually",
                        () -> checkBedConditionBeforeAssignment(bedNumber, appointment)
                );
                return;
            }
        }
        // No alternative bed found
        Toast.makeText(this, "No other beds available. Please wait or check bed conditions.", Toast.LENGTH_LONG).show();
    }
    
    private void proceedWithBedAssignment(int bedNumber, Appointment appointment) {
        User donor = userRepository.getUserById(appointment.getUserId());
        String donorName = donor != null ? donor.getName() : "Unknown";
        
        EnhancedDialogHelper.showConfirmationDialog(
                this,
                "Assign to Bed " + bedNumber + "?",
                "Assign " + donorName + " to bed " + bedNumber + "?",
                "Assign",
                "Cancel",
                () -> {
                    appointment.assignToBed(bedNumber);
                    boolean success = appointmentRepository.assignToBed(appointment.getId(), bedNumber);
                    
                    if (success) {
                        // Broadcast the update
                        Intent intent = new Intent(MyAppointmentsActivity.ACTION_APPOINTMENT_UPDATED);
                        intent.putExtra(MyAppointmentsActivity.EXTRA_APPOINTMENT_ID, appointment.getId());
                        LocalBroadcastManager.getInstance(BedManagementActivity.this).sendBroadcast(intent);
                        
                        Toast.makeText(BedManagementActivity.this, "Donor assigned to bed " + bedNumber, Toast.LENGTH_SHORT).show();
                        loadData();
                    } else {
                        Toast.makeText(BedManagementActivity.this, "Failed to assign bed", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
    
    private void completeDonation(int bedNumber, Appointment appointment) {
        User donor = userRepository.getUserById(appointment.getUserId());
        String donorName = donor != null ? donor.getName() : "Unknown";
        
        EnhancedDialogHelper.showConfirmationDialog(
                this,
                "Complete Donation?",
                "Has " + donorName + " completed their donation on bed " + bedNumber + "?\n\n" +
                "A verification code will be generated for the donor to enter in their app.",
                "Generate Code",
                "Cancel",
                () -> {
                    // Generate verification code
                    String code = VerificationCodeGenerator.generateCode();
                    appointment.setPendingVerification(code);
                    
                    boolean success = appointmentRepository.setPendingVerification(appointment.getId(), code);
                    
                    if (success) {
                        // Broadcast the update
                        Intent intent = new Intent(MyAppointmentsActivity.ACTION_APPOINTMENT_UPDATED);
                        intent.putExtra(MyAppointmentsActivity.EXTRA_APPOINTMENT_ID, appointment.getId());
                        LocalBroadcastManager.getInstance(BedManagementActivity.this).sendBroadcast(intent);
                        
                        showVerificationCodeDialog(donorName, code);
                        loadData();
                    } else {
                        Toast.makeText(BedManagementActivity.this, "Failed to generate code", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
    
    private void showVerificationCodeDialog(String donorName, String code) {
        EnhancedDialogHelper.showInfoDialog(
                this,
                "✓ Donation Complete",
                "Give this code to " + donorName + " to enter in their app:\n\n" +
                "CODE: " + code + "\n\n" +
                "They must enter this code to complete the donation process.",
                "OK",
                null
        );
    }
}
