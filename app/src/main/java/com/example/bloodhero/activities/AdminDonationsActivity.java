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
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AdminDonationsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView rvDonations;
    private View emptyState;
    
    private DonationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_donations);

        initViews();
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
        // Mock data - appointments that need to be marked as donated
        List<PendingDonation> donations = new ArrayList<>();
        donations.add(new PendingDonation("1", "Mohammed Alaoui", "B+", "Red Crescent Center", 
                "Dec 19, 2024", "Confirmed"));
        donations.add(new PendingDonation("2", "Ahmed El Fassi", "A+", "Blood Drive - Casablanca", 
                "Dec 20, 2024", "Confirmed"));
        donations.add(new PendingDonation("3", "Fatima Benali", "O-", "Hospital Ibn Sina", 
                "Dec 20, 2024", "Confirmed"));

        adapter.setDonations(donations);
        emptyState.setVisibility(donations.isEmpty() ? View.VISIBLE : View.GONE);
        rvDonations.setVisibility(donations.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void markDonationComplete(PendingDonation donation) {
        new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("Confirm Donation")
                .setMessage("Mark " + donation.donorName + "'s donation as completed?\n\nThis will:\n• Award 50 points\n• Update donation count\n• Unlock eligible badges")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    Toast.makeText(this, "Donation recorded! " + donation.donorName + " earned 50 points", Toast.LENGTH_LONG).show();
                    // In real app, update database and trigger badge check
                    loadPendingDonations(); // Refresh
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Data class
    private static class PendingDonation {
        String id, donorName, bloodType, location, date, status;

        PendingDonation(String id, String donorName, String bloodType, String location, String date, String status) {
            this.id = id;
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
