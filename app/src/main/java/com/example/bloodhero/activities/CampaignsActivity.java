package com.example.bloodhero.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
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
import com.example.bloodhero.models.Appointment;
import com.example.bloodhero.models.Campaign;
import com.example.bloodhero.models.User;
import com.example.bloodhero.repository.AppointmentRepository;
import com.example.bloodhero.utils.UserHelper;
import com.example.bloodhero.utils.StatusDialogHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class CampaignsActivity extends AppCompatActivity {

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
    
    private AppointmentRepository appointmentRepository;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campaigns);

        currentUser = UserHelper.getCurrentUser(this);
        appointmentRepository = AppointmentRepository.getInstance(this);
        
        initViews();
        setupToolbar();
        setupMapButton();
        loadUserLocation();
        loadAllCampaigns();
        filterCampaignsByLocation();
        setupSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data and filter campaigns when returning
        currentUser = UserHelper.getCurrentUser(this);
        loadUserLocation();
        filterCampaignsByLocation();
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
        // Get location from current user's profile (from database)
        if (currentUser != null && currentUser.getLocation() != null) {
            userCity = currentUser.getLocation().toLowerCase();
        } else {
            // Fallback to SharedPreferences if user object doesn't have location
            SharedPreferences prefs = getSharedPreferences("BloodHeroPrefs", MODE_PRIVATE);
            userCity = prefs.getString("user_location", "").toLowerCase();
        }
        
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
        Random random = new Random();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);

        // CASABLANCA Campaigns
        allCampaigns.add(new Campaign("c1", "Hôpital Cheikh Khalifa", "Fondation Cheikh Khalifa",
                "Route de Nouaceur, Casablanca", generateRandomDate(random, dateFormat), "8:00 AM - 4:00 PM", 2.5,
                Arrays.asList("O+", "O-", "A-"), "Besoin urgent de sang type O. Sans rendez-vous accepté!"));
        
        allCampaigns.add(new Campaign("c2", "CHU Ibn Rochd", "Ministère de la Santé",
                "Quartier des Hôpitaux, Casablanca", generateRandomDate(random, dateFormat), "9:00 AM - 5:00 PM", 3.2,
                Arrays.asList("A+", "B+", "AB+", "O+"), "Collecte hebdomadaire - tous les groupes bienvenus."));
        
        allCampaigns.add(new Campaign("c3", "Morocco Mall", "Croissant Rouge Marocain",
                "Morocco Mall, Casablanca", generateRandomDate(random, dateFormat), "10:00 AM - 8:00 PM", 5.0,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"), "Journée de don de sang au centre commercial."));
        
        allCampaigns.add(new Campaign("c4", "Clinique Badr", "Groupe Akdital",
                "Boulevard Zerktouni, Casablanca", generateRandomDate(random, dateFormat), "8:00 AM - 3:00 PM", 4.1,
                Arrays.asList("B-", "O-"), "URGENT: Besoin de groupes négatifs."));
        
        allCampaigns.add(new Campaign("c5", "Clinique Al Madina", "Croissant Rouge",
                "Avenue Mers Sultan, Casablanca", generateRandomDate(random, dateFormat), "2:00 PM - 8:00 PM", 3.8,
                Arrays.asList("A+", "O+", "B+"), "Collecte de l'après-midi."));

        // RABAT Campaigns
        allCampaigns.add(new Campaign("r1", "Centre de Transfusion Sanguine", "CHU Ibn Sina",
                "Avenue Mohamed V, Rabat", generateRandomDate(random, dateFormat), "9:00 AM - 5:00 PM", 1.5,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"), "Collecte mensuelle de sang."));
        
        allCampaigns.add(new Campaign("r2", "Université Mohammed V", "Faculté de Médecine",
                "Avenue Allal El Fassi, Rabat", generateRandomDate(random, dateFormat), "10:00 AM - 6:00 PM", 3.8,
                Arrays.asList("A+", "B+", "AB+", "O+"), "Collecte annuelle universitaire."));
        
        allCampaigns.add(new Campaign("r3", "Hôpital Militaire", "Forces Armées Royales",
                "Avenue des FAR, Rabat", generateRandomDate(random, dateFormat), "7:00 AM - 4:00 PM", 2.2,
                Arrays.asList("O-", "O+", "A-"), "Don de sang pour les forces armées."));
        
        allCampaigns.add(new Campaign("r4", "Mega Mall Rabat", "Association des Donneurs",
                "Route de Témara, Rabat", generateRandomDate(random, dateFormat), "11:00 AM - 7:00 PM", 6.5,
                Arrays.asList("A+", "B+", "O+"), "Campagne de sensibilisation au don."));
        
        allCampaigns.add(new Campaign("r5", "Agdal Centre", "Ministère de la Santé",
                "Avenue Mehdi Ben Barka, Rabat", generateRandomDate(random, dateFormat), "3:00 PM - 9:00 PM", 4.2,
                Arrays.asList("O-", "A-", "B-"), "Session du soir pour donneurs actifs."));

        // MARRAKECH Campaigns
        allCampaigns.add(new Campaign("m1", "Centre Régional de Transfusion", "Ministère de la Santé",
                "Boulevard Zerktouni, Marrakech", generateRandomDate(random, dateFormat), "9:00 AM - 3:00 PM", 2.0,
                Arrays.asList("B-", "AB-", "O-"), "Journée nationale du don de sang."));
        
        allCampaigns.add(new Campaign("m2", "CHU Mohammed VI", "Université Cadi Ayyad",
                "Avenue Ibn Sina, Marrakech", generateRandomDate(random, dateFormat), "8:00 AM - 5:00 PM", 3.5,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-"), "Collecte universitaire."));
        
        allCampaigns.add(new Campaign("m3", "Menara Mall", "Croissant Rouge",
                "Avenue Mohammed VI, Marrakech", generateRandomDate(random, dateFormat), "10:00 AM - 8:00 PM", 4.2,
                Arrays.asList("A+", "B+", "AB+", "O+"), "Campagne de sensibilisation."));
        
        allCampaigns.add(new Campaign("m4", "Carré Eden", "Association Dar Zhor",
                "Route de Casablanca, Marrakech", generateRandomDate(random, dateFormat), "1:00 PM - 7:00 PM", 5.1,
                Arrays.asList("O+", "A+"), "Don de sang au centre commercial."));

        // TANGER Campaigns
        allCampaigns.add(new Campaign("t1", "Clinique Internationale", "Groupe Akdital",
                "Avenue Hassan II, Tanger", generateRandomDate(random, dateFormat), "11:00 AM - 7:00 PM", 2.8,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-"), "Don de sang lors de la foire."));
        
        allCampaigns.add(new Campaign("t2", "Hôpital Mohammed V", "Ministère de la Santé",
                "Avenue Moulay Ismail, Tanger", generateRandomDate(random, dateFormat), "8:00 AM - 4:00 PM", 1.9,
                Arrays.asList("O+", "O-", "A+"), "Collecte hebdomadaire."));
        
        allCampaigns.add(new Campaign("t3", "Tanger City Mall", "Association Sang pour Tous",
                "Route de Rabat, Tanger", generateRandomDate(random, dateFormat), "10:00 AM - 6:00 PM", 5.5,
                Arrays.asList("A+", "B+", "O+", "AB+"), "Journée portes ouvertes."));
        
        allCampaigns.add(new Campaign("t4", "Grand Socco", "Croissant Rouge Marocain",
                "Place du 9 Avril, Tanger", generateRandomDate(random, dateFormat), "9:00 AM - 3:00 PM", 3.7,
                Arrays.asList("A+", "A-", "O+", "O-"), "Campagne au coeur de la ville."));

        // FES Campaigns
        allCampaigns.add(new Campaign("f1", "CHU Hassan II", "Université Sidi Mohammed Ben Abdellah",
                "Route Sidi Harazem, Fès", generateRandomDate(random, dateFormat), "8:00 AM - 4:00 PM", 2.3,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"), "Collecte régionale."));
        
        allCampaigns.add(new Campaign("f2", "Borj Fez Mall", "Croissant Rouge Marocain",
                "Route de Meknès, Fès", generateRandomDate(random, dateFormat), "10:00 AM - 7:00 PM", 4.8,
                Arrays.asList("O+", "O-", "A-", "B-"), "URGENT: Groupes négatifs recherchés."));
        
        allCampaigns.add(new Campaign("f3", "Hôpital Ibn Al Khatib", "CHU Fès",
                "Avenue Allal Ben Abdellah, Fès", generateRandomDate(random, dateFormat), "7:00 AM - 2:00 PM", 2.1,
                Arrays.asList("AB+", "AB-", "B+"), "Collecte matinale."));

        // MEKNES Campaigns
        allCampaigns.add(new Campaign("mk1", "Hôpital Mohammed V", "Ministère de la Santé",
                "Avenue des FAR, Meknès", generateRandomDate(random, dateFormat), "7:00 AM - 3:00 PM", 1.5,
                Arrays.asList("O-", "O+"), "URGENT: Pénurie critique de sang type O."));
        
        allCampaigns.add(new Campaign("mk2", "Faculté de Médecine", "Université Moulay Ismail",
                "Marjane, Meknès", generateRandomDate(random, dateFormat), "9:00 AM - 5:00 PM", 3.0,
                Arrays.asList("A+", "B+", "AB+", "O+"), "Collecte étudiante."));
        
        allCampaigns.add(new Campaign("mk3", "Palais des Congrès", "Association Al Amal",
                "Avenue Okba Ibn Nafiaa, Meknès", generateRandomDate(random, dateFormat), "10:00 AM - 4:00 PM", 2.8,
                Arrays.asList("A+", "A-", "B+", "B-"), "Journée solidaire de don."));

        // AGADIR Campaigns
        allCampaigns.add(new Campaign("a1", "CHU Agadir", "Ministère de la Santé",
                "Avenue Hassan II, Agadir", generateRandomDate(random, dateFormat), "8:00 AM - 4:00 PM", 2.1,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-"), "Collecte hebdomadaire."));
        
        allCampaigns.add(new Campaign("a2", "Marina Shopping", "Croissant Rouge",
                "Marina d'Agadir", generateRandomDate(random, dateFormat), "10:00 AM - 6:00 PM", 3.5,
                Arrays.asList("A+", "B+", "O+"), "Campagne touristique de don."));
        
        allCampaigns.add(new Campaign("a3", "Souk El Had", "Association Amal",
                "Boulevard Mohammed V, Agadir", generateRandomDate(random, dateFormat), "2:00 PM - 8:00 PM", 1.8,
                Arrays.asList("O+", "O-", "A+"), "Don de sang au marché central."));

        // OUJDA Campaigns
        allCampaigns.add(new Campaign("o1", "CHU Mohammed VI Oujda", "Université Mohammed Premier",
                "Route de Sidi Yahya, Oujda", generateRandomDate(random, dateFormat), "8:00 AM - 3:00 PM", 2.0,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"), "Collecte régionale."));
        
        allCampaigns.add(new Campaign("o2", "Centre Ville Oujda", "Croissant Rouge",
                "Boulevard Mohammed Derfoufi, Oujda", generateRandomDate(random, dateFormat), "11:00 AM - 6:00 PM", 1.5,
                Arrays.asList("A+", "B+", "O+"), "Campagne urbaine de sensibilisation."));
    }
    
    private String generateRandomDate(Random random, SimpleDateFormat dateFormat) {
        Calendar calendar = Calendar.getInstance();
        int daysToAdd = random.nextInt(7); // 0 to 6 days
        calendar.add(Calendar.DAY_OF_MONTH, daysToAdd);
        return dateFormat.format(calendar.getTime());
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
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to book an appointment", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check donation eligibility
        if (!currentUser.canDonateNow()) {
            int daysRemaining = currentUser.getDaysUntilEligible();
            StatusDialogHelper.showStatusDialog(
                this,
                StatusDialogHelper.StatusType.WARNING,
                "Not eligible yet",
                "You must wait 56 days (8 weeks) between donations.\n" +
                    "You donated " + getDaysSinceLastDonation() + " days ago.\n" +
                    "Come back in " + daysRemaining + " days.",
                "Got it",
                null
            );
            return;
        }
        
        // Check if user already has an active appointment
        List<Appointment> userAppointments = appointmentRepository.getAppointmentsByUserId(currentUser.getId());
        for (Appointment appt : userAppointments) {
            if (appt.getStatus() == Appointment.Status.SCHEDULED) {
                StatusDialogHelper.showStatusDialog(
                        this,
                        StatusDialogHelper.StatusType.INFO,
                        "Active appointment",
                        "You already have a scheduled appointment. Complete or cancel it before booking a new one.",
                        "View",
                        () -> {
                            Intent intent = new Intent(this, MyAppointmentsActivity.class);
                            startActivity(intent);
                        }
                );
                return;
            }
        }
        
        // Show enhanced book appointment dialog
        showBookAppointmentDialog(campaign);
    }
    
    private int getDaysSinceLastDonation() {
        if (currentUser.getLastDonationDate() == 0) {
            return 0;
        }
        return (int) ((System.currentTimeMillis() - currentUser.getLastDonationDate()) / (1000 * 60 * 60 * 24));
    }

    private void showBookAppointmentDialog(Campaign campaign) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_book_appointment, null);
        
        TextView tvCampaignName = dialogView.findViewById(R.id.tvCampaignName);
        TextView tvLocation = dialogView.findViewById(R.id.tvLocation);
        TextView tvDate = dialogView.findViewById(R.id.tvDate);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnBookNow = dialogView.findViewById(R.id.btnBookNow);
        
        tvCampaignName.setText(campaign.getName());
        tvLocation.setText(campaign.getLocation());
        tvDate.setText(campaign.getDate());
        
        AlertDialog dialog = builder.setView(dialogView).create();
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnBookNow.setOnClickListener(v -> {
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
            
            // Show success dialog
            StatusDialogHelper.showStatusDialog(
                    this,
                    StatusDialogHelper.StatusType.SUCCESS,
                    "Appointment Booked!",
                    "Your appointment has been successfully scheduled for " + campaign.getDate() + ".",
                    "View",
                    () -> {
                        Intent intent = new Intent(this, MyAppointmentsActivity.class);
                        startActivity(intent);
                    }
            );
            
            dialog.dismiss();
        });
        
        dialog.show();
        
        // Set dialog to fit screen properly without overlapping
        if (dialog.getWindow() != null) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            lp.width = (int) (screenWidth * 0.85);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.y = 0;
            dialog.getWindow().setAttributes(lp);
        }
    }
}