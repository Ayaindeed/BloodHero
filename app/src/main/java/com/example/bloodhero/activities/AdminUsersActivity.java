package com.example.bloodhero.activities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.example.bloodhero.database.BloodHeroDatabaseHelper;
import com.example.bloodhero.models.User;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersActivity extends AppCompatActivity {
    
    private ImageButton btnBack;
    private RecyclerView rvUsers;
    private TabLayout tabLayout;
    private TextView tvUserCount;
    
    private UserAdapter adapter;
    private List<User> allUsers;
    private BloodHeroDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        dbHelper = BloodHeroDatabaseHelper.getInstance(this);

        initViews();
        loadUsers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload users when returning to activity
        loadUsers();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvUsers = findViewById(R.id.rvUsers);
        tabLayout = findViewById(R.id.tabLayout);
        tvUserCount = findViewById(R.id.tvUserCount);

        btnBack.setOnClickListener(v -> finish());

        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter();
        rvUsers.setAdapter(adapter);

        setupTabs();
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("All"));
        tabLayout.addTab(tabLayout.newTab().setText("Active"));
        tabLayout.addTab(tabLayout.newTab().setText("New"));
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterUsers(tab.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadUsers() {
        allUsers = new ArrayList<>();
        
        // Load real users from SQLite
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users ORDER BY created_at DESC", null);
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                User user = new User();
                user.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                user.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                user.setBloodType(cursor.getString(cursor.getColumnIndexOrThrow("blood_type")));
                user.setLocation(cursor.getString(cursor.getColumnIndexOrThrow("location")));
                user.setPhoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
                user.setTotalDonations(cursor.getInt(cursor.getColumnIndexOrThrow("total_donations")));
                user.setTotalPoints(cursor.getInt(cursor.getColumnIndexOrThrow("total_points")));
                user.setProfileImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("profile_image_url")));
                user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
                
                allUsers.add(user);
            }
            cursor.close();
        }

        adapter.setUsers(allUsers);
        tvUserCount.setText(String.valueOf(allUsers.size()));
    }

    private void filterUsers(String filter) {
        List<User> filtered = new ArrayList<>();
        for (User user : allUsers) {
            if (filter.equals("All") || 
                (filter.equals("Active") && user.getTotalDonations() > 0) ||
                (filter.equals("New") && user.getTotalDonations() == 0)) {
                filtered.add(user);
            }
        }
        adapter.setUsers(filtered);
        tvUserCount.setText(String.valueOf(filtered.size()));
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
                tvName.setText(user.getName() != null ? user.getName() : "Unknown");
                tvEmail.setText(user.getEmail() != null ? user.getEmail() : "No email");
                tvBloodType.setText(user.getBloodType() != null ? user.getBloodType() : "--");
                tvDonations.setText(user.getTotalDonations() + " donations");
                tvVerified.setVisibility(View.GONE); // Remove verified badge
            }
        }
    }
}
