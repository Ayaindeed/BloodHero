package com.example.bloodhero.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodhero.R;
import com.example.bloodhero.models.BloodRequest;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class BloodRequestAdapter extends RecyclerView.Adapter<BloodRequestAdapter.RequestViewHolder> {

    private List<BloodRequest> requests;
    private OnRequestClickListener listener;
    private String userBloodType;

    public interface OnRequestClickListener {
        void onRespondClick(BloodRequest request);
        void onCallClick(BloodRequest request);
    }

    public BloodRequestAdapter(List<BloodRequest> requests, String userBloodType, OnRequestClickListener listener) {
        this.requests = requests;
        this.userBloodType = userBloodType;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_blood_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        BloodRequest request = requests.get(position);
        holder.bind(request);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        private CardView cardRequest;
        private TextView tvBloodType, tvUrgencyBadge, tvPatientName;
        private TextView tvHospital, tvCity, tvUnitsNeeded, tvPostedTime;
        private TextView tvCompatibleBadge;
        private MaterialButton btnRespond, btnCall;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRequest = itemView.findViewById(R.id.cardRequest);
            tvBloodType = itemView.findViewById(R.id.tvBloodType);
            tvUrgencyBadge = itemView.findViewById(R.id.tvUrgencyBadge);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvHospital = itemView.findViewById(R.id.tvHospital);
            tvCity = itemView.findViewById(R.id.tvCity);
            tvUnitsNeeded = itemView.findViewById(R.id.tvUnitsNeeded);
            tvPostedTime = itemView.findViewById(R.id.tvPostedTime);
            tvCompatibleBadge = itemView.findViewById(R.id.tvCompatibleBadge);
            btnRespond = itemView.findViewById(R.id.btnRespond);
            btnCall = itemView.findViewById(R.id.btnCall);
        }

        public void bind(BloodRequest request) {
            tvBloodType.setText(request.getBloodType());
            tvPatientName.setText("Patient: " + request.getPatientName());
            tvHospital.setText(request.getHospital());
            tvCity.setText(request.getCity());
            tvUnitsNeeded.setText(request.getUnitsNeeded() + " units needed");
            tvPostedTime.setText(request.getPostedTime());

            // Set urgency badge
            switch (request.getUrgencyLevel()) {
                case "critical":
                    tvUrgencyBadge.setText("CRITICAL");
                    tvUrgencyBadge.setBackgroundColor(Color.parseColor("#D32F2F"));
                    break;
                case "urgent":
                    tvUrgencyBadge.setText("URGENT");
                    tvUrgencyBadge.setBackgroundColor(Color.parseColor("#F57C00"));
                    break;
                default:
                    tvUrgencyBadge.setText("NEEDED");
                    tvUrgencyBadge.setBackgroundColor(Color.parseColor("#1976D2"));
                    break;
            }

            // Check compatibility
            boolean isCompatible = checkCompatibility(userBloodType, request.getBloodType());
            if (isCompatible) {
                tvCompatibleBadge.setVisibility(View.VISIBLE);
                tvCompatibleBadge.setText("You can donate!");
                cardRequest.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
            } else {
                tvCompatibleBadge.setVisibility(View.GONE);
                cardRequest.setCardBackgroundColor(Color.WHITE);
            }

            btnRespond.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRespondClick(request);
                }
            });

            btnCall.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCallClick(request);
                }
            });
        }

        private boolean checkCompatibility(String donorType, String recipientType) {
            if (donorType == null || donorType.isEmpty()) return false;
            
            // O- can donate to anyone
            if (donorType.equals("O-")) return true;
            
            // O+ can donate to all positive
            if (donorType.equals("O+")) {
                return recipientType.endsWith("+");
            }
            
            // A- can donate to A+, A-, AB+, AB-
            if (donorType.equals("A-")) {
                return recipientType.startsWith("A") || recipientType.startsWith("AB");
            }
            
            // A+ can donate to A+, AB+
            if (donorType.equals("A+")) {
                return recipientType.equals("A+") || recipientType.equals("AB+");
            }
            
            // B- can donate to B+, B-, AB+, AB-
            if (donorType.equals("B-")) {
                return recipientType.startsWith("B") || recipientType.startsWith("AB");
            }
            
            // B+ can donate to B+, AB+
            if (donorType.equals("B+")) {
                return recipientType.equals("B+") || recipientType.equals("AB+");
            }
            
            // AB- can donate to AB+, AB-
            if (donorType.equals("AB-")) {
                return recipientType.startsWith("AB");
            }
            
            // AB+ can only donate to AB+
            if (donorType.equals("AB+")) {
                return recipientType.equals("AB+");
            }
            
            return false;
        }
    }
}
