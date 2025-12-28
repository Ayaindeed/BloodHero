package com.example.bloodhero.activities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodhero.utils.EnhancedDialogHelper;import com.example.bloodhero.R;
import com.example.bloodhero.database.BloodHeroDatabaseHelper;
import com.example.bloodhero.models.User;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AdminBadgesActivity extends AppCompatActivity {

    private BloodHeroDatabaseHelper dbHelper;

    private ImageButton btnBack;
    private RecyclerView rvBadges, rvUsers;
    
    private BadgeAdapter badgeAdapter;
    private UserBadgeAdapter userAdapter;
    private Badge selectedBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_badges);

        dbHelper = BloodHeroDatabaseHelper.getInstance(this);
        initViews();
        loadBadges();
        loadUsers();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvBadges = findViewById(R.id.rvBadges);
        rvUsers = findViewById(R.id.rvUsers);

        btnBack.setOnClickListener(v -> finish());

        rvBadges.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        badgeAdapter = new BadgeAdapter();
        rvBadges.setAdapter(badgeAdapter);

        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserBadgeAdapter();
        rvUsers.setAdapter(userAdapter);
    }

    private void loadBadges() {
        List<Badge> badges = new ArrayList<>();
        badges.add(new Badge("1", "First Drop", "Complete your first donation", R.drawable.ic_badge, "#FFD700"));
        badges.add(new Badge("2", "Regular Hero", "Donate 5 times", R.drawable.ic_badge, "#C0C0C0"));
        badges.add(new Badge("3", "Life Saver", "Donate 10 times", R.drawable.ic_badge, "#CD7F32"));
        badges.add(new Badge("4", "Blood Champion", "Donate 25 times", R.drawable.ic_badge, "#FF4444"));
        badges.add(new Badge("5", "Super Donor", "Donate 50 times", R.drawable.ic_badge, "#9C27B0"));

        badgeAdapter.setBadges(badges);
        if (!badges.isEmpty()) {
            selectedBadge = badges.get(0);
        }
    }

    private void loadUsers() {
        List<UserBadge> users = new ArrayList<>();
        
        // Load real users from SQLite database
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, name, email, blood_type, total_donations FROM users WHERE email != ? ORDER BY total_donations DESC", 
                                   new String[]{"admin@contact.me"});
        
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(0);
                String name = cursor.getString(1);
                String bloodType = cursor.getString(3);
                int donations = cursor.getInt(4);
                boolean hasBadge = donations >= 1; // Has at least first donation badge
                
                users.add(new UserBadge(id, name, bloodType, donations, hasBadge));
            } while (cursor.moveToNext());
        }
        cursor.close();

        userAdapter.setUsers(users);
    }

    private void awardBadge(UserBadge user) {
        if (selectedBadge == null) {
            Toast.makeText(this, "Please select a badge first", Toast.LENGTH_SHORT).show();
            return;
        }

        EnhancedDialogHelper.showConfirmationDialog(
                this,
                "Award Badge",
                "Award \"" + selectedBadge.name + "\" badge to " + user.name + "?",
                "Award",
                "Cancel",
                () -> Toast.makeText(AdminBadgesActivity.this, "Badge awarded to " + user.name + "!", Toast.LENGTH_SHORT).show()
        );
    }

    // Data classes
    private static class Badge {
        String id, name, description;
        int iconRes;
        String color;

        Badge(String id, String name, String description, int iconRes, String color) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.iconRes = iconRes;
            this.color = color;
        }
    }

    private static class UserBadge {
        String id, name, bloodType;
        int donations;
        boolean hasBadge;

        UserBadge(String id, String name, String bloodType, int donations, boolean hasBadge) {
            this.id = id;
            this.name = name;
            this.bloodType = bloodType;
            this.donations = donations;
            this.hasBadge = hasBadge;
        }
    }

    // Badge Adapter
    private class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.ViewHolder> {
        private List<Badge> badges = new ArrayList<>();
        private int selectedPosition = 0;

        void setBadges(List<Badge> badges) {
            this.badges = badges;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_badge_select, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(badges.get(position), position == selectedPosition);
        }

        @Override
        public int getItemCount() {
            return badges.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivBadge;
            TextView tvName;
            View cardView;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = itemView;
                ivBadge = itemView.findViewById(R.id.ivBadge);
                tvName = itemView.findViewById(R.id.tvName);
            }

            void bind(Badge badge, boolean isSelected) {
                tvName.setText(badge.name);
                ivBadge.setImageResource(badge.iconRes);
                
                cardView.setAlpha(isSelected ? 1f : 0.6f);
                cardView.setScaleX(isSelected ? 1.1f : 1f);
                cardView.setScaleY(isSelected ? 1.1f : 1f);

                itemView.setOnClickListener(v -> {
                    int oldPos = selectedPosition;
                    selectedPosition = getAdapterPosition();
                    selectedBadge = badge;
                    notifyItemChanged(oldPos);
                    notifyItemChanged(selectedPosition);
                });
            }
        }
    }

    // User Badge Adapter
    private class UserBadgeAdapter extends RecyclerView.Adapter<UserBadgeAdapter.ViewHolder> {
        private List<UserBadge> users = new ArrayList<>();

        void setUsers(List<UserBadge> users) {
            this.users = users;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_badge, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(users.get(position));
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvBloodType, tvDonations, tvBadgeStatus;
            MaterialButton btnAward;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvBloodType = itemView.findViewById(R.id.tvBloodType);
                tvDonations = itemView.findViewById(R.id.tvDonations);
                tvBadgeStatus = itemView.findViewById(R.id.tvBadgeStatus);
                btnAward = itemView.findViewById(R.id.btnAward);
            }

            void bind(UserBadge user) {
                tvName.setText(user.name);
                tvBloodType.setText(user.bloodType);
                tvDonations.setText(user.donations + " donations");
                
                if (user.hasBadge) {
                    tvBadgeStatus.setVisibility(View.VISIBLE);
                    tvBadgeStatus.setText("Has badge");
                    btnAward.setVisibility(View.GONE);
                } else {
                    tvBadgeStatus.setVisibility(View.GONE);
                    btnAward.setVisibility(View.VISIBLE);
                    btnAward.setOnClickListener(v -> awardBadge(user));
                }
            }
        }
    }
}
