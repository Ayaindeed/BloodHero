package com.example.bloodhero.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bloodhero.R;
import com.example.bloodhero.models.Appointment;
import com.example.bloodhero.models.Campaign;
import com.example.bloodhero.models.User;
import com.example.bloodhero.repository.AppointmentRepository;
import com.example.bloodhero.utils.UserHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class BookingActivity extends AppCompatActivity {

    private AppointmentRepository appointmentRepository;
    private User currentUser;
    private Campaign selectedCampaign;

    private ImageButton btnBack;
    private TextView tvCampaignName, tvLocation, tvSelectedDate;
    private LinearLayout datePickerButton;
    private GridLayout gridTimeSlots;
    private CheckBox cbHealthy, cbMeal, cbHydrated, cbNoTattoo;
    private EditText etNotes;
    private Button btnConfirmBooking;

    private String selectedDate = "";
    private String selectedTime = "";
    private TextView selectedTimeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        appointmentRepository = AppointmentRepository.getInstance(this);
        currentUser = UserHelper.getCurrentUser(this);

        // Get campaign info from intent
        String campaignId = getIntent().getStringExtra("campaign_id");
        String campaignName = getIntent().getStringExtra("campaign_name");
        String campaignLocation = getIntent().getStringExtra("campaign_location");
        String campaignDate = getIntent().getStringExtra("campaign_date");

        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        populateCampaignInfo(campaignName, campaignLocation);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvCampaignName = findViewById(R.id.tvCampaignName);
        tvLocation = findViewById(R.id.tvLocation);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        datePickerButton = findViewById(R.id.datePickerButton);
        gridTimeSlots = findViewById(R.id.gridTimeSlots);
        cbHealthy = findViewById(R.id.cbHealthy);
        cbMeal = findViewById(R.id.cbMeal);
        cbHydrated = findViewById(R.id.cbHydrated);
        cbNoTattoo = findViewById(R.id.cbNoTattoo);
        etNotes = findViewById(R.id.etNotes);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        datePickerButton.setOnClickListener(v -> showDatePicker());

        // Setup time slot selection
        setupTimeSlots();

        // Setup checklist listeners
        CompoundButton.OnCheckedChangeListener checklistListener = (buttonView, isChecked) -> validateForm();
        cbHealthy.setOnCheckedChangeListener(checklistListener);
        cbMeal.setOnCheckedChangeListener(checklistListener);
        cbHydrated.setOnCheckedChangeListener(checklistListener);
        cbNoTattoo.setOnCheckedChangeListener(checklistListener);

        btnConfirmBooking.setOnClickListener(v -> confirmBooking());
    }

    private void setupTimeSlots() {
        int[] timeSlotIds = {R.id.time09, R.id.time10, R.id.time11, R.id.time12,
                R.id.time14, R.id.time15, R.id.time16, R.id.time17};

        for (int id : timeSlotIds) {
            TextView timeSlot = findViewById(id);
            timeSlot.setOnClickListener(v -> {
                // Deselect previous
                if (selectedTimeView != null) {
                    selectedTimeView.setSelected(false);
                    selectedTimeView.setBackgroundResource(R.drawable.bg_time_slot);
                }

                // Select new
                selectedTimeView = (TextView) v;
                selectedTimeView.setSelected(true);
                selectedTimeView.setBackgroundResource(R.drawable.bg_time_slot_selected);
                selectedTime = selectedTimeView.getText().toString();
                validateForm();
            });
        }
    }

    private void populateCampaignInfo(String name, String location) {
        if (!TextUtils.isEmpty(name)) {
            tvCampaignName.setText(name);
        }
        if (!TextUtils.isEmpty(location)) {
            tvLocation.setText(location);
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.ThemeOverlay_BloodHero_DatePicker,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    selectedDate = sdf.format(selectedCal.getTime());

                    SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                    tvSelectedDate.setText(displayFormat.format(selectedCal.getTime()));
                    tvSelectedDate.setTextColor(getResources().getColor(R.color.text_primary, null));
                    validateForm();
                },
                year,
                month,
                day
        );

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        // Set maximum date to 3 months from now
        calendar.add(Calendar.MONTH, 3);
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());

        datePickerDialog.show();
    }

    private void validateForm() {
        boolean isValid = !TextUtils.isEmpty(selectedDate) &&
                !TextUtils.isEmpty(selectedTime) &&
                cbHealthy.isChecked() &&
                cbMeal.isChecked() &&
                cbHydrated.isChecked() &&
                cbNoTattoo.isChecked();

        btnConfirmBooking.setEnabled(isValid);
    }

    private void confirmBooking() {
        String notes = etNotes.getText().toString().trim();

        // Create appointment
        String appointmentId = UUID.randomUUID().toString();
        Appointment appointment = new Appointment(
                appointmentId,
                currentUser.getId(),
                getIntent().getStringExtra("campaign_id"),
                tvCampaignName.getText().toString(),
                tvLocation.getText().toString(),
                selectedDate,
                selectedTime
        );
        appointment.setStatus(Appointment.Status.SCHEDULED);

        // Save to database
        boolean success = appointmentRepository.createAppointment(appointment);

        if (success) {
            showSuccessDialog();
        } else {
            Toast.makeText(this, "Failed to create appointment. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("Booking Confirmed!")
                .setMessage("Your appointment has been scheduled for " + selectedDate + " at " + selectedTime +
                        ".\n\nPlease arrive 10 minutes early and bring a valid ID.")
                .setPositiveButton("View My Appointments", (dialog, which) -> {
                    finish();
                    // User can navigate to MyAppointmentsActivity from home
                })
                .setNegativeButton("Done", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}
