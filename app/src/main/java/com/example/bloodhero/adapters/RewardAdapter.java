package com.example.bloodhero.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodhero.R;
import com.example.bloodhero.models.Reward;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.RewardViewHolder> {

    private List<Reward> rewards;
    private OnRewardClickListener listener;
    private int userPoints;

    public interface OnRewardClickListener {
        void onRedeemClick(Reward reward, int position);
    }

    public RewardAdapter(List<Reward> rewards, int userPoints, OnRewardClickListener listener) {
        this.rewards = rewards;
        this.userPoints = userPoints;
        this.listener = listener;
    }

    public void setUserPoints(int points) {
        this.userPoints = points;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reward, parent, false);
        return new RewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
        Reward reward = rewards.get(position);
        holder.bind(reward, position);
    }

    @Override
    public int getItemCount() {
        return rewards.size();
    }

    class RewardViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivRewardIcon;
        private TextView tvRewardName, tvRewardDescription, tvPartnerName, tvPointsCost;
        private MaterialButton btnRedeem;
        private View redeemOverlay;

        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRewardIcon = itemView.findViewById(R.id.ivRewardIcon);
            tvRewardName = itemView.findViewById(R.id.tvRewardName);
            tvRewardDescription = itemView.findViewById(R.id.tvRewardDescription);
            tvPartnerName = itemView.findViewById(R.id.tvPartnerName);
            tvPointsCost = itemView.findViewById(R.id.tvPointsCost);
            btnRedeem = itemView.findViewById(R.id.btnRedeem);
            redeemOverlay = itemView.findViewById(R.id.redeemOverlay);
        }

        public void bind(Reward reward, int position) {
            ivRewardIcon.setImageResource(reward.getIconResId());
            tvRewardName.setText(reward.getName());
            tvRewardDescription.setText(reward.getDescription());
            tvPartnerName.setText(reward.getPartnerName());
            tvPointsCost.setText(reward.getPointsCost() + " pts");

            if (reward.isRedeemed()) {
                btnRedeem.setEnabled(false);
                btnRedeem.setText("Redeemed");
                btnRedeem.setAlpha(0.5f);
                if (redeemOverlay != null) {
                    redeemOverlay.setVisibility(View.VISIBLE);
                }
            } else if (userPoints < reward.getPointsCost()) {
                btnRedeem.setEnabled(false);
                btnRedeem.setText("Not enough points");
                btnRedeem.setAlpha(0.7f);
                if (redeemOverlay != null) {
                    redeemOverlay.setVisibility(View.GONE);
                }
            } else {
                btnRedeem.setEnabled(true);
                btnRedeem.setText("Redeem");
                btnRedeem.setAlpha(1f);
                if (redeemOverlay != null) {
                    redeemOverlay.setVisibility(View.GONE);
                }
                btnRedeem.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRedeemClick(reward, position);
                    }
                });
            }
        }
    }
}
