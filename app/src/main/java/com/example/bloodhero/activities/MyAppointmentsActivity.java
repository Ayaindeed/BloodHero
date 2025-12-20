package com.example.bloodhero.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.bloodhero.HomeActivity;
import com.example.bloodhero.R;
import com.example.bloodhero.models.Appointment;
import com.example.bloodhero.models.Donation;
import com.example.bloodhero.models.User;
import com.example.bloodhero.repository.AppointmentRepository;
import com.example.bloodhero.repository.DonationRepository;
import com.example.bloodhero.repository.UserRepository;
import com.example.bloodhero.utils.QRCodeHelper;
import com.example.bloodhero.utils.UserHelper;
import com.example.bloodhero.utils.VerificationCodeGenerator;

import java.util.List;
import java.util.UUID;

public class MyAppointmentsActivity extends AppCompatActivity {

    public static final String ACTION_APPOINTMENT_UPDATED = "com.example.bloodhero.APPOINTMENT_UPDATED";
    public static final String EXTRA_APPOINTMENT_ID = "appointment_id";

    private ImageButton btnBack;
    private LinearLayout emptyState;
    private CardView cardAppointment;
    private TextView tvCampaignName, tvAppointmentDate, tvAppointmentTime, tvLocation, tvStatus;
    private Button btnCancelAppointment, btnReschedule, btnEnterCode, btnCheckIn;
    private ImageView ivQRCode;
    private LinearLayout qrCodeSection;
    
    private AppointmentRepository appointmentRepository;
    private DonationRepository donationRepository;
    private UserRepository userRepository;
    private User currentUser;
    private Appointment currentAppointment;
    
    private BroadcastReceiver appointmentUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String updatedAppointmentId = intent.getStringExtra(EXTRA_APPOINTMENT_ID);
            // Reload appointment if it's the current one or if no specific ID was sent
            if (updatedAppointmentId == null || 
                (currentAppointment != null && currentAppointment.getId().equals(updatedAppointmentId))) {
                loadAppointment();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_appointments);

        currentUser = UserHelper.getCurrentUser(this);
        appointmentRepository = AppointmentRepository.getInstance(this);
        donationRepository = DonationRepository.getInstance(this);
        userRepository = UserRepository.getInstance(this);
        
