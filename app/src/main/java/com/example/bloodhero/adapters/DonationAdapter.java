package com.example.bloodhero.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodhero.R;
import com.example.bloodhero.models.Donation;

import java.util.List;

public class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.DonationViewHolder> {

    private List<Donation> donations;
    private OnDonationClickListener listener;

    public interface OnDonationClickListener {
        void onDonationClick(Donation donation);
    }

    public DonationAdapter(List<Donation> donations, OnDonationClickListener listener) {
        this.donations = donations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DonationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_donation, parent, false);
        return new DonationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DonationViewHolder holder, int position) {
        Donation donation = donations.get(position);
        holder.bind(donation, listener);
    }

    @Override
    public int getItemCount() {
        return donations.size();
    }

    public void updateDonations(List<Donation> newDonations) {
        this.donations = newDonations;
        notifyDataSetChanged();
    }

    static class DonationViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivDonationType;
        private TextView tvDonationDate;
        private TextView tvDonationLocation;
        private TextView tvBloodType;
        private TextView tvPoints;

        public DonationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDonationType = itemView.findViewById(R.id.ivDonationType);
            tvDonationDate = itemView.findViewById(R.id.tvDonationDate);
            tvDonationLocation = itemView.findViewById(R.id.tvDonationLocation);
            tvBloodType = itemView.findViewById(R.id.tvBloodType);
            tvPoints = itemView.findViewById(R.id.tvPoints);
        }

        public void bind(Donation donation, OnDonationClickListener listener) {
            tvDonationDate.setText(donation.getDate());
            tvDonationLocation.setText(donation.getLocation());
            tvBloodType.setText(donation.getBloodType());
            tvPoints.setText("+" + donation.getPointsEarned() + " pts");

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDonationClick(donation);
                }
            });
        }
    }
}