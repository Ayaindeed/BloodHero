package com.example.bloodhero.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bloodhero.R;
import com.example.bloodhero.utils.UserStorage;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminAnalyticsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvTotalUsers, tvTotalDonations, tvActiveCampaigns;
    private PieChart pieChartBloodTypes;
    private BarChart barChartMonthly;
    private LineChart lineChartUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_analytics);

        initViews();
        loadStats();
        setupPieChart();
        setupBarChart();
        setupLineChart();
        setupClickListeners();
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
        // Load real user count
        int userCount = UserStorage.getUserCount(this);
        tvTotalUsers.setText(String.valueOf(Math.max(userCount, 0)));
        
        // Load real donation count from all users
        int totalDonations = UserStorage.getTotalDonations(this);
        tvTotalDonations.setText(String.valueOf(totalDonations));
        
        // Count active campaigns (mock value since campaigns are hardcoded)
        tvActiveCampaigns.setText("12");
    }

    private void setupPieChart() {
        // Get actual blood type distribution from registered users
        Map<String, Integer> bloodTypeStats = UserStorage.getBloodTypeStats(this);
        
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

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(getBloodTypeColors());
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new PercentFormatter(pieChartBloodTypes));

        PieData data = new PieData(dataSet);
        pieChartBloodTypes.setData(data);
        pieChartBloodTypes.setUsePercentValues(true);
        pieChartBloodTypes.getDescription().setEnabled(false);
        pieChartBloodTypes.setHoleRadius(40f);
        pieChartBloodTypes.setTransparentCircleRadius(45f);
        pieChartBloodTypes.setEntryLabelTextSize(11f);
        pieChartBloodTypes.setEntryLabelColor(Color.WHITE);
        
        Legend legend = pieChartBloodTypes.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setWordWrapEnabled(true);
        
        pieChartBloodTypes.animateY(1000);
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
        
        // Mock monthly donations data
        entries.add(new BarEntry(0, 15));
        entries.add(new BarEntry(1, 22));
        entries.add(new BarEntry(2, 18));
        entries.add(new BarEntry(3, 30));
        entries.add(new BarEntry(4, 25));
        entries.add(new BarEntry(5, 47));

        BarDataSet dataSet = new BarDataSet(entries, "Donations");
        dataSet.setColor(Color.parseColor("#D32F2F"));
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        barChartMonthly.setData(data);
        barChartMonthly.getDescription().setEnabled(false);
        barChartMonthly.setFitBars(true);
        
        String[] months = {"Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        XAxis xAxis = barChartMonthly.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        
        barChartMonthly.getAxisLeft().setAxisMinimum(0f);
        barChartMonthly.getAxisRight().setEnabled(false);
        barChartMonthly.getLegend().setEnabled(false);
        
        barChartMonthly.animateY(1000);
        barChartMonthly.invalidate();
    }

    private void setupLineChart() {
        ArrayList<Entry> entries = new ArrayList<>();
        
        // Mock user registration trend
        entries.add(new Entry(0, 5));
        entries.add(new Entry(1, 12));
        entries.add(new Entry(2, 18));
        entries.add(new Entry(3, 25));
        entries.add(new Entry(4, 35));
        entries.add(new Entry(5, 48));

        LineDataSet dataSet = new LineDataSet(entries, "New Users");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setCircleColor(Color.parseColor("#4CAF50"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#4CAF50"));
        dataSet.setFillAlpha(50);

        LineData data = new LineData(dataSet);

        lineChartUsers.setData(data);
        lineChartUsers.getDescription().setEnabled(false);
        
        String[] months = {"Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        XAxis xAxis = lineChartUsers.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        
        lineChartUsers.getAxisLeft().setAxisMinimum(0f);
        lineChartUsers.getAxisRight().setEnabled(false);
        lineChartUsers.getLegend().setEnabled(false);
        
        lineChartUsers.animateX(1000);
        lineChartUsers.invalidate();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
    }
}
