package com.example.bloodhero.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminDonationsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView rvDonations;
    private View emptyState;
    
    private DonationAdapter adapter;
    private UserRepository userRepository;
    private AppointmentRepository appointmentRepository;
    private DonationRepository donationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_donations);

        userRepository = UserRepository.getInstance(this);
        appointmentRepository = AppointmentRepository.getInstance(this);
        donationRepository = DonationRepository.getInstance(this);
        
        initViews();
        loadPendingDonations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload donations when returning to activity
        loadPendingDonations();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvDonations = findViewById(R.id.rvDonations);
        emptyState = findViewById(R.id.emptyState);

        btnBack.setOnClickListener(v -> finish());

        rvDonations.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DonationAdapter();
        rvDonations.setAdapter(adapter);
    }

    private void loadPendingDonations() {
        List<PendingDonation> donations = new ArrayList<>();
        
        // Load COMPLETED status appointments (admin-confirmed, awaiting final completion)
        List<Appointment> completedAppointments = appointmentRepository.getAppointmentsByStatus("COMPLETED");
        
        for (Appointment appt : completedAppointments) {
            // Get the user from SQLite database
            User user = userRepository.getUserById(appt.getUserId());
            String donorName = "Unknown";
            String bloodType = "Unknown";
            
            if (user != null) {
                donorName = user.getName() != null ? user.getName() : "Unknown";
                bloodType = user.getBloodType() != null ? user.getBloodType() : "Unknown";
            }
            
            donations.add(new PendingDonation(
                appt.getId(),
                user != null ? user.getId() : "",
                donorName,
                bloodType,
                appt.getLocation(),
                appt.getDate(),
                appt.getStatus().toString()
            ));
        }

        adapter.setDonations(donations);
        emptyState.setVisibility(donations.isEmpty() ? View.VISIBLE : View.GONE);
        rvDonations.setVisibility(donations.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void markDonationComplete(PendingDonation donation) {
        new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("Confirm Donation")
                .setMessage("Mark " + donation.donorName + "'s donation as completed?\n\nThis will:\n• Award 50 points\n• Update donation count\n• Unlock eligible badges")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    // Get user to update their donation count
                    User user = userRepository.getUserById(donation.userId);
                    if (user != null) {
                        // Create and save donation record
                        Donation completedDonation = new Donation(
                                UUID.randomUUID().toString(),
                                user.getId(),
                                "",
                                donation.location,
                                donation.location,
                                donation.date,
                                user.getBloodType(),
                                50,
                                "COMPLETED"
                        );
                        donationRepository.saveDonation(completedDonation);
                        
                        // Update user points and donation count
                        userRepository.incrementDonations(user.getId(), 50);
                    }
                    
                    Toast.makeText(this, "Donation recorded! " + donation.donorName + " earned 50 points", Toast.LENGTH_LONG).show();
                    loadPendingDonations(); // Refresh
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Data class
    private static class PendingDonation {
        String id, userId, donorName, bloodType, location, date, status;

        PendingDonation(String id, String userId, String donorName, String bloodType, String location, String date, String status) {
            this.id = id;
            this.userId = userId;
            this.donorName = donorName;
            this.bloodType = bloodType;
            this.location = location;
            this.date = date;
            this.status = status;
        }
    }

    // Adapter
    private class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.ViewHolder> {
        private List<PendingDonation> donations = new ArrayList<>();

        void setDonations(List<PendingDonation> donations) {
            this.donations = donations;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_pending_donation, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(donations.get(position));
        }

        @Override
        public int getItemCount() {
            return donations.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDonorName, tvBloodType, tvLocation, tvDate;
            MaterialButton btnMarkComplete;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDonorName = itemView.findViewById(R.id.tvDonorName);
                tvBloodType = itemView.findViewById(R.id.tvBloodType);
                tvLocation = itemView.findViewById(R.id.tvLocation);
                tvDate = itemView.findViewById(R.id.tvDate);
                btnMarkComplete = itemView.findViewById(R.id.btnMarkComplete);
            }

            void bind(PendingDonation donation) {
                tvDonorName.setText(donation.donorName);
                tvBloodType.setText(donation.bloodType);
                tvLocation.setText(donation.location);
                tvDate.setText(donation.date);
                btnMarkComplete.setOnClickListener(v -> markDonationComplete(donation));
            }
        }
    }
}
