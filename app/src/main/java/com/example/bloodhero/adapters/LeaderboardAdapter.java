package com.example.bloodhero.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodhero.R;
import com.example.bloodhero.models.LeaderboardEntry;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    private List<LeaderboardEntry> entries;

    public LeaderboardAdapter(List<LeaderboardEntry> entries) {
        this.entries = entries;
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        LeaderboardEntry entry = entries.get(position);
        holder.bind(entry);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public void updateEntries(List<LeaderboardEntry> newEntries) {
        this.entries = newEntries;
        notifyDataSetChanged();
    }

    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        private TextView tvRank;
        private CircleImageView ivProfile;
        private TextView tvUserName;
        private TextView tvPoints;
        private TextView tvDonations;
        private View itemContainer;

        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvPoints = itemView.findViewById(R.id.tvPoints);
            tvDonations = itemView.findViewById(R.id.tvDonations);
            itemContainer = itemView.findViewById(R.id.itemContainer);
        }

        public void bind(LeaderboardEntry entry) {
            tvRank.setText(entry.getRankDisplay());
            tvUserName.setText(entry.getUserName());
            tvPoints.setText(entry.getTotalPoints() + " pts");
            tvDonations.setText(entry.getTotalDonations() + " donations");

            // Highlight current user
            if (entry.isCurrentUser()) {
                itemContainer.setBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.surface_tint));
            } else {
                itemContainer.setBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));
            }

            // Special styling for top 3
            if (entry.getRank() <= 3) {
                tvRank.setTextSize(20);
            } else {
                tvRank.setTextSize(16);
            }
        }
    }
}