package com.example.bloodhero.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodhero.R;
import com.example.bloodhero.models.Badge;

import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    private List<Badge> badges;
    private OnBadgeClickListener listener;

    public interface OnBadgeClickListener {
        void onBadgeClick(Badge badge);
    }

    public BadgeAdapter(List<Badge> badges, OnBadgeClickListener listener) {
        this.badges = badges;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        Badge badge = badges.get(position);
        holder.bind(badge, listener);
    }

    @Override
    public int getItemCount() {
        return badges.size();
    }

    public void updateBadges(List<Badge> newBadges) {
        this.badges = newBadges;
        notifyDataSetChanged();
    }

    static class BadgeViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivBadgeIcon;
        private TextView tvBadgeName;
        private TextView tvBadgeDescription;
        private View badgeContainer;

        public BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBadgeIcon = itemView.findViewById(R.id.ivBadgeIcon);
            tvBadgeName = itemView.findViewById(R.id.tvBadgeName);
            tvBadgeDescription = itemView.findViewById(R.id.tvBadgeDescription);
            badgeContainer = itemView.findViewById(R.id.badgeContainer);
        }

        public void bind(Badge badge, OnBadgeClickListener listener) {
            tvBadgeName.setText(badge.getName());
            tvBadgeDescription.setText(badge.getDescription());
            
            if (badge.getIconResId() != 0) {
                ivBadgeIcon.setImageResource(badge.getIconResId());
            }

            // Style based on unlock status
            if (badge.isUnlocked()) {
                itemView.setAlpha(1.0f);
                ivBadgeIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.primary));
            } else {
                itemView.setAlpha(0.5f);
                ivBadgeIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.badge_locked));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBadgeClick(badge);
                }
            });
        }
    }
}