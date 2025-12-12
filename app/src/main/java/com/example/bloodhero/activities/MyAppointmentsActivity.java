package com.example.bloodhero.activities;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class MyAppointmentsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BloodHeroPrefs";

    private ImageButton btnBack;
    private LinearLayout emptyState;
    private CardView cardAppointment;
    private TextView tvCampaignName, tvAppointmentDate, tvAppointmentTime, tvLocation, tvStatus;
    private Button btnCancelAppointment, btnReschedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_appointments);

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
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String campaignName = prefs.getString("last_appointment_campaign", null);
        String appointmentDate = prefs.getString("last_appointment_date", null);
        String appointmentTime = prefs.getString("last_appointment_time", null);
        String location = prefs.getString("last_appointment_location", null);
        String status = prefs.getString("last_appointment_status", "Confirmed");

        if (campaignName != null && appointmentDate != null) {
            // Show appointment
            emptyState.setVisibility(View.GONE);
            cardAppointment.setVisibility(View.VISIBLE);

            tvCampaignName.setText(campaignName);
            tvAppointmentDate.setText(formatDate(appointmentDate));
            tvAppointmentTime.setText(appointmentTime);
            tvLocation.setText(location != null ? location : "Location not specified");
            tvStatus.setText(status);

            // Set status color
            if ("Confirmed".equals(status)) {
                tvStatus.setTextColor(getColor(R.color.success));
                tvStatus.setBackgroundResource(R.drawable.bg_status_confirmed);
            } else if ("Completed".equals(status)) {
                tvStatus.setTextColor(getColor(R.color.info));
                tvStatus.setBackgroundResource(R.drawable.bg_status_completed);
                btnCancelAppointment.setVisibility(View.GONE);
                btnReschedule.setVisibility(View.GONE);
            } else if ("Cancelled".equals(status)) {
                tvStatus.setTextColor(getColor(R.color.error));
                tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
                btnCancelAppointment.setVisibility(View.GONE);
                btnReschedule.setVisibility(View.GONE);
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
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("last_appointment_status", "Cancelled");
        editor.apply();

        Toast.makeText(this, "Appointment cancelled successfully", Toast.LENGTH_SHORT).show();
        loadAppointment(); // Refresh UI
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAppointment();
    }
}