        initViews();
        loadAppointment();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        emptyState = findViewById(R.id.emptyState);
        cardAppointment = findViewById(R.id.cardAppointment);
        tvCampaignName = findViewById(R.id.tvCampaignName);
        tvAppointmentDate = findViewById(R.id.tvAppointmentDate);
        tvAppointmentTime = findViewById(R.id.tvAppointmentTime);
        tvLocation = findViewById(R.id.tvLocation);
        tvStatus = findViewById(R.id.tvStatus);
        btnCancelAppointment = findViewById(R.id.btnCancelAppointment);
        btnReschedule = findViewById(R.id.btnReschedule);
        btnEnterCode = findViewById(R.id.btnEnterCode);
        btnCheckIn = findViewById(R.id.btnCheckIn);
        ivQRCode = findViewById(R.id.ivQRCode);
        qrCodeSection = findViewById(R.id.qrCodeSection);
    }

    private void loadAppointment() {
        if (currentUser == null) {
            emptyState.setVisibility(View.VISIBLE);
            cardAppointment.setVisibility(View.GONE);
            return;
        }
        
        // Get user's appointments from SQLite, ordered by date descending
        List<Appointment> appointments = appointmentRepository.getAppointmentsByUserId(currentUser.getId());
        
        // Find the most recent non-cancelled appointment
        currentAppointment = null;
        for (Appointment appt : appointments) {
            if (appt.getStatus() != Appointment.Status.CANCELLED) {
                currentAppointment = appt;
                break;
            }
        }

        if (currentAppointment != null) {
            // Show appointment
            emptyState.setVisibility(View.GONE);
            cardAppointment.setVisibility(View.VISIBLE);

            tvCampaignName.setText(currentAppointment.getCampaignName() != null ? 
                currentAppointment.getCampaignName() : "Blood Donation");
            tvAppointmentDate.setText(formatDate(currentAppointment.getDate()));
            tvAppointmentTime.setText(currentAppointment.getTime());
            tvLocation.setText(currentAppointment.getLocation() != null ? 
                currentAppointment.getLocation() : "Location not specified");
            tvStatus.setText(currentAppointment.getStatus().toString());
            
            // Generate and display QR code
            displayQRCode(currentAppointment);
            
            // Reset button visibility
            btnCancelAppointment.setVisibility(View.VISIBLE);
            btnReschedule.setVisibility(View.VISIBLE);
            btnEnterCode.setVisibility(View.GONE);
            btnCheckIn.setVisibility(View.GONE);

            // Set status color and button visibility based on status
            switch (currentAppointment.getStatus()) {
                case CONFIRMED:
                    tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                    tvStatus.setText("CONFIRMED");
                    qrCodeSection.setVisibility(View.VISIBLE);
                    btnCheckIn.setVisibility(View.VISIBLE); // Show check-in button for testing
                    break;
                case CHECKED_IN:
                    tvStatus.setBackgroundResource(R.drawable.bg_status_completed);
                    tvStatus.setText("CHECKED IN");
                    btnCancelAppointment.setVisibility(View.GONE);
                    btnReschedule.setVisibility(View.GONE);
                    qrCodeSection.setVisibility(View.GONE);
                    break;
                case IN_PROGRESS:
                    tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                    tvStatus.setText("DONATING - BED " + (currentAppointment.getBedNumber() != null ? currentAppointment.getBedNumber() : ""));
                    btnCancelAppointment.setVisibility(View.GONE);
                    btnReschedule.setVisibility(View.GONE);
                    qrCodeSection.setVisibility(View.GONE);
                    break;
                case PENDING_VERIFICATION:
                    tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                    tvStatus.setText("ENTER VERIFICATION CODE");
                    btnCancelAppointment.setVisibility(View.GONE);
                    btnReschedule.setVisibility(View.GONE);
                    btnEnterCode.setVisibility(View.VISIBLE);
                    qrCodeSection.setVisibility(View.GONE);
                    break;
                case COMPLETED:
                    tvStatus.setBackgroundResource(R.drawable.bg_status_completed);
                    btnCancelAppointment.setVisibility(View.GONE);
                    btnReschedule.setVisibility(View.GONE);
                    qrCodeSection.setVisibility(View.GONE);
                    break;
                case CANCELLED:
                    tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
                    btnCancelAppointment.setVisibility(View.GONE);
                    btnReschedule.setVisibility(View.GONE);
                    qrCodeSection.setVisibility(View.GONE);
                    break;
                case SCHEDULED:
                default:
                    tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                    qrCodeSection.setVisibility(View.VISIBLE);
                    break;
            }
        } else {
            // Show empty state
            emptyState.setVisibility(View.VISIBLE);
            cardAppointment.setVisibility(View.GONE);
        }
    }
    
    private void displayQRCode(Appointment appointment) {
        if (appointment == null || appointment.getId() == null) {
            qrCodeSection.setVisibility(View.GONE);
            return;
        }
        
        // Generate QR code bitmap
        Bitmap qrBitmap = QRCodeHelper.generateQRCode(appointment.getId(), 400);
        if (qrBitmap != null) {
            ivQRCode.setImageBitmap(qrBitmap);
            qrCodeSection.setVisibility(View.VISIBLE);
        } else {
            qrCodeSection.setVisibility(View.GONE);
        }
    }

    private String formatDate(String dateStr) {
        // Convert yyyy-MM-dd to readable format
        try {
            String[] parts = dateStr.split("-");
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            int monthIndex = Integer.parseInt(parts[1]) - 1;
            return months[monthIndex] + " " + parts[2] + ", " + parts[0];
        } catch (Exception e) {
            return dateStr;
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnCancelAppointment.setOnClickListener(v -> showCancelConfirmation());

        btnReschedule.setOnClickListener(v -> {
            // Go to campaigns to book a new appointment
            Intent intent = new Intent(this, CampaignsActivity.class);
            startActivity(intent);
        });
        
        btnEnterCode.setOnClickListener(v -> showVerificationCodeDialog());
        
        btnCheckIn.setOnClickListener(v -> performCheckIn());

        findViewById(R.id.btnFindCampaign).setOnClickListener(v -> {
            Intent intent = new Intent(this, CampaignsActivity.class);
            startActivity(intent);
        });
    }

    private void showCancelConfirmation() {
        new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("Cancel Appointment")
                .setMessage("Are you sure you want to cancel this appointment? This action cannot be undone.")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    cancelAppointment();
                })
                .setNegativeButton("No, Keep It", null)
                .show();
    }

    private void cancelAppointment() {
        if (currentAppointment == null) {
            Toast.makeText(this, "No appointment to cancel", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Update appointment status in SQLite
        boolean success = appointmentRepository.updateStatus(currentAppointment.getId(), Appointment.Status.CANCELLED);
        
        if (success) {
            Toast.makeText(this, "Appointment cancelled successfully", Toast.LENGTH_SHORT).show();
            // Reload appointment from database to ensure sync
            currentAppointment = null;
            loadAppointment(); // Refresh UI with updated data
        } else {
            Toast.makeText(this, "Failed to cancel appointment", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register broadcast receiver for real-time updates
        LocalBroadcastManager.getInstance(this).registerReceiver(
            appointmentUpdateReceiver,
            new IntentFilter(ACTION_APPOINTMENT_UPDATED)
        );
        loadAppointment();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Unregister broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(appointmentUpdateReceiver);
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // Refresh when window regains focus (e.g., coming back from QR scanner)
        if (hasFocus) {
            loadAppointment();
        }
    }
    
    private void performCheckIn() {
        if (currentAppointment == null) {
            Toast.makeText(this, "No appointment found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Check In")
                .setMessage("Check in for your appointment now?")
                .setPositiveButton("Check In", (dialog, which) -> {
                    // Update status to CHECKED_IN
                    boolean success = appointmentRepository.updateStatus(
                            currentAppointment.getId(), 
                            Appointment.Status.CHECKED_IN
                    );
                    
                    if (success) {
                        Toast.makeText(this, "✓ Checked in successfully!", Toast.LENGTH_SHORT).show();
                        loadAppointment(); // Refresh UI
                    } else {
                        Toast.makeText(this, "Failed to check in", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void showVerificationCodeDialog() {
        if (currentAppointment == null) {
            Toast.makeText(this, "No appointment found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create input field
        final EditText input = new EditText(this);
        input.setHint("Enter 4-character code");
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4), new InputFilter.AllCaps()});
        input.setPadding(50, 30, 50, 30);
        
        new AlertDialog.Builder(this)
                .setTitle("Enter Verification Code")
                .setMessage("Enter the 4-character code given by the admin after your donation:")
                .setView(input)
                .setPositiveButton("Verify", (dialog, which) -> {
                    String enteredCode = input.getText().toString().trim().toUpperCase();
                    verifyDonationCode(enteredCode);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void verifyDonationCode(String enteredCode) {
        if (enteredCode.length() != 4) {
            Toast.makeText(this, "Code must be 4 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!VerificationCodeGenerator.isValidFormat(enteredCode)) {
            Toast.makeText(this, "Invalid code format", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if code matches
        if (currentAppointment.verifyCode(enteredCode)) {
            completeDonation();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Invalid Code")
                    .setMessage("The code you entered is incorrect. Please try again or contact the donation center.")
                    .setPositiveButton("Try Again", (dialog, which) -> showVerificationCodeDialog())
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }
    
    private void completeDonation() {
        // Create donation record
        Donation donation = new Donation();
        donation.setId(UUID.randomUUID().toString());
        donation.setUserId(currentUser.getId());
        donation.setCampaignId(currentAppointment.getCampaignId());
        donation.setCampaignName(currentAppointment.getCampaignName());
        donation.setLocation(currentAppointment.getLocation());
        donation.setDate(currentAppointment.getDate());
        donation.setBloodType(currentUser.getBloodType());
        donation.setPointsEarned(50); // Base points
        donation.setStatus("COMPLETED");
        
        // Save donation
        boolean donationSaved = donationRepository.saveDonation(donation);
        
        // Update appointment status to COMPLETED
        boolean appointmentUpdated = appointmentRepository.updateStatus(
                currentAppointment.getId(), 
                Appointment.Status.COMPLETED
        );
        
        // Broadcast the update (even though we're in the same activity, in case there are other listeners)
        if (appointmentUpdated) {
            Intent intent = new Intent(ACTION_APPOINTMENT_UPDATED);
            intent.putExtra(EXTRA_APPOINTMENT_ID, currentAppointment.getId());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
        
        // Update user stats - incrementDonations already updates count, points, and last donation date
        userRepository.incrementDonations(currentUser.getId(), 50);
        
        // Reload current user to reflect updated values
        currentUser = UserHelper.getCurrentUser(this);
        
        if (donationSaved && appointmentUpdated) {
            showCompletionDialog();
        } else {
            Toast.makeText(this, "Error completing donation. Please contact support.", Toast.LENGTH_LONG).show();
        }
    }
    
    private void showCompletionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🎉 Donation Complete!")
                .setMessage("Thank you for your donation!\n\n" +
                        "You've earned 50 points!\n" +
                        "Total donations: " + currentUser.getTotalDonations() + "\n" +
                        "Total points: " + currentUser.getTotalPoints())
                .setPositiveButton("OK", (dialog, which) -> {
                    loadAppointment(); // Refresh UI
                })
                .setCancelable(false)
                .show();
    }
}
