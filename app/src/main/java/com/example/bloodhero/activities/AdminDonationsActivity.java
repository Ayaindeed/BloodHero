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
        
        // Load actual donations from donations table
        List<Donation> completedDonations = donationRepository.getAllDonations();
        
        for (Donation don : completedDonations) {
            // Get the user from SQLite database
            User user = userRepository.getUserById(don.getUserId());
            String donorName = "Unknown";
            String bloodType = "Unknown";
            
            if (user != null) {
                donorName = user.getName() != null ? user.getName() : "Unknown";
                bloodType = user.getBloodType() != null ? user.getBloodType() : "Unknown";
            }
            
            donations.add(new PendingDonation(
                don.getId(),
                don.getUserId(),
                donorName,
                bloodType,
                don.getLocation(),
                don.getDate(),
                don.getStatus()
            ));
        }

        adapter.setDonations(donations);
        emptyState.setVisibility(donations.isEmpty() ? View.VISIBLE : View.GONE);
        rvDonations.setVisibility(donations.isEmpty() ? View.GONE : View.VISIBLE);
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

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDonorName = itemView.findViewById(R.id.tvDonorName);
                tvBloodType = itemView.findViewById(R.id.tvBloodType);
                tvLocation = itemView.findViewById(R.id.tvLocation);
                tvDate = itemView.findViewById(R.id.tvDate);
                // Remove btnMarkComplete - donations are automatic when donor enters verification code
            }

            void bind(PendingDonation donation) {
                tvDonorName.setText(donation.donorName);
                tvBloodType.setText(donation.bloodType);
                tvLocation.setText(donation.location);
                tvDate.setText(donation.date);
                // No click listener needed - just display donation records
            }
        }
    }
}
