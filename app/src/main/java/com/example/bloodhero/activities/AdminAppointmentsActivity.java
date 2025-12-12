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
import com.example.bloodhero.utils.UserStorage;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class AdminAppointmentsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TabLayout tabLayout;
    private RecyclerView rvAppointments;
    private View emptyState;
    
    private AppointmentAdapter adapter;
    private List<Appointment> allAppointments;
    private List<Appointment> filteredAppointments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_appointments);

        initViews();
        setupTabs();
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
        
        // Load real appointments from UserStorage
        List<UserStorage.AppointmentData> storedAppointments = UserStorage.getAllAppointments(this);
        for (UserStorage.AppointmentData apptData : storedAppointments) {
            allAppointments.add(new Appointment(
                apptData.id,
                apptData.userEmail,
                apptData.userName,
                apptData.bloodType,
                apptData.campaignName,
                apptData.getFormattedDate(),
                apptData.time,
                apptData.status
            ));
        }

        filterAppointments(0); // Show pending by default
    }

    private void filterAppointments(int tabPosition) {
        filteredAppointments = new ArrayList<>();
        
        String filter;
        switch (tabPosition) {
            case 0:
                filter = "Pending";
                break;
            case 1:
                filter = "Confirmed";
                break;
            case 2:
                filter = "Completed";
                break;
            default:
                filter = null; // Show all
        }

        for (Appointment appointment : allAppointments) {
            if (filter == null || appointment.status.equals(filter)) {
                filteredAppointments.add(appointment);
            }
        }

        adapter.setAppointments(filteredAppointments);
        emptyState.setVisibility(filteredAppointments.isEmpty() ? View.VISIBLE : View.GONE);
        rvAppointments.setVisibility(filteredAppointments.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void confirmAppointment(Appointment appointment) {
        new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("Confirm Appointment")
                .setMessage("Confirm appointment for " + appointment.donorName + "?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    // Update in UserStorage
                    UserStorage.updateAppointmentStatus(this, appointment.id, "Confirmed");
                    appointment.status = "Confirmed";
                    filterAppointments(tabLayout.getSelectedTabPosition());
                    Toast.makeText(this, "Appointment confirmed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void markAsCompleted(Appointment appointment) {
        new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("Mark as Completed")
                .setMessage("Mark " + appointment.donorName + "'s donation as completed?\n\nThis will:\n• Award 50 points\n• Update donation count\n• Unlock eligible badges")
                .setPositiveButton("Complete", (dialog, which) -> {
                    // Update in UserStorage (this also increments donation count and unlocks badges)
                    UserStorage.updateAppointmentStatus(this, appointment.id, "Completed");
                    
                    // Save as a completed donation record
                    UserStorage.saveDonation(this, appointment.id, appointment.userEmail,
                            appointment.donorName, appointment.bloodType, appointment.location,
                            appointment.location, appointment.date, 50);
                    
                    appointment.status = "Completed";
                    filterAppointments(tabLayout.getSelectedTabPosition());
                    Toast.makeText(this, "Donation marked as completed! " + appointment.donorName + " earned 50 points", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Inner class for Appointment data
    private static class Appointment {
        String id;
        String userEmail;
        String donorName;
        String bloodType;
        String location;
        String date;
        String time;
        String status;

        Appointment(String id, String userEmail, String donorName, String bloodType, String location,
                   String date, String time, String status) {
            this.id = id;
            this.userEmail = userEmail;
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
        private List<Appointment> appointments = new ArrayList<>();

        void setAppointments(List<Appointment> appointments) {
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
            Appointment appointment = appointments.get(position);
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

            void bind(Appointment appointment) {
                tvDonorName.setText(appointment.donorName);
                tvBloodType.setText(appointment.bloodType);
                tvLocation.setText(appointment.location);
                tvDateTime.setText(appointment.date + " at " + appointment.time);
                tvStatus.setText(appointment.status);

                // Set status color
                int statusBg;
                switch (appointment.status) {
                    case "Confirmed":
                        statusBg = R.drawable.bg_status_confirmed;
                        break;
                    case "Completed":
                        statusBg = R.drawable.bg_status_completed;
                        break;
                    default:
                        statusBg = R.drawable.bg_status_pending;
                }
                tvStatus.setBackgroundResource(statusBg);

                // Configure action buttons based on status
                switch (appointment.status) {
                    case "Pending":
                        btnAction.setText("Confirm");
                        btnAction.setVisibility(View.VISIBLE);
                        btnSecondaryAction.setText("Reject");
                        btnSecondaryAction.setVisibility(View.VISIBLE);
                        btnAction.setOnClickListener(v -> confirmAppointment(appointment));
                        btnSecondaryAction.setOnClickListener(v -> {
                            // Reject logic
                            Toast.makeText(itemView.getContext(), "Appointment rejected", Toast.LENGTH_SHORT).show();
                        });
                        break;
                    case "Confirmed":
                        btnAction.setText("Mark Completed");
                        btnAction.setVisibility(View.VISIBLE);
                        btnSecondaryAction.setVisibility(View.GONE);
                        btnAction.setOnClickListener(v -> markAsCompleted(appointment));
                        break;
                    case "Completed":
                        btnAction.setVisibility(View.GONE);
                        btnSecondaryAction.setVisibility(View.GONE);
                        break;
                }
            }
        }
    }
}
