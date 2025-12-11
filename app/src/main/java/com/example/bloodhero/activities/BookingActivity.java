package com.example.bloodhero.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodhero.HomeActivity;
import com.example.bloodhero.R;
import com.example.bloodhero.adapters.DateSlotAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BloodHeroPrefs";

    private ImageButton btnBack;
    private TextView tvCampaignName, tvLocation;
    private RecyclerView rvDates;
    private CheckBox cbHealthy, cbMeal, cbHydrated, cbNoTattoo;
    private EditText etNotes;
    private Button btnConfirmBooking;

    private TextView[] timeSlots;
    private String selectedTime = null;
    private Date selectedDate = null;
    private DateSlotAdapter dateSlotAdapter;

    private String campaignId;
    private String campaignName;
    private String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        // Get campaign data from intent
        campaignId = getIntent().getStringExtra("campaign_id");
        campaignName = getIntent().getStringExtra("campaign_name");
        location = getIntent().getStringExtra("location");

        initViews();
        setupDateSelector();
        setupTimeSlots();
        setupChecklistListeners();
        setupClickListeners();

        // Set campaign info
        if (campaignName != null) {
            tvCampaignName.setText(campaignName);
        }
        if (location != null) {
            tvLocation.setText(location);
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvCampaignName = findViewById(R.id.tvCampaignName);
        tvLocation = findViewById(R.id.tvLocation);
        rvDates = findViewById(R.id.rvDates);
        cbHealthy = findViewById(R.id.cbHealthy);
        cbMeal = findViewById(R.id.cbMeal);
        cbHydrated = findViewById(R.id.cbHydrated);
        cbNoTattoo = findViewById(R.id.cbNoTattoo);
        etNotes = findViewById(R.id.etNotes);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);

        // Initialize time slot views
        timeSlots = new TextView[]{
                findViewById(R.id.time09),
                findViewById(R.id.time10),
                findViewById(R.id.time11),
                findViewById(R.id.time12),
                findViewById(R.id.time14),
                findViewById(R.id.time15),
                findViewById(R.id.time16),
                findViewById(R.id.time17)
        };
    }

    private void setupDateSelector() {
        List<Date> dates = generateNextDays(7);
        dateSlotAdapter = new DateSlotAdapter(dates, (date, position) -> {
            selectedDate = date;
            updateBookingButton();
        });
        rvDates.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvDates.setAdapter(dateSlotAdapter);
    }

    private List<Date> generateNextDays(int count) {
        List<Date> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < count; i++) {
            dates.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return dates;
    }

    private void setupTimeSlots() {
        for (TextView timeSlot : timeSlots) {
            timeSlot.setOnClickListener(v -> {
                // Deselect all
                for (TextView slot : timeSlots) {
                    slot.setSelected(false);
                    slot.setTextColor(getColor(R.color.text_primary));
                }
                // Select clicked
                timeSlot.setSelected(true);
                timeSlot.setTextColor(getColor(android.R.color.white));
                selectedTime = timeSlot.getText().toString();
                updateBookingButton();
            });
        }
    }

    private void setupChecklistListeners() {
        View.OnClickListener checklistListener = v -> updateBookingButton();
        cbHealthy.setOnClickListener(checklistListener);
        cbMeal.setOnClickListener(checklistListener);
        cbHydrated.setOnClickListener(checklistListener);
        cbNoTattoo.setOnClickListener(checklistListener);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnConfirmBooking.setOnClickListener(v -> confirmBooking());
    }

    private void updateBookingButton() {
        boolean allChecklistCompleted = cbHealthy.isChecked() && cbMeal.isChecked() 
                && cbHydrated.isChecked() && cbNoTattoo.isChecked();
        boolean dateSelected = selectedDate != null;
        boolean timeSelected = selectedTime != null;

        btnConfirmBooking.setEnabled(allChecklistCompleted && dateSelected && timeSelected);
    }

    private void confirmBooking() {
        // Save appointment data
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String appointmentDate = dateFormat.format(selectedDate);

        // In a real app, this would be saved to a database
        editor.putString("last_appointment_campaign", campaignName);
        editor.putString("last_appointment_date", appointmentDate);
        editor.putString("last_appointment_time", selectedTime);
        editor.putString("last_appointment_location", location);
        editor.apply();

        Toast.makeText(this, "Appointment booked successfully!", Toast.LENGTH_LONG).show();

        // Navigate back to home
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}