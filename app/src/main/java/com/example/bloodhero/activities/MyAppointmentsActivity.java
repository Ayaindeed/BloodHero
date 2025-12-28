package com.example.bloodhero.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.bloodhero.HomeActivity;
import com.example.bloodhero.R;
import com.example.bloodhero.models.Appointment;
import com.example.bloodhero.models.Campaign;
import com.example.bloodhero.models.Donation;
import com.example.bloodhero.models.User;
import com.example.bloodhero.repository.AppointmentRepository;
import com.example.bloodhero.utils.EnhancedDialogHelper;
import com.example.bloodhero.repository.DonationRepository;
import com.example.bloodhero.repository.UserRepository;
import com.example.bloodhero.utils.QRCodeHelper;
import com.example.bloodhero.utils.UserHelper;
import com.example.bloodhero.utils.VerificationCodeGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MyAppointmentsActivity extends AppCompatActivity {

    public static final String ACTION_APPOINTMENT_UPDATED = "com.example.bloodhero.APPOINTMENT_UPDATED";
    public static final String EXTRA_APPOINTMENT_ID = "appointment_id";

    private ImageButton btnBack;
    private LinearLayout emptyState;
    private CardView cardAppointment;
    private TextView tvCampaignName, tvAppointmentDate, tvAppointmentTime, tvLocation, tvStatus;
    private Button btnCancelAppointment, btnReschedule, btnEnterCode, btnCheckIn;
    private ImageView ivQRCode;
    private LinearLayout qrCodeSection;
    
    private AppointmentRepository appointmentRepository;
    private DonationRepository donationRepository;
    private UserRepository userRepository;
    private User currentUser;
    private Appointment currentAppointment;
    
    private BroadcastReceiver appointmentUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String updatedAppointmentId = intent.getStringExtra(EXTRA_APPOINTMENT_ID);
            // Reload appointment if it's the current one or if no specific ID was sent
            if (updatedAppointmentId == null || 
                (currentAppointment != null && currentAppointment.getId().equals(updatedAppointmentId))) {
                loadAppointment();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_appointments);

        currentUser = UserHelper.getCurrentUser(this);
        appointmentRepository = AppointmentRepository.getInstance(this);
        donationRepository = DonationRepository.getInstance(this);
        userRepository = UserRepository.getInstance(this);
        
        initViews();
        loadAppointment();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        emptyState = findViewById(R.id.emptyState);
        cardAppointment = findViewById(R.id.cardAppointment);
        tvCampaignName = findViewById(R.id.tvCampaignName);
        tvAppointmentDate = findViewById(R.id.tvAppointmentDate);
        tvAppointmentTime = findViewById(R.id.tvAppointmentTime);
        tvLocation = findViewById(R.id.tvLocation);
        tvStatus = findViewById(R.id.tvStatus);
        btnCancelAppointment = findViewById(R.id.btnCancelAppointment);
        btnReschedule = findViewById(R.id.btnReschedule);
        btnEnterCode = findViewById(R.id.btnEnterCode);
        btnCheckIn = findViewById(R.id.btnCheckIn);
        ivQRCode = findViewById(R.id.ivQRCode);
        qrCodeSection = findViewById(R.id.qrCodeSection);
    }

    private void loadAppointment() {
        if (currentUser == null) {
            emptyState.setVisibility(View.VISIBLE);
            cardAppointment.setVisibility(View.GONE);
            return;
        }
        
        // Get user's appointments from SQLite, ordered by date descending
        List<Appointment> appointments = appointmentRepository.getAppointmentsByUserId(currentUser.getId());
        
        // Find the most recent non-cancelled appointment
        currentAppointment = null;
        for (Appointment appt : appointments) {
            if (appt.getStatus() != Appointment.Status.CANCELLED) {
                currentAppointment = appt;
                break;
            }
        }

        if (currentAppointment != null) {
            // Show appointment
            emptyState.setVisibility(View.GONE);
            cardAppointment.setVisibility(View.VISIBLE);

            tvCampaignName.setText(currentAppointment.getCampaignName() != null ? 
                currentAppointment.getCampaignName() : "Blood Donation");
            tvAppointmentDate.setText(formatDate(currentAppointment.getDate()));
            tvAppointmentTime.setText(currentAppointment.getTime());
            tvLocation.setText(currentAppointment.getLocation() != null ? 
                currentAppointment.getLocation() : "Location not specified");
            tvStatus.setText(currentAppointment.getStatus().toString());
            
            // Generate and display QR code
            displayQRCode(currentAppointment);
            
            // Reset button visibility
            btnCancelAppointment.setVisibility(View.VISIBLE);
            btnReschedule.setVisibility(View.VISIBLE);
            btnEnterCode.setVisibility(View.GONE);
            btnCheckIn.setVisibility(View.GONE);

            // Set status color and button visibility based on status
            switch (currentAppointment.getStatus()) {
                case CONFIRMED:
                    tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                    tvStatus.setText("CONFIRMED");
                    qrCodeSection.setVisibility(View.VISIBLE);
                    btnCheckIn.setVisibility(View.VISIBLE); // Show check-in button for testing
                    break;
                case CHECKED_IN:
                    tvStatus.setBackgroundResource(R.drawable.bg_status_completed);
                    tvStatus.setText("CHECKED IN");
                    btnCancelAppointment.setVisibility(View.GONE);
                    btnReschedule.setVisibility(View.GONE);
                    qrCodeSection.setVisibility(View.GONE);
                    break;
                case IN_PROGRESS:
                    tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                    tvStatus.setText("DONATING - BED " + (currentAppointment.getBedNumber() != null ? currentAppointment.getBedNumber() : ""));
                    btnCancelAppointment.setVisibility(View.GONE);
                    btnReschedule.setVisibility(View.GONE);
                    qrCodeSection.setVisibility(View.GONE);
                    break;
                case PENDING_VERIFICATION:
                    tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                    tvStatus.setText("ENTER VERIFICATION CODE");
                    btnCancelAppointment.setVisibility(View.GONE);
                    btnReschedule.setVisibility(View.GONE);
                    btnEnterCode.setVisibility(View.VISIBLE);
                    qrCodeSection.setVisibility(View.GONE);
                    break;
                case COMPLETED:
                    tvStatus.setBackgroundResource(R.drawable.bg_status_completed);
                    btnCancelAppointment.setVisibility(View.GONE);
                    btnReschedule.setVisibility(View.GONE);
                    qrCodeSection.setVisibility(View.GONE);
                    break;
                case CANCELLED:
                    tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
                    btnCancelAppointment.setVisibility(View.GONE);
                    btnReschedule.setVisibility(View.GONE);
                    qrCodeSection.setVisibility(View.GONE);
                    break;
                case SCHEDULED:
                default:
                    tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                    qrCodeSection.setVisibility(View.VISIBLE);
                    break;
            }
        } else {
            // Show empty state
            emptyState.setVisibility(View.VISIBLE);
            cardAppointment.setVisibility(View.GONE);
        }
    }
    
    private void displayQRCode(Appointment appointment) {
        if (appointment == null || appointment.getId() == null) {
            qrCodeSection.setVisibility(View.GONE);
            return;
        }
        
        // Generate QR code bitmap
        Bitmap qrBitmap = QRCodeHelper.generateQRCode(appointment.getId(), 400);
        if (qrBitmap != null) {
            ivQRCode.setImageBitmap(qrBitmap);
            qrCodeSection.setVisibility(View.VISIBLE);
        } else {
            qrCodeSection.setVisibility(View.GONE);
        }
    }

    private String formatDate(String dateStr) {
        // Convert yyyy-MM-dd to readable format
        try {
            String[] parts = dateStr.split("-");
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            int monthIndex = Integer.parseInt(parts[1]) - 1;
            return months[monthIndex] + " " + parts[2] + ", " + parts[0];
        } catch (Exception e) {
            return dateStr;
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnCancelAppointment.setOnClickListener(v -> showCancelConfirmation());

        btnReschedule.setOnClickListener(v -> {
            // Go to campaigns to book a new appointment
            Intent intent = new Intent(this, CampaignsActivity.class);
            startActivity(intent);
        });
        
        btnEnterCode.setOnClickListener(v -> showVerificationCodeDialog());
        
        btnCheckIn.setOnClickListener(v -> performCheckIn());
    }

    private void showCancelConfirmation() {
        EnhancedDialogHelper.showConfirmationDialog(
                this,
                "Cancel Appointment",
                "Are you sure you want to cancel this appointment? This action cannot be undone.",
                "Yes, Cancel",
                "No, Keep It",
                this::cancelAppointment
        );
    }

    private void cancelAppointment() {
        if (currentAppointment == null) {
            Toast.makeText(this, "No appointment to cancel", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Update appointment status in SQLite
        boolean success = appointmentRepository.updateStatus(currentAppointment.getId(), Appointment.Status.CANCELLED);
        
        if (success) {
            Toast.makeText(this, "Appointment cancelled successfully", Toast.LENGTH_SHORT).show();
            // Reload appointment from database to ensure sync
            currentAppointment = null;
            loadAppointment(); // Refresh UI with updated data
        } else {
            Toast.makeText(this, "Failed to cancel appointment", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register broadcast receiver for real-time updates
        LocalBroadcastManager.getInstance(this).registerReceiver(
            appointmentUpdateReceiver,
            new IntentFilter(ACTION_APPOINTMENT_UPDATED)
        );
        loadAppointment();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Unregister broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(appointmentUpdateReceiver);
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // Refresh when window regains focus (e.g., coming back from QR scanner)
        if (hasFocus) {
            loadAppointment();
        }
    }
    
    private void performCheckIn() {
        if (currentAppointment == null) {
            Toast.makeText(this, "No appointment found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        EnhancedDialogHelper.showConfirmationDialog(
                this,
                "Check In",
                "Check in for your appointment now?",
                "Check In",
                "Cancel",
                () -> {
                    // Update status to CHECKED_IN
                    boolean success = appointmentRepository.updateStatus(
                            currentAppointment.getId(), 
                            Appointment.Status.CHECKED_IN
                    );
                    
                    if (success) {
                        Toast.makeText(MyAppointmentsActivity.this, "✓ Checked in successfully!", Toast.LENGTH_SHORT).show();
                        loadAppointment(); // Refresh UI
                    } else {
                        Toast.makeText(MyAppointmentsActivity.this, "Failed to check in", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
    
    private void showVerificationCodeDialog() {
        if (currentAppointment == null) {
            Toast.makeText(this, "No appointment found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create enhanced dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_enter_verification_code, null);
        
        EditText etCode = dialogView.findViewById(R.id.etVerificationCode);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnVerify = dialogView.findViewById(R.id.btnVerify);
        
        AlertDialog dialog = builder.setView(dialogView).create();
        
        // Set proper window attributes for dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                (int)(getResources().getDisplayMetrics().widthPixels * 0.90),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        
        dialog.show();
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnVerify.setOnClickListener(v -> {
            String enteredCode = etCode.getText().toString().trim().toUpperCase();
            if (enteredCode.isEmpty()) {
                Toast.makeText(this, "Please enter a code", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            verifyDonationCode(enteredCode);
        });
    }
    
    private void verifyDonationCode(String enteredCode) {
        if (enteredCode.length() != 4) {
            Toast.makeText(this, "Code must be 4 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!VerificationCodeGenerator.isValidFormat(enteredCode)) {
            Toast.makeText(this, "Invalid code format", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if code matches
        if (currentAppointment.verifyCode(enteredCode)) {
            completeDonation();
        } else {
            EnhancedDialogHelper.showConfirmationDialog(
                    this,
                    "Invalid Code",
                    "The code you entered is incorrect. Please try again or contact the donation center.",
                    "Try Again",
                    "Cancel",
                    this::showVerificationCodeDialog
            );
        }
    }
    
    private void completeDonation() {
        // Create donation record
        Donation donation = new Donation();
        donation.setId(UUID.randomUUID().toString());
        donation.setUserId(currentUser.getId());
        donation.setCampaignId(currentAppointment.getCampaignId());
        donation.setCampaignName(currentAppointment.getCampaignName());
        donation.setLocation(currentAppointment.getLocation());
        donation.setDate(currentAppointment.getDate());
        donation.setBloodType(currentUser.getBloodType());
        donation.setPointsEarned(50); // Base points
        donation.setStatus("COMPLETED");
        
        // Save donation
        boolean donationSaved = donationRepository.saveDonation(donation);
        
        // Update appointment status to COMPLETED
        boolean appointmentUpdated = appointmentRepository.updateStatus(
                currentAppointment.getId(), 
                Appointment.Status.COMPLETED
        );
        
        // Broadcast the update (even though we're in the same activity, in case there are other listeners)
        if (appointmentUpdated) {
            Intent intent = new Intent(ACTION_APPOINTMENT_UPDATED);
            intent.putExtra(EXTRA_APPOINTMENT_ID, currentAppointment.getId());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
        
        // Update user stats - incrementDonations already updates count, points, and last donation date
        userRepository.incrementDonations(currentUser.getId(), 50);
        
        // Reload current user to reflect updated values
        currentUser = UserHelper.getCurrentUser(this);
        
        if (donationSaved && appointmentUpdated) {
            showCompletionDialog();
        } else {
            Toast.makeText(this, "Error completing donation. Please contact support.", Toast.LENGTH_LONG).show();
        }
    }
    
    private void showCompletionDialog() {
        EnhancedDialogHelper.showInfoDialog(
                this,
                "🎉 Donation Complete!",
                "Thank you for your donation!\n\n" +
                "You've earned 50 points!\n" +
                "Total donations: " + currentUser.getTotalDonations() + "\n" +
                "Total points: " + currentUser.getTotalPoints(),
                "OK",
                () -> {
                    loadAppointment(); // Refresh UI
                }
        );
    }

    private void seedAppointmentsForAllCampaigns() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        String today = sdf.format(new java.util.Date());
        
        // Get user's city
        String userCity = getUserCity();
        
        // Create all campaigns and filter by user's city
        List<Campaign> userCampaigns = filterCampaignsByUserCity(getAllCampaigns(), userCity);
        
        // Create appointments for user's city campaigns with today's date
        for (Campaign campaign : userCampaigns) {
            Appointment appointment = new Appointment(
                    UUID.randomUUID().toString(),
                    currentUser.getId(),
                    campaign.getId(),
                    campaign.getName(),
                    campaign.getLocation(),
                    today,
                    campaign.getTime(),
                    Appointment.Status.CONFIRMED
            );
            appointmentRepository.createAppointment(appointment);
        }
    }

    private String getUserCity() {
        String userCity = "";
        
        if (currentUser != null && currentUser.getLocation() != null) {
            userCity = currentUser.getLocation().toLowerCase();
        } else {
            android.content.SharedPreferences prefs = getSharedPreferences("BloodHeroPrefs", MODE_PRIVATE);
            userCity = prefs.getString("user_location", "").toLowerCase();
        }
        
        // Extract city name from location string
        if (userCity.contains("casablanca") || userCity.contains("casa")) {
            return "casablanca";
        } else if (userCity.contains("rabat")) {
            return "rabat";
        } else if (userCity.contains("marrakech") || userCity.contains("marrakesh")) {
            return "marrakech";
        } else if (userCity.contains("tanger") || userCity.contains("tangier")) {
            return "tanger";
        } else if (userCity.contains("fes") || userCity.contains("fez") || userCity.contains("fès")) {
            return "fes";
        } else if (userCity.contains("meknes") || userCity.contains("meknès")) {
            return "meknes";
        } else if (userCity.contains("agadir")) {
            return "agadir";
        } else if (userCity.contains("oujda")) {
            return "oujda";
        }
        
        return "casablanca"; // Default to Casablanca
    }

    private List<Campaign> filterCampaignsByUserCity(List<Campaign> campaigns, String userCity) {
        List<Campaign> filtered = new ArrayList<>();
        
        for (Campaign campaign : campaigns) {
            String location = campaign.getLocation().toLowerCase();
            
            if (userCity.equals("casablanca") && (location.contains("casablanca") || location.contains("casa"))) {
                filtered.add(campaign);
            } else if (userCity.equals("rabat") && location.contains("rabat")) {
                filtered.add(campaign);
            } else if (userCity.equals("marrakech") && (location.contains("marrakech") || location.contains("marrakesh"))) {
                filtered.add(campaign);
            } else if (userCity.equals("tanger") && (location.contains("tanger") || location.contains("tangier"))) {
                filtered.add(campaign);
            } else if (userCity.equals("fes") && (location.contains("fès") || location.contains("fes") || location.contains("fez"))) {
                filtered.add(campaign);
            } else if (userCity.equals("meknes") && (location.contains("mekn") || location.contains("meknès"))) {
                filtered.add(campaign);
            } else if (userCity.equals("agadir") && location.contains("agadir")) {
                filtered.add(campaign);
            } else if (userCity.equals("oujda") && location.contains("oujda")) {
                filtered.add(campaign);
            }
        }
        
        return filtered;
    }

    private List<Campaign> getAllCampaigns() {
        List<Campaign> campaigns = new ArrayList<>();
        java.util.Random random = new java.util.Random();
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MMMM d, yyyy", java.util.Locale.ENGLISH);

        // CASABLANCA Campaigns
        campaigns.add(new Campaign("c1", "Hôpital Cheikh Khalifa", "Fondation Cheikh Khalifa",
                "Route de Nouaceur, Casablanca", generateRandomDate(random, dateFormat), "8:00 AM - 4:00 PM", 2.5,
                Arrays.asList("O+", "O-", "A+", "A-"), "Don de sang organisé par la Fondation Cheikh Khalifa."));
        
        campaigns.add(new Campaign("c2", "Centre Khalid Ibn Walid", "Ministère de la Santé",
                "Quartier Ain Sebaa, Casablanca", generateRandomDate(random, dateFormat), "9:00 AM - 5:00 PM", 3.2,
                Arrays.asList("B+", "B-", "AB+", "AB-"), "Collecte régulière pour les patients."));
        
        campaigns.add(new Campaign("c3", "Casablanca City Center", "Croissant Rouge Marocain",
                "Boulevard de la Corniche, Casablanca", generateRandomDate(random, dateFormat), "10:00 AM - 6:00 PM", 4.1,
                Arrays.asList("A+", "O+", "B+"), "Journée internationale du don de sang."));
        
        campaigns.add(new Campaign("c4", "Hôpital Ibn Sina", "CHU Casablanca",
                "Route d'El Jadida, Casablanca", generateRandomDate(random, dateFormat), "8:00 AM - 4:00 PM", 2.8,
                Arrays.asList("O+", "A+", "B+", "AB+"), "Don de sang urgent."));
        
        campaigns.add(new Campaign("c5", "Aïn Chock Medical Center", "Centre Médical Aïn Chock",
                "Avenue Lalla Yacout, Casablanca", generateRandomDate(random, dateFormat), "11:00 AM - 7:00 PM", 1.9,
                Arrays.asList("O+", "A+"), "Don de sang au centre commercial."));

        // RABAT Campaigns
        campaigns.add(new Campaign("r1", "Hôpital Al Farabi", "Ministère de la Santé",
                "Avenue de la République, Rabat", generateRandomDate(random, dateFormat), "8:00 AM - 4:00 PM", 2.2,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-"), "Centre principal de transfusion."));
        
        campaigns.add(new Campaign("r2", "Centre Hospitalier Universitaire", "Université Mohammed V",
                "Avenue de Marrakech, Rabat", generateRandomDate(random, dateFormat), "9:00 AM - 5:00 PM", 3.5,
                Arrays.asList("O+", "O-", "AB+"), "Collecte pour les étudiants."));
        
        campaigns.add(new Campaign("r3", "Takadoum Medical Plaza", "Croissant Rouge",
                "Boulevard de l'Océan Atlantique, Rabat", generateRandomDate(random, dateFormat), "10:00 AM - 7:00 PM", 4.3,
                Arrays.asList("A+", "B+", "O+"), "Campagne de sensibilisation."));
        
        campaigns.add(new Campaign("r4", "Hôpital Cheikh Zaid", "Waqf Islamique",
                "Rue Oum Assalam, Rabat", generateRandomDate(random, dateFormat), "8:30 AM - 3:30 PM", 2.0,
                Arrays.asList("B+", "B-"), "Don de sang spécial."));
        
        campaigns.add(new Campaign("r5", "Centre de Rabat Ville", "Collecte Nationale",
                "Avenue Allal Ben Abdellah, Rabat", generateRandomDate(random, dateFormat), "2:00 PM - 8:00 PM", 1.7,
                Arrays.asList("O+", "A+"), "Don de sang au centre commercial."));

        // MARRAKECH Campaigns
        campaigns.add(new Campaign("m1", "Hôpital Ibn Nafis", "CHU Marrakech",
                "Boulevard Zerktouni, Marrakech", generateRandomDate(random, dateFormat), "8:00 AM - 4:00 PM", 2.4,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-"), "Centre régional de santé."));
        
        campaigns.add(new Campaign("m2", "Medina Clinic", "Ministère de la Santé",
                "Rue de Youssef Ben Tachfine, Marrakech", generateRandomDate(random, dateFormat), "9:00 AM - 5:00 PM", 3.6,
                Arrays.asList("AB+", "AB-", "O+"), "Collecte dans la médina."));
        
        campaigns.add(new Campaign("m3", "Marrakech Mall", "Association Sang Maroc",
                "Avenue Mohammed VI, Marrakech", generateRandomDate(random, dateFormat), "10:00 AM - 8:00 PM", 5.2,
                Arrays.asList("A+", "B+", "O+"), "Campagne publique grande envergure."));
        
        campaigns.add(new Campaign("m4", "Hôpital El Ghassani", "CHU Marrakech",
                "Rue El Ghanim, Marrakech", generateRandomDate(random, dateFormat), "7:00 AM - 3:00 PM", 2.1,
                Arrays.asList("O+", "O-", "A+"), "Don de sang matinal."));

        // TANGER Campaigns
        campaigns.add(new Campaign("t1", "Clinique Internationale", "Groupe Akdital",
                "Avenue Hassan II, Tanger", generateRandomDate(random, dateFormat), "11:00 AM - 7:00 PM", 2.8,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-"), "Don de sang lors de la foire."));
        
        campaigns.add(new Campaign("t2", "Hôpital Mohammed V", "Ministère de la Santé",
                "Avenue Moulay Ismail, Tanger", generateRandomDate(random, dateFormat), "8:00 AM - 4:00 PM", 1.9,
                Arrays.asList("O+", "O-", "A+"), "Collecte hebdomadaire."));
        
        campaigns.add(new Campaign("t3", "Tanger City Mall", "Association Sang pour Tous",
                "Route de Rabat, Tanger", generateRandomDate(random, dateFormat), "10:00 AM - 6:00 PM", 5.5,
                Arrays.asList("A+", "B+", "O+", "AB+"), "Journée portes ouvertes."));
        
        campaigns.add(new Campaign("t4", "Grand Socco", "Croissant Rouge Marocain",
                "Place du 9 Avril, Tanger", generateRandomDate(random, dateFormat), "9:00 AM - 3:00 PM", 3.7,
                Arrays.asList("A+", "A-", "O+", "O-"), "Campagne au coeur de la ville."));

        // FES Campaigns
        campaigns.add(new Campaign("f1", "CHU Hassan II", "Université Sidi Mohammed Ben Abdellah",
                "Route Sidi Harazem, Fès", generateRandomDate(random, dateFormat), "8:00 AM - 4:00 PM", 2.3,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"), "Collecte régionale."));
        
        campaigns.add(new Campaign("f2", "Borj Fez Mall", "Croissant Rouge Marocain",
                "Route de Meknès, Fès", generateRandomDate(random, dateFormat), "10:00 AM - 7:00 PM", 4.8,
                Arrays.asList("O+", "O-", "A-", "B-"), "URGENT: Groupes négatifs recherchés."));
        
        campaigns.add(new Campaign("f3", "Hôpital Ibn Al Khatib", "CHU Fès",
                "Avenue Allal Ben Abdellah, Fès", generateRandomDate(random, dateFormat), "7:00 AM - 2:00 PM", 2.1,
                Arrays.asList("AB+", "AB-", "B+"), "Collecte matinale."));

        // MEKNES Campaigns
        campaigns.add(new Campaign("mk1", "Hôpital Mohammed V", "Ministère de la Santé",
                "Avenue des FAR, Meknès", generateRandomDate(random, dateFormat), "7:00 AM - 3:00 PM", 1.5,
                Arrays.asList("O-", "O+"), "URGENT: Pénurie critique de sang type O."));
        
        campaigns.add(new Campaign("mk2", "Faculté de Médecine", "Université Moulay Ismail",
                "Marjane, Meknès", generateRandomDate(random, dateFormat), "9:00 AM - 5:00 PM", 3.0,
                Arrays.asList("A+", "B+", "AB+", "O+"), "Collecte étudiante."));
        
        campaigns.add(new Campaign("mk3", "Palais des Congrès", "Association Al Amal",
                "Avenue Okba Ibn Nafiaa, Meknès", generateRandomDate(random, dateFormat), "10:00 AM - 4:00 PM", 2.8,
                Arrays.asList("A+", "A-", "B+", "B-"), "Journée solidaire de don."));

        // AGADIR Campaigns
        campaigns.add(new Campaign("a1", "CHU Agadir", "Ministère de la Santé",
                "Avenue Hassan II, Agadir", generateRandomDate(random, dateFormat), "8:00 AM - 4:00 PM", 2.1,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-"), "Collecte hebdomadaire."));
        
        campaigns.add(new Campaign("a2", "Marina Shopping", "Croissant Rouge",
                "Marina d'Agadir", generateRandomDate(random, dateFormat), "10:00 AM - 6:00 PM", 3.5,
                Arrays.asList("A+", "B+", "O+"), "Campagne touristique de don."));
        
        campaigns.add(new Campaign("a3", "Souk El Had", "Association Amal",
                "Boulevard Mohammed V, Agadir", generateRandomDate(random, dateFormat), "2:00 PM - 8:00 PM", 1.8,
                Arrays.asList("O+", "O-", "A+"), "Don de sang au marché central."));

        // OUJDA Campaigns
        campaigns.add(new Campaign("o1", "CHU Mohammed VI Oujda", "Université Mohammed Premier",
                "Route de Sidi Yahya, Oujda", generateRandomDate(random, dateFormat), "8:00 AM - 3:00 PM", 2.0,
                Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"), "Collecte régionale."));
        
        campaigns.add(new Campaign("o2", "Centre Ville Oujda", "Croissant Rouge",
                "Boulevard Mohammed Derfoufi, Oujda", generateRandomDate(random, dateFormat), "11:00 AM - 6:00 PM", 1.5,
                Arrays.asList("A+", "B+", "O+"), "Campagne urbaine de sensibilisation."));

        return campaigns;
    }

    private String generateRandomDate(java.util.Random random, java.text.SimpleDateFormat dateFormat) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int daysToAdd = random.nextInt(7); // 0 to 6 days
        calendar.add(java.util.Calendar.DAY_OF_MONTH, daysToAdd);
        return dateFormat.format(calendar.getTime());
    }
}

