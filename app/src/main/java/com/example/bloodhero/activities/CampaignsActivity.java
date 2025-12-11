package com.example.bloodhero.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodhero.R;
import com.example.bloodhero.adapters.CampaignAdapter;
import com.example.bloodhero.models.Campaign;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CampaignsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText etSearch;
    private RecyclerView rvCampaigns;

    private CampaignAdapter campaignAdapter;
    private List<Campaign> allCampaigns;
    private List<Campaign> filteredCampaigns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campaigns);

        initViews();
        setupToolbar();
        loadCampaigns();
        setupSearch();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etSearch = findViewById(R.id.etSearch);
        rvCampaigns = findViewById(R.id.rvCampaigns);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadCampaigns() {
        allCampaigns = new ArrayList<>();

        // Morocco-based campaign data
        allCampaigns.add(new Campaign(
                "1",
                "Centre de Transfusion Sanguine",
                "CHU Ibn Sina",
                "Avenue Mohamed V, Rabat",
                "January 15, 2025",
                "9:00 AM - 5:00 PM",
                2.5,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"),
                "Rejoignez notre collecte mensuelle de sang! Aidez à sauver des vies."
        ));

        allCampaigns.add(new Campaign(
                "2",
                "Hôpital Cheikh Khalifa",
                "Fondation Cheikh Khalifa",
                "Route de Nouaceur, Casablanca",
                "January 18, 2025",
                "8:00 AM - 4:00 PM",
                5.0,
                Arrays.asList("O+", "O-", "A-"),
                "Besoin urgent de sang type O. Sans rendez-vous accepté!"
        ));

        allCampaigns.add(new Campaign(
                "3",
                "Université Mohammed V",
                "Faculté de Médecine",
                "Avenue Allal El Fassi, Rabat",
                "January 20, 2025",
                "10:00 AM - 6:00 PM",
                8.3,
                Arrays.asList("A+", "B+", "AB+", "O+"),
                "Collecte annuelle universitaire. Étudiants et personnel bienvenus!"
        ));

        allCampaigns.add(new Campaign(
                "4",
                "Centre Régional de Transfusion",
                "Ministère de la Santé",
                "Boulevard Zerktouni, Marrakech",
                "January 22, 2025",
                "9:00 AM - 3:00 PM",
                12.0,
                Arrays.asList("B-", "AB-", "O-"),
                "Journée nationale du don de sang. Tous les groupes sanguins sont les bienvenus!"
        ));

        allCampaigns.add(new Campaign(
                "5",
                "Clinique Internationale",
                "Groupe Akdital",
                "Avenue Hassan II, Tanger",
                "January 25, 2025",
                "11:00 AM - 7:00 PM",
                6.2,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-"),
                "Don de sang accepté lors de la foire de santé annuelle."
        ));

        allCampaigns.add(new Campaign(
                "6",
                "Hôpital Militaire",
                "Forces Armées Royales",
                "Avenue des FAR, Meknès",
                "January 27, 2025",
                "7:00 AM - 9:00 PM",
                3.8,
                Arrays.asList("O-", "O+"),
                "URGENT: Pénurie critique de sang. Donneurs de type O particulièrement nécessaires!"
        ));

        filteredCampaigns = new ArrayList<>(allCampaigns);

        campaignAdapter = new CampaignAdapter(filteredCampaigns, campaign -> {
            // Navigate to booking activity
            Intent intent = new Intent(this, BookingActivity.class);
            intent.putExtra("campaign_id", campaign.getId());
            intent.putExtra("campaign_name", campaign.getName());
            intent.putExtra("location", campaign.getLocation());
            startActivity(intent);
        });

        rvCampaigns.setLayoutManager(new LinearLayoutManager(this));
        rvCampaigns.setAdapter(campaignAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCampaigns(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterCampaigns(String query) {
        filteredCampaigns.clear();

        if (query.isEmpty()) {
            filteredCampaigns.addAll(allCampaigns);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Campaign campaign : allCampaigns) {
                if (campaign.getName().toLowerCase().contains(lowerQuery) ||
                        campaign.getOrganizer().toLowerCase().contains(lowerQuery) ||
                        campaign.getLocation().toLowerCase().contains(lowerQuery)) {
                    filteredCampaigns.add(campaign);
                }
            }
        }

        campaignAdapter.notifyDataSetChanged();
    }
}