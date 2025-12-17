package com.example.bloodhero.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.bloodhero.R;
import com.example.bloodhero.models.Appointment;
import com.example.bloodhero.models.Campaign;
import com.example.bloodhero.models.User;
import com.example.bloodhero.repository.AppointmentRepository;
import com.example.bloodhero.utils.UserHelper;
import com.google.android.material.button.MaterialButton;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CampaignMapActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;

    private MapView mapView;
    private Toolbar toolbar;
    private CardView cardCampaignInfo;
    private TextView tvCampaignName, tvCampaignOrganizer, tvCampaignLocation, tvCampaignDate, tvCampaignBloodTypes;
    private MaterialButton btnBookNow;
    private LinearLayout btnListView;
    
    private AppointmentRepository appointmentRepository;
    private User currentUser;

    private List<Campaign> campaigns;
    private Map<String, GeoPoint> cityCoordinates;
    private Campaign selectedCampaign;
    private String userCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize OSMDroid configuration
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_campaign_map);

        currentUser = UserHelper.getCurrentUser(this);
        appointmentRepository = AppointmentRepository.getInstance(this);
        
        initCityCoordinates();
        initViews();
        setupToolbar();
        loadUserLocation();
        
        if (checkPermissions()) {
            initMap();
        }
    }

    private void initCityCoordinates() {
        cityCoordinates = new HashMap<>();
        // Morocco city coordinates
        cityCoordinates.put("casablanca", new GeoPoint(33.5731, -7.5898));
        cityCoordinates.put("rabat", new GeoPoint(34.0209, -6.8416));
        cityCoordinates.put("marrakech", new GeoPoint(31.6295, -7.9811));
        cityCoordinates.put("tanger", new GeoPoint(35.7595, -5.8340));
        cityCoordinates.put("fes", new GeoPoint(34.0181, -5.0078));
        cityCoordinates.put("meknes", new GeoPoint(33.8935, -5.5473));
        cityCoordinates.put("agadir", new GeoPoint(30.4278, -9.5981));
        cityCoordinates.put("oujda", new GeoPoint(34.6805, -1.9076));
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        mapView = findViewById(R.id.mapView);
        cardCampaignInfo = findViewById(R.id.cardCampaignInfo);
        tvCampaignName = findViewById(R.id.tvCampaignName);
        tvCampaignOrganizer = findViewById(R.id.tvCampaignOrganizer);
        tvCampaignLocation = findViewById(R.id.tvCampaignLocation);
        tvCampaignDate = findViewById(R.id.tvCampaignDate);
        tvCampaignBloodTypes = findViewById(R.id.tvCampaignBloodTypes);
        btnBookNow = findViewById(R.id.btnBookNow);
        btnListView = findViewById(R.id.btnListView);

        cardCampaignInfo.setVisibility(View.GONE);

        btnBookNow.setOnClickListener(v -> {
            if (selectedCampaign != null) {
                // Show booking confirmation dialog
                showBookingDialog(selectedCampaign);
            }
        });

        btnListView.setOnClickListener(v -> {
            startActivity(new Intent(this, CampaignsActivity.class));
            finish();
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
        SharedPreferences prefs = getSharedPreferences("BloodHeroPrefs", MODE_PRIVATE);
        String location = prefs.getString("user_location", "").toLowerCase();
        
        if (location.contains("casablanca") || location.contains("casa")) {
            userCity = "casablanca";
        } else if (location.contains("rabat")) {
            userCity = "rabat";
        } else if (location.contains("marrakech") || location.contains("marrakesh")) {
            userCity = "marrakech";
        } else if (location.contains("tanger") || location.contains("tangier")) {
            userCity = "tanger";
        } else if (location.contains("fes") || location.contains("fez") || location.contains("fès")) {
            userCity = "fes";
        } else if (location.contains("meknes") || location.contains("meknès")) {
            userCity = "meknes";
        } else if (location.contains("agadir")) {
            userCity = "agadir";
        } else if (location.contains("oujda")) {
            userCity = "oujda";
        } else {
            userCity = "casablanca";
        }
    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Even if permission is denied, we can still show the map, just without user location
            initMap();
        }
    }

    private void initMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        
        IMapController mapController = mapView.getController();
        
        // Get user city coordinates
        GeoPoint userCityPoint = cityCoordinates.getOrDefault(userCity, 
                cityCoordinates.get("casablanca"));
        
        mapController.setZoom(12.0);
        mapController.setCenter(userCityPoint);

        loadCampaigns();
        addCampaignMarkers();
    }

    private void loadCampaigns() {
        campaigns = new ArrayList<>();

        Map<String, String> cityPrefixes = new HashMap<>();
        cityPrefixes.put("casablanca", "c");
        cityPrefixes.put("rabat", "r");
        cityPrefixes.put("marrakech", "m");
        cityPrefixes.put("tanger", "t");
        cityPrefixes.put("fes", "f");
        cityPrefixes.put("meknes", "mk");
        cityPrefixes.put("agadir", "a");
        cityPrefixes.put("oujda", "o");

        List<Campaign> allCampaigns = getAllCampaigns();
        String prefix = cityPrefixes.getOrDefault(userCity, "c");

        for (Campaign campaign : allCampaigns) {
            if (campaign.getId().startsWith(prefix)) {
                campaigns.add(campaign);
            }
        }
    }

    private List<Campaign> getAllCampaigns() {
        List<Campaign> all = new ArrayList<>();

        // CASABLANCA
        all.add(new Campaign("c1", "Hôpital Cheikh Khalifa", "Fondation Cheikh Khalifa",
                "Route de Nouaceur, Casablanca", "January 18, 2025", "8:00 AM - 4:00 PM", 2.5,
                Arrays.asList("O+", "O-", "A-"), "Besoin urgent de sang type O."));
        all.add(new Campaign("c2", "CHU Ibn Rochd", "Ministère de la Santé",
                "Quartier des Hôpitaux, Casablanca", "January 20, 2025", "9:00 AM - 5:00 PM", 3.2,
                Arrays.asList("A+", "B+", "AB+", "O+"), "Collecte hebdomadaire."));
        all.add(new Campaign("c3", "Morocco Mall", "Croissant Rouge Marocain",
                "Morocco Mall, Casablanca", "January 22, 2025", "10:00 AM - 8:00 PM", 5.0,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"), "Journée de don."));
        all.add(new Campaign("c4", "Clinique Badr", "Groupe Akdital",
                "Boulevard Zerktouni, Casablanca", "January 25, 2025", "8:00 AM - 3:00 PM", 4.1,
                Arrays.asList("B-", "O-"), "URGENT: Groupes négatifs."));

        // RABAT
        all.add(new Campaign("r1", "Centre de Transfusion", "CHU Ibn Sina",
                "Avenue Mohamed V, Rabat", "January 15, 2025", "9:00 AM - 5:00 PM", 1.5,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"), "Collecte mensuelle."));
        all.add(new Campaign("r2", "Université Mohammed V", "Faculté de Médecine",
                "Avenue Allal El Fassi, Rabat", "January 20, 2025", "10:00 AM - 6:00 PM", 3.8,
                Arrays.asList("A+", "B+", "AB+", "O+"), "Collecte universitaire."));
        all.add(new Campaign("r3", "Hôpital Militaire", "Forces Armées Royales",
                "Avenue des FAR, Rabat", "January 23, 2025", "7:00 AM - 4:00 PM", 2.2,
                Arrays.asList("O-", "O+", "A-"), "Don pour les forces armées."));
        all.add(new Campaign("r4", "Mega Mall Rabat", "Association des Donneurs",
                "Route de Témara, Rabat", "January 27, 2025", "11:00 AM - 7:00 PM", 6.5,
                Arrays.asList("A+", "B+", "O+"), "Campagne de sensibilisation."));

        // MARRAKECH
        all.add(new Campaign("m1", "Centre Régional", "Ministère de la Santé",
                "Boulevard Zerktouni, Marrakech", "January 22, 2025", "9:00 AM - 3:00 PM", 2.0,
                Arrays.asList("B-", "AB-", "O-"), "Journée nationale."));
        all.add(new Campaign("m2", "CHU Mohammed VI", "Université Cadi Ayyad",
                "Avenue Ibn Sina, Marrakech", "January 24, 2025", "8:00 AM - 5:00 PM", 3.5,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-"), "Collecte universitaire."));
        all.add(new Campaign("m3", "Menara Mall", "Croissant Rouge",
                "Avenue Mohammed VI, Marrakech", "January 26, 2025", "10:00 AM - 8:00 PM", 4.2,
                Arrays.asList("A+", "B+", "AB+", "O+"), "Campagne de sensibilisation."));

        // TANGER
        all.add(new Campaign("t1", "Clinique Internationale", "Groupe Akdital",
                "Avenue Hassan II, Tanger", "January 25, 2025", "11:00 AM - 7:00 PM", 2.8,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-"), "Don lors de la foire."));
        all.add(new Campaign("t2", "Hôpital Mohammed V", "Ministère de la Santé",
                "Avenue Moulay Ismail, Tanger", "January 28, 2025", "8:00 AM - 4:00 PM", 1.9,
                Arrays.asList("O+", "O-", "A+"), "Collecte hebdomadaire."));
        all.add(new Campaign("t3", "Tanger City Mall", "Association Sang pour Tous",
                "Route de Rabat, Tanger", "January 30, 2025", "10:00 AM - 6:00 PM", 5.5,
                Arrays.asList("A+", "B+", "O+", "AB+"), "Journée portes ouvertes."));

        // FES
        all.add(new Campaign("f1", "CHU Hassan II", "Université Sidi Mohammed",
                "Route Sidi Harazem, Fès", "January 19, 2025", "8:00 AM - 4:00 PM", 2.3,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"), "Collecte régionale."));
        all.add(new Campaign("f2", "Borj Fez Mall", "Croissant Rouge Marocain",
                "Route de Meknès, Fès", "January 21, 2025", "10:00 AM - 7:00 PM", 4.8,
                Arrays.asList("O+", "O-", "A-", "B-"), "URGENT: Groupes négatifs."));

        // MEKNES
        all.add(new Campaign("mk1", "Hôpital Mohammed V", "Ministère de la Santé",
                "Avenue des FAR, Meknès", "January 27, 2025", "7:00 AM - 3:00 PM", 1.5,
                Arrays.asList("O-", "O+"), "URGENT: Pénurie de sang type O."));
        all.add(new Campaign("mk2", "Faculté de Médecine", "Université Moulay Ismail",
                "Marjane, Meknès", "January 29, 2025", "9:00 AM - 5:00 PM", 3.0,
                Arrays.asList("A+", "B+", "AB+", "O+"), "Collecte étudiante."));

        // AGADIR
        all.add(new Campaign("a1", "CHU Agadir", "Ministère de la Santé",
                "Avenue Hassan II, Agadir", "January 20, 2025", "8:00 AM - 4:00 PM", 2.1,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-"), "Collecte hebdomadaire."));
        all.add(new Campaign("a2", "Marina Shopping", "Croissant Rouge",
                "Marina d'Agadir", "January 23, 2025", "10:00 AM - 6:00 PM", 3.5,
                Arrays.asList("A+", "B+", "O+"), "Campagne touristique."));

        // OUJDA
        all.add(new Campaign("o1", "CHU Mohammed VI", "Université Mohammed Premier",
                "Route de Sidi Yahya, Oujda", "January 22, 2025", "8:00 AM - 3:00 PM", 2.0,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"), "Collecte régionale."));

        return all;
    }

    private void addCampaignMarkers() {
        GeoPoint cityCenter = cityCoordinates.getOrDefault(userCity, cityCoordinates.get("casablanca"));

        for (int i = 0; i < campaigns.size(); i++) {
            Campaign campaign = campaigns.get(i);
            
            // Create markers at slightly different positions around the city center
            double latOffset = (i % 3 - 1) * 0.015 + (Math.random() - 0.5) * 0.01;
            double lonOffset = (i / 3 - 1) * 0.018 + (Math.random() - 0.5) * 0.01;
            
            GeoPoint point = new GeoPoint(
                    cityCenter.getLatitude() + latOffset,
                    cityCenter.getLongitude() + lonOffset
            );

            Marker marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setTitle(campaign.getName());
            marker.setSnippet(campaign.getLocation());
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            
            // Store campaign reference
            final Campaign markerCampaign = campaign;
            marker.setOnMarkerClickListener((m, mv) -> {
                showCampaignInfo(markerCampaign);
                return true;
            });

            mapView.getOverlays().add(marker);
        }

        mapView.invalidate();
    }

    private void showCampaignInfo(Campaign campaign) {
        selectedCampaign = campaign;
        cardCampaignInfo.setVisibility(View.VISIBLE);

        tvCampaignName.setText(campaign.getName());
        tvCampaignOrganizer.setText(campaign.getOrganizer());
        tvCampaignLocation.setText(campaign.getLocation());
        tvCampaignDate.setText(campaign.getDate() + " • " + campaign.getTime());
        
        StringBuilder bloodTypes = new StringBuilder();
        for (int i = 0; i < campaign.getBloodTypesNeeded().size(); i++) {
            if (i > 0) bloodTypes.append(", ");
            bloodTypes.append(campaign.getBloodTypesNeeded().get(i));
        }
        tvCampaignBloodTypes.setText("Needed: " + bloodTypes.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }
    
    private void showBookingDialog(Campaign campaign) {
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to book an appointment", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if user already has an active appointment
        List<Appointment> userAppointments = appointmentRepository.getAppointmentsByUserId(currentUser.getId());
        for (Appointment appt : userAppointments) {
            if (appt.getStatus() == Appointment.Status.SCHEDULED) {
                new AlertDialog.Builder(this)
                        .setTitle("Active Appointment Exists")
                        .setMessage("You already have an active appointment scheduled. You can only book one appointment at a time.\n\nPlease complete or cancel your current appointment before booking a new one.")
                        .setPositiveButton("View My Appointment", (dialog, which) -> {
                            Intent intent = new Intent(this, MyAppointmentsActivity.class);
                            startActivity(intent);
                        })
                        .setNegativeButton("Close", null)
                        .show();
                return;
            }
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Book Appointment")
                .setMessage("Would you like to book an appointment at " + campaign.getName() + "?\n\nLocation: " + campaign.getLocation() + "\nDate: " + campaign.getDate())
                .setPositiveButton("Book Now", (dialog, which) -> {
                    // Create appointment and save to SQLite
                    Appointment appointment = new Appointment(
                            UUID.randomUUID().toString(),
                            currentUser.getId(),
                            campaign.getId(),
                            campaign.getName(),
                            campaign.getLocation(),
                            campaign.getDate(),
                            "09:00 AM",
                            Appointment.Status.SCHEDULED
                    );
                    
                    appointmentRepository.createAppointment(appointment);
                    
                    Toast.makeText(this, "Appointment booked successfully!", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to MyAppointmentsActivity
                    Intent intent = new Intent(this, MyAppointmentsActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
