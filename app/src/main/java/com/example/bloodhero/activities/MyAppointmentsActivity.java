package com.example.bloodhero.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.bloodhero.HomeActivity;
import com.example.bloodhero.R;
import com.example.bloodhero.models.Appointment;
import com.example.bloodhero.models.User;
import com.example.bloodhero.repository.AppointmentRepository;
import com.example.bloodhero.utils.UserHelper;

import java.util.List;

public class MyAppointmentsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private LinearLayout emptyState;
    private CardView cardAppointment;
    private TextView tvCampaignName, tvAppointmentDate, tvAppointmentTime, tvLocation, tvStatus;
    private Button btnCancelAppointment, btnReschedule;
    
    private AppointmentRepository appointmentRepository;
    private User currentUser;
    private Appointment currentAppointment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_appointments);

        currentUser = UserHelper.getCurrentUser(this);
        appointmentRepository = AppointmentRepository.getInstance(this);
        
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
            
            // Reset button visibility
            btnCancelAppointment.setVisibility(View.VISIBLE);
            btnReschedule.setVisibility(View.VISIBLE);

            // Set status color and button visibility based on status
            switch (currentAppointment.getStatus()) {
                case COMPLETED:
                    tvStatus.setBackgroundResource(R.drawable.bg_status_completed);
                    btnCancelAppointment.setVisibility(View.GONE);
                    btnReschedule.setVisibility(View.GONE);
                    break;
                case CANCELLED:
                    tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
                    btnCancelAppointment.setVisibility(View.GONE);
                    btnReschedule.setVisibility(View.GONE);
                    break;
                case SCHEDULED:
                default:
                    tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                    break;
            }
        } else {
            // Show empty state
            emptyState.setVisibility(View.VISIBLE);
            cardAppointment.setVisibility(View.GONE);
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
        appointmentRepository.updateStatus(currentAppointment.getId(), Appointment.Status.CANCELLED);
        
        Toast.makeText(this, "Appointment cancelled successfully", Toast.LENGTH_SHORT).show();
        loadAppointment(); // Refresh UI
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAppointment();
    }
}
