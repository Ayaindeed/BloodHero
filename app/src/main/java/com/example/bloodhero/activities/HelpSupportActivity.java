package com.example.bloodhero.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bloodhero.R;

public class HelpSupportActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private LinearLayout menuFaq, menuContactUs, menuFeedback, menuAbout;
    private LinearLayout faqSection;
    
    // FAQ items
    private LinearLayout faqItem1, faqItem2, faqItem3, faqItem4, faqItem5;
    private TextView faqAnswer1, faqAnswer2, faqAnswer3, faqAnswer4, faqAnswer5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        menuFaq = findViewById(R.id.menuFaq);
        menuContactUs = findViewById(R.id.menuContactUs);
        menuFeedback = findViewById(R.id.menuFeedback);
        menuAbout = findViewById(R.id.menuAbout);
        faqSection = findViewById(R.id.faqSection);
        
        // FAQ items
        faqItem1 = findViewById(R.id.faqItem1);
        faqItem2 = findViewById(R.id.faqItem2);
        faqItem3 = findViewById(R.id.faqItem3);
        faqItem4 = findViewById(R.id.faqItem4);
        faqItem5 = findViewById(R.id.faqItem5);
        
        faqAnswer1 = findViewById(R.id.faqAnswer1);
        faqAnswer2 = findViewById(R.id.faqAnswer2);
        faqAnswer3 = findViewById(R.id.faqAnswer3);
        faqAnswer4 = findViewById(R.id.faqAnswer4);
        faqAnswer5 = findViewById(R.id.faqAnswer5);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        menuFaq.setOnClickListener(v -> {
            // Toggle FAQ section visibility
            if (faqSection.getVisibility() == View.VISIBLE) {
                faqSection.setVisibility(View.GONE);
            } else {
                faqSection.setVisibility(View.VISIBLE);
            }
        });

        menuContactUs.setOnClickListener(v -> {
            // Open email client
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@bloodhero.ma"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "BloodHero Support Request");
            try {
                startActivity(Intent.createChooser(intent, "Contact Support"));
            } catch (Exception e) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });

        menuFeedback.setOnClickListener(v -> {
            // Open email for feedback
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:feedback@bloodhero.ma"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "BloodHero Feedback");
            try {
                startActivity(Intent.createChooser(intent, "Send Feedback"));
            } catch (Exception e) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });

        menuAbout.setOnClickListener(v -> {
            // Show enhanced about dialog
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_about_bloodhero, null);
            androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create();
            
            // Make dialog background transparent for custom design
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
            
            // Find and setup close button
            com.google.android.material.button.MaterialButton btnClose = dialogView.findViewById(R.id.btnClose);
            if (btnClose != null) {
                btnClose.setOnClickListener(closeView -> dialog.dismiss());
            }
            
            dialog.show();
        });

        // FAQ toggle listeners
        setupFaqToggle(faqItem1, faqAnswer1);
        setupFaqToggle(faqItem2, faqAnswer2);
        setupFaqToggle(faqItem3, faqAnswer3);
        setupFaqToggle(faqItem4, faqAnswer4);
        setupFaqToggle(faqItem5, faqAnswer5);
    }

    private void setupFaqToggle(LinearLayout faqItem, TextView answer) {
        faqItem.setOnClickListener(v -> {
            if (answer.getVisibility() == View.VISIBLE) {
                answer.setVisibility(View.GONE);
            } else {
                answer.setVisibility(View.VISIBLE);
            }
        });
    }
}
