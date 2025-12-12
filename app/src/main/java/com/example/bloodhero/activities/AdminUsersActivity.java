package com.example.bloodhero.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodhero.R;
import com.example.bloodhero.utils.UserStorage;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BloodHeroPrefs";
    
    private ImageButton btnBack;
    private RecyclerView rvUsers;
    private ChipGroup chipGroup;
    private TextView tvUserCount;
    
    private UserAdapter adapter;
    private List<User> allUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        initViews();
        loadUsers();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvUsers = findViewById(R.id.rvUsers);
        chipGroup = findViewById(R.id.chipGroup);
        tvUserCount = findViewById(R.id.tvUserCount);

        btnBack.setOnClickListener(v -> finish());

        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter();
        rvUsers.setAdapter(adapter);

        setupFilters();
    }

    private void setupFilters() {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                adapter.setUsers(allUsers);
            } else {
                Chip chip = group.findViewById(checkedIds.get(0));
                String filter = chip.getText().toString();
                filterUsers(filter);
            }
        });
    }

    private void loadUsers() {
        allUsers = new ArrayList<>();
        
        // Add admin account
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String adminName = prefs.getString("user_name", "Admin");
        String adminEmail = prefs.getString("user_email", "admin@contact.me");
        allUsers.add(new User("admin", adminName, adminEmail, "Admin", 0, true));
        
        // Load real users from UserStorage
        List<UserStorage.UserData> registeredUsers = UserStorage.getAllUsers(this);
        for (UserStorage.UserData userData : registeredUsers) {
            // Skip if it's the admin email
            if (!userData.email.equals(adminEmail)) {
                allUsers.add(new User(
                    userData.id,
                    userData.name,
                    userData.email,
                    userData.bloodType,
                    userData.donations,
                    userData.verified
                ));
            }
        }

        adapter.setUsers(allUsers);
        tvUserCount.setText(allUsers.size() + " users");
    }

    private void filterUsers(String filter) {
        List<User> filtered = new ArrayList<>();
        for (User user : allUsers) {
            if (filter.equals("All") || 
                (filter.equals("Active Donors") && user.totalDonations > 0) ||
                (filter.equals("New Users") && user.totalDonations == 0) ||
                filter.equals(user.bloodType)) {
                filtered.add(user);
            }
        }
        adapter.setUsers(filtered);
        tvUserCount.setText(filtered.size() + " users");
    }

    // User data class
    private static class User {
        String id, name, email, bloodType;
        int totalDonations;
        boolean isVerified;

        User(String id, String name, String email, String bloodType, int totalDonations, boolean isVerified) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.bloodType = bloodType;
            this.totalDonations = totalDonations;
            this.isVerified = isVerified;
        }
    }

    // Adapter
    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
        private List<User> users = new ArrayList<>();

        void setUsers(List<User> users) {
            this.users = users;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_user, parent, false);
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
            TextView tvName, tvEmail, tvBloodType, tvDonations, tvVerified;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvEmail = itemView.findViewById(R.id.tvEmail);
                tvBloodType = itemView.findViewById(R.id.tvBloodType);
                tvDonations = itemView.findViewById(R.id.tvDonations);
                tvVerified = itemView.findViewById(R.id.tvVerified);
            }

            void bind(User user) {
                tvName.setText(user.name);
                tvEmail.setText(user.email);
                tvBloodType.setText(user.bloodType);
                tvDonations.setText(user.totalDonations + " donations");
                tvVerified.setVisibility(user.isVerified ? View.VISIBLE : View.GONE);
            }
        }
    }
}
