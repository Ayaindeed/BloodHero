package com.example.bloodhero.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodhero.R;
import com.example.bloodhero.adapters.CampaignAdapter;
import com.example.bloodhero.models.Campaign;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CampaignsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BloodHeroPrefs";

    private Toolbar toolbar;
    private EditText etSearch;
    private RecyclerView rvCampaigns;
    private TextView tvLocationHeader;
    private android.widget.LinearLayout btnMapView;

    private CampaignAdapter campaignAdapter;
    private List<Campaign> allCampaigns;
    private List<Campaign> nearbyCampaigns;
    private List<Campaign> filteredCampaigns;
    private String userCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campaigns);

        initViews();
        setupToolbar();
        setupMapButton();
        loadUserLocation();
        loadAllCampaigns();
        filterCampaignsByLocation();
        setupSearch();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etSearch = findViewById(R.id.etSearch);
        rvCampaigns = findViewById(R.id.rvCampaigns);
        tvLocationHeader = findViewById(R.id.tvLocationHeader);
        btnMapView = findViewById(R.id.btnMapView);
    }
    
    private void setupMapButton() {
        btnMapView.setOnClickListener(v -> {
            startActivity(new Intent(this, CampaignMapActivity.class));
        });
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadUserLocation() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userCity = prefs.getString("user_location", "").toLowerCase();
        
        // Extract city name from location string
        if (userCity.contains("casablanca") || userCity.contains("casa")) {
            userCity = "casablanca";
        } else if (userCity.contains("rabat")) {
            userCity = "rabat";
        } else if (userCity.contains("marrakech") || userCity.contains("marrakesh")) {
            userCity = "marrakech";
        } else if (userCity.contains("tanger") || userCity.contains("tangier")) {
            userCity = "tanger";
        } else if (userCity.contains("fes") || userCity.contains("fez") || userCity.contains("fès")) {
            userCity = "fes";
        } else if (userCity.contains("meknes") || userCity.contains("meknès")) {
            userCity = "meknes";
        } else if (userCity.contains("agadir")) {
            userCity = "agadir";
        } else if (userCity.contains("oujda")) {
            userCity = "oujda";
        } else {
            userCity = "casablanca"; // Default to Casablanca
        }
    }

    private void loadAllCampaigns() {
        allCampaigns = new ArrayList<>();

        // CASABLANCA Campaigns
        allCampaigns.add(new Campaign("c1", "Hôpital Cheikh Khalifa", "Fondation Cheikh Khalifa",
                "Route de Nouaceur, Casablanca", "January 18, 2025", "8:00 AM - 4:00 PM", 2.5,
                Arrays.asList("O+", "O-", "A-"), "Besoin urgent de sang type O. Sans rendez-vous accepté!"));
        
        allCampaigns.add(new Campaign("c2", "CHU Ibn Rochd", "Ministère de la Santé",
                "Quartier des Hôpitaux, Casablanca", "January 20, 2025", "9:00 AM - 5:00 PM", 3.2,
                Arrays.asList("A+", "B+", "AB+", "O+"), "Collecte hebdomadaire - tous les groupes bienvenus."));
        
        allCampaigns.add(new Campaign("c3", "Morocco Mall", "Croissant Rouge Marocain",
                "Morocco Mall, Casablanca", "January 22, 2025", "10:00 AM - 8:00 PM", 5.0,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"), "Journée de don de sang au centre commercial."));
        
        allCampaigns.add(new Campaign("c4", "Clinique Badr", "Groupe Akdital",
                "Boulevard Zerktouni, Casablanca", "January 25, 2025", "8:00 AM - 3:00 PM", 4.1,
                Arrays.asList("B-", "O-"), "URGENT: Besoin de groupes négatifs."));

        // RABAT Campaigns
        allCampaigns.add(new Campaign("r1", "Centre de Transfusion Sanguine", "CHU Ibn Sina",
                "Avenue Mohamed V, Rabat", "January 15, 2025", "9:00 AM - 5:00 PM", 1.5,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"), "Collecte mensuelle de sang."));
        
        allCampaigns.add(new Campaign("r2", "Université Mohammed V", "Faculté de Médecine",
                "Avenue Allal El Fassi, Rabat", "January 20, 2025", "10:00 AM - 6:00 PM", 3.8,
                Arrays.asList("A+", "B+", "AB+", "O+"), "Collecte annuelle universitaire."));
        
        allCampaigns.add(new Campaign("r3", "Hôpital Militaire", "Forces Armées Royales",
                "Avenue des FAR, Rabat", "January 23, 2025", "7:00 AM - 4:00 PM", 2.2,
                Arrays.asList("O-", "O+", "A-"), "Don de sang pour les forces armées."));
        
        allCampaigns.add(new Campaign("r4", "Mega Mall Rabat", "Association des Donneurs",
                "Route de Témara, Rabat", "January 27, 2025", "11:00 AM - 7:00 PM", 6.5,
                Arrays.asList("A+", "B+", "O+"), "Campagne de sensibilisation au don."));

        // MARRAKECH Campaigns
        allCampaigns.add(new Campaign("m1", "Centre Régional de Transfusion", "Ministère de la Santé",
                "Boulevard Zerktouni, Marrakech", "January 22, 2025", "9:00 AM - 3:00 PM", 2.0,
                Arrays.asList("B-", "AB-", "O-"), "Journée nationale du don de sang."));
        
        allCampaigns.add(new Campaign("m2", "CHU Mohammed VI", "Université Cadi Ayyad",
                "Avenue Ibn Sina, Marrakech", "January 24, 2025", "8:00 AM - 5:00 PM", 3.5,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-"), "Collecte universitaire."));
        
        allCampaigns.add(new Campaign("m3", "Menara Mall", "Croissant Rouge",
                "Avenue Mohammed VI, Marrakech", "January 26, 2025", "10:00 AM - 8:00 PM", 4.2,
                Arrays.asList("A+", "B+", "AB+", "O+"), "Campagne de sensibilisation."));

        // TANGER Campaigns
        allCampaigns.add(new Campaign("t1", "Clinique Internationale", "Groupe Akdital",
                "Avenue Hassan II, Tanger", "January 25, 2025", "11:00 AM - 7:00 PM", 2.8,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-"), "Don de sang lors de la foire."));
        
        allCampaigns.add(new Campaign("t2", "Hôpital Mohammed V", "Ministère de la Santé",
                "Avenue Moulay Ismail, Tanger", "January 28, 2025", "8:00 AM - 4:00 PM", 1.9,
                Arrays.asList("O+", "O-", "A+"), "Collecte hebdomadaire."));
        
        allCampaigns.add(new Campaign("t3", "Tanger City Mall", "Association Sang pour Tous",
                "Route de Rabat, Tanger", "January 30, 2025", "10:00 AM - 6:00 PM", 5.5,
                Arrays.asList("A+", "B+", "O+", "AB+"), "Journée portes ouvertes."));

        // FES Campaigns
        allCampaigns.add(new Campaign("f1", "CHU Hassan II", "Université Sidi Mohammed Ben Abdellah",
                "Route Sidi Harazem, Fès", "January 19, 2025", "8:00 AM - 4:00 PM", 2.3,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"), "Collecte régionale."));
        
        allCampaigns.add(new Campaign("f2", "Borj Fez Mall", "Croissant Rouge Marocain",
                "Route de Meknès, Fès", "January 21, 2025", "10:00 AM - 7:00 PM", 4.8,
                Arrays.asList("O+", "O-", "A-", "B-"), "URGENT: Groupes négatifs recherchés."));

        // MEKNES Campaigns
        allCampaigns.add(new Campaign("mk1", "Hôpital Mohammed V", "Ministère de la Santé",
                "Avenue des FAR, Meknès", "January 27, 2025", "7:00 AM - 3:00 PM", 1.5,
                Arrays.asList("O-", "O+"), "URGENT: Pénurie critique de sang type O."));
        
        allCampaigns.add(new Campaign("mk2", "Faculté de Médecine", "Université Moulay Ismail",
                "Marjane, Meknès", "January 29, 2025", "9:00 AM - 5:00 PM", 3.0,
                Arrays.asList("A+", "B+", "AB+", "O+"), "Collecte étudiante."));

        // AGADIR Campaigns
        allCampaigns.add(new Campaign("a1", "CHU Agadir", "Ministère de la Santé",
                "Avenue Hassan II, Agadir", "January 20, 2025", "8:00 AM - 4:00 PM", 2.1,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-"), "Collecte hebdomadaire."));
        
        allCampaigns.add(new Campaign("a2", "Marina Shopping", "Croissant Rouge",
                "Marina d'Agadir", "January 23, 2025", "10:00 AM - 6:00 PM", 3.5,
                Arrays.asList("A+", "B+", "O+"), "Campagne touristique de don."));

        // OUJDA Campaigns
        allCampaigns.add(new Campaign("o1", "CHU Mohammed VI Oujda", "Université Mohammed Premier",
                "Route de Sidi Yahya, Oujda", "January 22, 2025", "8:00 AM - 3:00 PM", 2.0,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"), "Collecte régionale."));
    }

    private void filterCampaignsByLocation() {
        nearbyCampaigns = new ArrayList<>();
        
        // Map city prefixes
        Map<String, String> cityPrefixes = new HashMap<>();
        cityPrefixes.put("casablanca", "c");
        cityPrefixes.put("rabat", "r");
        cityPrefixes.put("marrakech", "m");
        cityPrefixes.put("tanger", "t");
        cityPrefixes.put("fes", "f");
        cityPrefixes.put("meknes", "mk");
        cityPrefixes.put("agadir", "a");
        cityPrefixes.put("oujda", "o");
        
        String prefix = cityPrefixes.getOrDefault(userCity, "c");
        
        // Filter campaigns by user city
        for (Campaign campaign : allCampaigns) {
            if (campaign.getId().startsWith(prefix)) {
                nearbyCampaigns.add(campaign);
            }
        }
        
        // Update header
        if (tvLocationHeader != null) {
            String cityName = userCity.substring(0, 1).toUpperCase() + userCity.substring(1);
            tvLocationHeader.setText("Nearby campaigns in " + cityName);
        }
        
        filteredCampaigns = new ArrayList<>(nearbyCampaigns);

        campaignAdapter = new CampaignAdapter(filteredCampaigns, campaign -> {
            showBookingDialog(campaign);
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
            filteredCampaigns.addAll(nearbyCampaigns);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Campaign campaign : nearbyCampaigns) {
                if (campaign.getName().toLowerCase().contains(lowerQuery) ||
                        campaign.getOrganizer().toLowerCase().contains(lowerQuery) ||
                        campaign.getLocation().toLowerCase().contains(lowerQuery)) {
                    filteredCampaigns.add(campaign);
                }
            }
        }

        campaignAdapter.notifyDataSetChanged();
    }
    
    private void showBookingDialog(Campaign campaign) {
        new AlertDialog.Builder(this)
                .setTitle("Book Appointment")
                .setMessage("Would you like to book an appointment at " + campaign.getName() + "?\n\nLocation: " + campaign.getLocation() + "\nDate: " + campaign.getDate())
                .setPositiveButton("Book Now", (dialog, which) -> {
                    // Save appointment to SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("BloodHeroPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("has_appointment", true);
                    editor.putString("last_appointment_campaign", campaign.getName());
                    editor.putString("last_appointment_location", campaign.getLocation());
                    editor.putString("last_appointment_date", campaign.getDate());
                    editor.putString("last_appointment_time", "09:00 AM");
                    editor.apply();
                    
                    Toast.makeText(this, "Appointment booked successfully!", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to MyAppointmentsActivity
                    Intent intent = new Intent(this, MyAppointmentsActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}