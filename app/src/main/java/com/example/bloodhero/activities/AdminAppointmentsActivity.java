package com.example.bloodhero.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodhero.R;
import com.example.bloodhero.models.Appointment;
import com.example.bloodhero.models.Donation;
import com.example.bloodhero.models.User;
import com.example.bloodhero.repository.AppointmentRepository;
import com.example.bloodhero.repository.DonationRepository;
import com.example.bloodhero.repository.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminAppointmentsActivity extends AppCompatActivity {

    private UserRepository userRepository;
    private AppointmentRepository appointmentRepository;
    private DonationRepository donationRepository;

    private ImageButton btnBack;
    private TabLayout tabLayout;
    private RecyclerView rvAppointments;
    private View emptyState;
    
    private AppointmentAdapter adapter;
    private List<AppointmentDisplay> allAppointments;
    private List<AppointmentDisplay> filteredAppointments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_appointments);

        userRepository = UserRepository.getInstance(this);
        appointmentRepository = AppointmentRepository.getInstance(this);
        donationRepository = DonationRepository.getInstance(this);
        initViews();
        setupTabs();
        loadAppointments();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload appointments when returning to activity
        loadAppointments();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tabLayout = findViewById(R.id.tabLayout);
        rvAppointments = findViewById(R.id.rvAppointments);
        emptyState = findViewById(R.id.emptyState);

        btnBack.setOnClickListener(v -> finish());

        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter();
        rvAppointments.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Pending"));
        tabLayout.addTab(tabLayout.newTab().setText("Confirmed"));
        tabLayout.addTab(tabLayout.newTab().setText("Completed"));
        tabLayout.addTab(tabLayout.newTab().setText("All"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterAppointments(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadAppointments() {
        allAppointments = new ArrayList<>();
        
        // Load all appointments from SQLite
        List<Appointment> storedAppointments = appointmentRepository.getAllAppointments();
        for (Appointment appt : storedAppointments) {
            // Get actual user data from SQLite
            User user = userRepository.getUserById(appt.getUserId());
            String donorName = "Unknown";
            String bloodType = "Unknown";
            
            if (user != null) {
                donorName = user.getName() != null ? user.getName() : "Unknown";
                bloodType = user.getBloodType() != null ? user.getBloodType() : "Unknown";
            }
            
            allAppointments.add(new AppointmentDisplay(
                appt.getId(),
                appt.getUserId(),
                donorName,
                bloodType,
                appt.getCampaignName(),
                formatDate(appt.getDate()),
                appt.getTimeSlot(),
                appt.getStatus().name()
            ));
        }

        filterAppointments(0); // Show pending by default
    }

    private String formatDate(String dateStr) {
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

    private void filterAppointments(int tabPosition) {
        filteredAppointments = new ArrayList<>();
        
        String filter;
        switch (tabPosition) {
            case 0:
                filter = "SCHEDULED";
                break;
            case 1:
                filter = "CONFIRMED";
                break;
            case 2:
                filter = "COMPLETED";
                break;
            default:
                filter = null; // Show all
        }

        for (AppointmentDisplay appointment : allAppointments) {
            if (filter == null || appointment.status.equals(filter)) {
                filteredAppointments.add(appointment);
            }
        }

        adapter.setAppointments(filteredAppointments);
        emptyState.setVisibility(filteredAppointments.isEmpty() ? View.VISIBLE : View.GONE);
        rvAppointments.setVisibility(filteredAppointments.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void confirmAppointment(AppointmentDisplay appointmentDisplay) {
        new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("Confirm Appointment")
                .setMessage("Confirm appointment for " + appointmentDisplay.donorName + "?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    // Update in SQLite - set to CONFIRMED, not COMPLETED
                    appointmentRepository.updateStatus(appointmentDisplay.id, Appointment.Status.CONFIRMED);
                    appointmentDisplay.status = "CONFIRMED";
                    
                    // Update the appointment in allAppointments list
                    for (AppointmentDisplay appt : allAppointments) {
                        if (appt.id.equals(appointmentDisplay.id)) {
                            appt.status = "CONFIRMED";
                            break;
                        }
                    }
                    
                    filterAppointments(tabLayout.getSelectedTabPosition());
                    Toast.makeText(this, "Appointment confirmed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void markAsCompleted(AppointmentDisplay appointmentDisplay) {
        new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("Mark as Completed")
                .setMessage("Mark " + appointmentDisplay.donorName + "'s donation as completed?\n\nThis will:\n• Award 50 points\n• Update donation count\n• Unlock eligible badges")
                .setPositiveButton("Complete", (dialog, which) -> {
                    // Update appointment status in SQLite
                    appointmentRepository.updateStatus(appointmentDisplay.id, Appointment.Status.COMPLETED);
                    
                    // Create and save donation record in SQLite
                    User user = userRepository.getUserById(appointmentDisplay.userId);
                    if (user != null) {
                        Donation donation = new Donation(
                                UUID.randomUUID().toString(),
                                user.getId(),
                                "",
                                appointmentDisplay.location,
                                appointmentDisplay.location,
                                appointmentDisplay.date,
                                user.getBloodType(),
                                50,
                                "COMPLETED"
                        );
                        donationRepository.saveDonation(donation);
                        
                        // Update user points and donations count
                        userRepository.incrementDonations(user.getId(), 50);
                    }
                    
                    appointmentDisplay.status = "COMPLETED";
                    
                    // Update the appointment in allAppointments list
                    for (AppointmentDisplay appt : allAppointments) {
                        if (appt.id.equals(appointmentDisplay.id)) {
                            appt.status = "COMPLETED";
                            break;
                        }
                    }
                    
                    filterAppointments(tabLayout.getSelectedTabPosition());
                    Toast.makeText(this, "Donation marked as completed! " + appointmentDisplay.donorName + " earned 50 points", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Inner class for displaying appointment data
    private static class AppointmentDisplay {
        String id;
        String userId;
        String donorName;
        String bloodType;
        String location;
        String date;
        String time;
        String status;

        AppointmentDisplay(String id, String userId, String donorName, String bloodType, String location,
                   String date, String time, String status) {
            this.id = id;
            this.userId = userId;
            this.donorName = donorName;
            this.bloodType = bloodType;
            this.location = location;
            this.date = date;
            this.time = time;
            this.status = status;
        }
    }

    // RecyclerView Adapter
    private class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {
        private List<AppointmentDisplay> appointments = new ArrayList<>();

        void setAppointments(List<AppointmentDisplay> appointments) {
            this.appointments = appointments;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_appointment, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AppointmentDisplay appointment = appointments.get(position);
            holder.bind(appointment);
        }

        @Override
        public int getItemCount() {
            return appointments.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDonorName, tvBloodType, tvLocation, tvDateTime, tvStatus;
            MaterialButton btnAction, btnSecondaryAction;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDonorName = itemView.findViewById(R.id.tvDonorName);
                tvBloodType = itemView.findViewById(R.id.tvBloodType);
                tvLocation = itemView.findViewById(R.id.tvLocation);
                tvDateTime = itemView.findViewById(R.id.tvDateTime);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                btnAction = itemView.findViewById(R.id.btnAction);
                btnSecondaryAction = itemView.findViewById(R.id.btnSecondaryAction);
            }

            void bind(AppointmentDisplay appointment) {
                tvDonorName.setText(appointment.donorName);
                tvBloodType.setText(appointment.bloodType);
                tvLocation.setText(appointment.location);
                tvDateTime.setText(appointment.date + " at " + appointment.time);
                tvStatus.setText(appointment.status);

                // Set status color
                int statusBg;
                switch (appointment.status) {
                    case "COMPLETED":
                        statusBg = R.drawable.bg_status_completed;
                        break;
                    case "CANCELLED":
                        statusBg = R.drawable.bg_status_pending;
                        break;
                    default:
                        statusBg = R.drawable.bg_status_confirmed;
                }
                tvStatus.setBackgroundResource(statusBg);

                // Configure action buttons based on status
                switch (appointment.status) {
                    case "SCHEDULED":
                        btnAction.setText("Confirm");
                        btnAction.setVisibility(View.VISIBLE);
                        btnSecondaryAction.setText("Reject");
                        btnSecondaryAction.setVisibility(View.VISIBLE);
                        btnAction.setOnClickListener(v -> confirmAppointment(appointment));
                        btnSecondaryAction.setOnClickListener(v -> {
                            // Reject logic
                            appointmentRepository.updateStatus(appointment.id, Appointment.Status.CANCELLED);
                            appointment.status = "CANCELLED";
                            
                            // Update the appointment in allAppointments list
                            for (AppointmentDisplay appt : allAppointments) {
                                if (appt.id.equals(appointment.id)) {
                                    appt.status = "CANCELLED";
                                    break;
                                }
                            }
                            
                            filterAppointments(tabLayout.getSelectedTabPosition());
                            Toast.makeText(itemView.getContext(), "Appointment rejected", Toast.LENGTH_SHORT).show();
                        });
                        break;
                    case "CONFIRMED":
                        btnAction.setText("Mark Completed");
                        btnAction.setVisibility(View.VISIBLE);
                        btnSecondaryAction.setVisibility(View.GONE);
                        btnAction.setOnClickListener(v -> markAsCompleted(appointment));
                        break;
                    case "COMPLETED":
                        btnAction.setVisibility(View.GONE);
                        btnSecondaryAction.setVisibility(View.GONE);
                        break;
                }
            }
        }
    }
}
