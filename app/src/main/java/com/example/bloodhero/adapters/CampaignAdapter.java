package com.example.bloodhero.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodhero.R;
import com.example.bloodhero.models.Campaign;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class CampaignAdapter extends RecyclerView.Adapter<CampaignAdapter.CampaignViewHolder> {

    private List<Campaign> campaigns;
    private OnCampaignClickListener listener;

    public interface OnCampaignClickListener {
        void onCampaignClick(Campaign campaign);
    }

    public CampaignAdapter(List<Campaign> campaigns, OnCampaignClickListener listener) {
        this.campaigns = campaigns;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CampaignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_campaign_card, parent, false);
        return new CampaignViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CampaignViewHolder holder, int position) {
        Campaign campaign = campaigns.get(position);
        holder.bind(campaign, listener);
    }

    @Override
    public int getItemCount() {
        return campaigns.size();
    }

    public void updateCampaigns(List<Campaign> newCampaigns) {
        this.campaigns = newCampaigns;
        notifyDataSetChanged();
    }

    static class CampaignViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCampaignName;
        private TextView tvCampaignOrganizer;
        private TextView tvDistance;
        private TextView tvCampaignDate;
        private TextView tvCampaignTime;
        private TextView tvCampaignLocation;
        private TextView tvBloodTypesNeeded;
        private MaterialButton btnBookCampaign;

        public CampaignViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCampaignName = itemView.findViewById(R.id.tvCampaignName);
            tvCampaignOrganizer = itemView.findViewById(R.id.tvCampaignOrganizer);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvCampaignDate = itemView.findViewById(R.id.tvCampaignDate);
            tvCampaignTime = itemView.findViewById(R.id.tvCampaignTime);
            tvCampaignLocation = itemView.findViewById(R.id.tvCampaignLocation);
            tvBloodTypesNeeded = itemView.findViewById(R.id.tvBloodTypesNeeded);
            btnBookCampaign = itemView.findViewById(R.id.btnBookCampaign);
        }

        public void bind(Campaign campaign, OnCampaignClickListener listener) {
            tvCampaignName.setText(campaign.getName());
            tvCampaignOrganizer.setText(campaign.getOrganizer());
            tvDistance.setText(campaign.getDistanceString());
            tvCampaignDate.setText(campaign.getDate());
            tvCampaignTime.setText(campaign.getTime());
            tvCampaignLocation.setText(campaign.getLocation());
            tvBloodTypesNeeded.setText(campaign.getBloodTypesString());

            btnBookCampaign.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCampaignClick(campaign);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCampaignClick(campaign);
                }
            });
        }
    }
}