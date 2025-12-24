package com.example.bloodhero.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bloodhero.R;
import com.example.bloodhero.models.Donation;
import com.example.bloodhero.models.User;
import com.example.bloodhero.repository.DonationRepository;
import com.example.bloodhero.repository.UserRepository;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminAnalyticsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvTotalUsers, tvTotalDonations, tvActiveCampaigns;
    private PieChart pieChartBloodTypes;
    private BarChart barChartMonthly;
    private LineChart lineChartUsers;
    
    private UserRepository userRepository;
    private DonationRepository donationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_analytics);

        userRepository = UserRepository.getInstance(this);
        donationRepository = DonationRepository.getInstance(this);
        
        initViews();
        loadStats();
        setupPieChart();
        setupBarChart();
        setupLineChart();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadStats();
        setupBarChart();
        setupPieChart();
        setupLineChart();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalDonations = findViewById(R.id.tvTotalDonations);
        tvActiveCampaigns = findViewById(R.id.tvActiveCampaigns);
        pieChartBloodTypes = findViewById(R.id.pieChartBloodTypes);
        barChartMonthly = findViewById(R.id.barChartMonthly);
        lineChartUsers = findViewById(R.id.lineChartUsers);
    }

    private void loadStats() {
        // Load real user count from SQLite (exclude admin)
        List<User> allUsers = userRepository.getAllUsers();
        int userCount = 0;
        for (User user : allUsers) {
            // Count only non-admin users (admin email is admin@contact.me)
            if (user.getEmail() != null && !user.getEmail().equalsIgnoreCase("admin@contact.me")) {
                userCount++;
            }
        }
        tvTotalUsers.setText(String.valueOf(userCount));
        
        // Load real donation count from donations table
        List<Donation> allDonations = donationRepository.getAllDonations();
        tvTotalDonations.setText(String.valueOf(allDonations.size()));
        
        // Count active campaigns (remaining from December 24 onwards)
        int activeCampaigns = countActiveCampaigns();
        tvActiveCampaigns.setText(String.valueOf(activeCampaigns));
    }
    
    private int countActiveCampaigns() {
        // Hardcoded count based on campaigns still available from Dec 24 onwards
        // Casablanca: 1 (Dec 25), Rabat: 2 (Dec 26, 30), Marrakech: 2 (Dec 24, 28)
        // Tanger: 3 (Dec 25, 29, Jan 2), Fes: 0, Meknes: 2 (Dec 27, 31)
        // Agadir: 1 (Dec 24), Oujda: 0
        return 11;
    }

    private void setupPieChart() {
        // Get actual blood type distribution from registered users
        List<User> allUsers = userRepository.getAllUsers();
        Map<String, Integer> bloodTypeStats = new HashMap<>();
        
        for (User user : allUsers) {
            if (user.getBloodType() != null && !user.getBloodType().isEmpty()) {
                bloodTypeStats.put(user.getBloodType(), 
                    bloodTypeStats.getOrDefault(user.getBloodType(), 0) + 1);
            }
        }
        
        ArrayList<PieEntry> entries = new ArrayList<>();
        
        // Add data from actual users or use mock data if empty
        if (bloodTypeStats.isEmpty() || bloodTypeStats.values().stream().mapToInt(Integer::intValue).sum() == 0) {
            // Mock data for demonstration
            entries.add(new PieEntry(35f, "O+"));
            entries.add(new PieEntry(25f, "A+"));
            entries.add(new PieEntry(15f, "B+"));
            entries.add(new PieEntry(8f, "AB+"));
            entries.add(new PieEntry(7f, "O-"));
            entries.add(new PieEntry(5f, "A-"));
            entries.add(new PieEntry(3f, "B-"));
            entries.add(new PieEntry(2f, "AB-"));
        } else {
            for (Map.Entry<String, Integer> entry : bloodTypeStats.entrySet()) {
                if (entry.getValue() > 0) {
                    entries.add(new PieEntry(entry.getValue(), entry.getKey()));
                }
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "Blood Types");
        dataSet.setColors(getBloodTypeColors());
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new PercentFormatter(pieChartBloodTypes));
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        pieChartBloodTypes.setData(data);
        pieChartBloodTypes.setUsePercentValues(true);
        pieChartBloodTypes.getDescription().setEnabled(false);
        pieChartBloodTypes.setDrawHoleEnabled(true);
        pieChartBloodTypes.setHoleRadius(45f);
        pieChartBloodTypes.setTransparentCircleRadius(50f);
        pieChartBloodTypes.setCenterText("Blood\nTypes");
        pieChartBloodTypes.setCenterTextSize(14f);
        pieChartBloodTypes.setEntryLabelTextSize(12f);
        pieChartBloodTypes.setEntryLabelColor(Color.WHITE);
        
        // Check if dark mode for legend color
        boolean isDarkMode = (getResources().getConfiguration().uiMode 
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK) 
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        
        Legend legend = pieChartBloodTypes.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setWordWrapEnabled(true);
        legend.setTextSize(11f);
        legend.setTextColor(isDarkMode ? Color.WHITE : Color.BLACK);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setFormSize(10f);
        
        pieChartBloodTypes.setRotationEnabled(true);
        pieChartBloodTypes.setHighlightPerTapEnabled(true);
        pieChartBloodTypes.animateY(1200);
        pieChartBloodTypes.invalidate();
    }

    private int[] getBloodTypeColors() {
        return new int[]{
                Color.parseColor("#E53935"), // O+
                Color.parseColor("#D32F2F"), // A+
                Color.parseColor("#C62828"), // B+
                Color.parseColor("#B71C1C"), // AB+
                Color.parseColor("#EF5350"), // O-
                Color.parseColor("#E57373"), // A-
                Color.parseColor("#EF9A9A"), // B-
                Color.parseColor("#FFCDD2")  // AB-
        };
    }

    private void setupBarChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        
        // Get real monthly donation data from SQLite
        List<Donation> allDonations = donationRepository.getAllDonations();
        int[] monthlyCounts = new int[6]; // Last 6 months
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar now = Calendar.getInstance();
        int currentMonth = now.get(Calendar.MONTH);
        int currentYear = now.get(Calendar.YEAR);
        
        Log.d("AdminAnalytics", "Total donations in DB: " + allDonations.size());
        
        // Calculate donations for each of the last 6 months
        for (Donation donation : allDonations) {
            try {
                if (donation.getDate() == null || donation.getDate().isEmpty()) continue;
                
                Calendar donationCal = Calendar.getInstance();
                donationCal.setTime(sdf.parse(donation.getDate()));
                int donationMonth = donationCal.get(Calendar.MONTH);
                int donationYear = donationCal.get(Calendar.YEAR);
                
                Log.d("AdminAnalytics", "Donation date: " + donation.getDate() + 
                      ", month: " + donationMonth + ", year: " + donationYear);
                
                // Get month difference (0 = current month, 1 = last month, etc.)
                int monthDiff = (currentYear - donationYear) * 12 + (currentMonth - donationMonth);
                
                Log.d("AdminAnalytics", "Month diff: " + monthDiff);
                
                if (monthDiff >= 0 && monthDiff < 6) {
                    int index = 5 - monthDiff; // Reverse order for chart display
                    monthlyCounts[index]++;
                    Log.d("AdminAnalytics", "Adding to index " + index + ", new count: " + monthlyCounts[index]);
                }
            } catch (Exception e) {
                Log.e("AdminAnalytics", "Error parsing donation date: " + donation.getDate(), e);
                e.printStackTrace();
            }
        }
        
        // Log final counts
        for (int i = 0; i < 6; i++) {
            Log.d("AdminAnalytics", "Month " + i + " count: " + monthlyCounts[i]);
        }
        
        // Add entries (show actual count even if 0)
        for (int i = 0; i < 6; i++) {
            entries.add(new BarEntry(i, monthlyCounts[i]));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Donations");
        dataSet.setColor(Color.parseColor("#E53935"));
        dataSet.setValueTextSize(12f);
        
        // Check if dark mode for text color
        boolean isDarkMode = (getResources().getConfiguration().uiMode 
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK) 
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        dataSet.setValueTextColor(isDarkMode ? Color.WHITE : Color.BLACK);
        dataSet.setDrawValues(true);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.7f);

        barChartMonthly.setData(data);
        barChartMonthly.getDescription().setEnabled(false);
        barChartMonthly.setFitBars(true);
        barChartMonthly.setDrawGridBackground(false);
        barChartMonthly.setDrawBarShadow(false);
        barChartMonthly.setHighlightFullBarEnabled(true);
        
        // Generate last 6 months labels
        String[] months = getLastSixMonths();
        XAxis xAxis = barChartMonthly.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(11f);
        xAxis.setTextColor(isDarkMode ? Color.WHITE : Color.BLACK);
        
        barChartMonthly.getAxisLeft().setAxisMinimum(0f);
        barChartMonthly.getAxisLeft().setDrawGridLines(true);
        barChartMonthly.getAxisLeft().setGridColor(isDarkMode ? Color.parseColor("#2D2424") : Color.parseColor("#E0E0E0"));
        barChartMonthly.getAxisLeft().setTextColor(isDarkMode ? Color.WHITE : Color.BLACK);
        barChartMonthly.getAxisLeft().setTextSize(11f);
        barChartMonthly.getAxisRight().setEnabled(false);
        
        Legend legend = barChartMonthly.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(isDarkMode ? Color.WHITE : Color.BLACK);
        legend.setTextSize(12f);
        
        barChartMonthly.animateY(1200);
        barChartMonthly.invalidate();
    }

    private void setupLineChart() {
        ArrayList<Entry> entries = new ArrayList<>();
        
        // Get real user registration data from SQLite
        List<User> allUsers = userRepository.getAllUsers();
        int[] monthlyCounts = new int[6]; // Last 6 months
        Calendar now = Calendar.getInstance();
        
        // Count cumulative user registrations by month
        for (User user : allUsers) {
            if (user.getCreatedAt() <= 0) continue; // Skip if no creation timestamp
            
            Calendar userCal = Calendar.getInstance();
            userCal.setTimeInMillis(user.getCreatedAt());
            
            // Get month difference
            int monthDiff = (now.get(Calendar.YEAR) - userCal.get(Calendar.YEAR)) * 12 
                    + (now.get(Calendar.MONTH) - userCal.get(Calendar.MONTH));
            
            if (monthDiff >= 0 && monthDiff < 6) {
                monthlyCounts[5 - monthDiff]++; // Reverse order
            }
        }
        
        // Make cumulative for trend line
        int cumulative = 0;
        for (int i = 0; i < 6; i++) {
            cumulative += monthlyCounts[i];
            entries.add(new Entry(i, cumulative));
        }

        LineDataSet dataSet = new LineDataSet(entries, "New Users");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setCircleColor(Color.parseColor("#4CAF50"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleRadius(2.5f);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setValueTextSize(11f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.2f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#4CAF50"));
        dataSet.setFillAlpha(80);
        
        // Check if dark mode for text color
        boolean isDarkMode = (getResources().getConfiguration().uiMode 
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK) 
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        dataSet.setValueTextColor(isDarkMode ? Color.WHITE : Color.BLACK);
        dataSet.setDrawValues(true);

        LineData data = new LineData(dataSet);

        lineChartUsers.setData(data);
        lineChartUsers.getDescription().setEnabled(false);
        lineChartUsers.setDrawGridBackground(false);
        lineChartUsers.setTouchEnabled(true);
        lineChartUsers.setDragEnabled(true);
        lineChartUsers.setScaleEnabled(false);
        lineChartUsers.setPinchZoom(false);
        
        // Generate last 6 months labels
        String[] months = getLastSixMonths();
        XAxis xAxis = lineChartUsers.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(11f);
        xAxis.setTextColor(isDarkMode ? Color.WHITE : Color.BLACK);
        
        lineChartUsers.getAxisLeft().setAxisMinimum(0f);
        lineChartUsers.getAxisLeft().setDrawGridLines(true);
        lineChartUsers.getAxisLeft().setGridColor(isDarkMode ? Color.parseColor("#2D2424") : Color.parseColor("#E0E0E0"));
        lineChartUsers.getAxisLeft().setTextColor(isDarkMode ? Color.WHITE : Color.BLACK);
        lineChartUsers.getAxisLeft().setTextSize(11f);
        lineChartUsers.getAxisRight().setEnabled(false);
        
        Legend legend = lineChartUsers.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(isDarkMode ? Color.WHITE : Color.BLACK);
        legend.setTextSize(12f);
        legend.setForm(Legend.LegendForm.LINE);
        
        lineChartUsers.animateX(1200);
        lineChartUsers.invalidate();
    }

    private String[] getLastSixMonths() {
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                               "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String[] result = new String[6];
        Calendar cal = Calendar.getInstance();
        
        for (int i = 5; i >= 0; i--) {
            result[5 - i] = monthNames[cal.get(Calendar.MONTH)];
            cal.add(Calendar.MONTH, -1);
        }
        
        return result;
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
    }
}
