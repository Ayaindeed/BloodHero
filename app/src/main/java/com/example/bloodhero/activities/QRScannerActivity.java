package com.example.bloodhero.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.bloodhero.R;
import com.example.bloodhero.models.Appointment;
import com.example.bloodhero.repository.AppointmentRepository;
import com.example.bloodhero.utils.QRCodeHelper;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

/**
 * Activity for scanning QR codes to check in donors for their appointments
 */
public class QRScannerActivity extends AppCompatActivity {
    
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    
    private DecoratedBarcodeView barcodeView;
    private ImageButton btnBack;
    private AppointmentRepository appointmentRepository;
    private boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        appointmentRepository = AppointmentRepository.getInstance(this);
        
        initViews();
        checkCameraPermission();
    }

    private void initViews() {
        barcodeView = findViewById(R.id.barcodeView);
        btnBack = findViewById(R.id.btnBack);
        
        btnBack.setOnClickListener(v -> finish());
        
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null && !isScanning) {
                    handleScanResult(result.getText());
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
                // Optional: handle possible result points
            }
        });
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        } else {
            startScanning();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR codes",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startScanning() {
        barcodeView.resume();
        isScanning = false;
    }

    private void handleScanResult(String qrContent) {
        isScanning = true;
        barcodeView.pause();
        
        // Validate QR code format
        if (!QRCodeHelper.isValidAppointmentQR(qrContent)) {
            showErrorDialog("Invalid QR Code", 
                "This is not a valid BloodHero appointment QR code.");
            return;
        }
        
        // Extract appointment ID
        String appointmentId = QRCodeHelper.extractAppointmentId(qrContent);
        if (appointmentId == null) {
            showErrorDialog("Invalid QR Code", 
                "Could not read appointment information from QR code.");
            return;
        }
        
        // Get appointment from database
        Appointment appointment = appointmentRepository.getAppointmentById(appointmentId);
        if (appointment == null) {
            showErrorDialog("Appointment Not Found", 
                "No appointment found with this QR code. It may have been cancelled.");
            return;
        }
        
        // Check appointment status
        if (appointment.getStatus() == Appointment.Status.CANCELLED) {
            showErrorDialog("Appointment Cancelled", 
                "This appointment has been cancelled and cannot be checked in.");
            return;
        }
        
        if (appointment.getStatus() == Appointment.Status.COMPLETED) {
            showErrorDialog("Already Completed", 
                "This donation has already been completed.");
            return;
        }
        
        if (appointment.getStatus() == Appointment.Status.CHECKED_IN) {
            showErrorDialog("Already Checked In", 
                "This donor has already been checked in.");
            return;
        }
        
        // Check in the appointment
        checkInAppointment(appointment);
    }

    private void checkInAppointment(Appointment appointment) {
        // Update appointment status
        appointment.checkIn();
        boolean success = appointmentRepository.updateAppointment(appointment);
        
        if (success) {
            showSuccessDialog(appointment);
        } else {
            showErrorDialog("Check-in Failed", 
                "Could not update appointment status. Please try again.");
        }
    }

    private void showSuccessDialog(Appointment appointment) {
        new AlertDialog.Builder(this)
                .setTitle("✓ Check-in Successful")
                .setMessage("Donor checked in successfully!\n\n" +
                        "Campaign: " + appointment.getCampaignName() + "\n" +
                        "Date: " + appointment.getDate() + "\n" +
                        "Time: " + appointment.getTime())
                .setPositiveButton("Continue Scanning", (dialog, which) -> {
                    isScanning = false;
                    barcodeView.resume();
                })
                .setNegativeButton("Close", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Try Again", (dialog, which) -> {
                    isScanning = false;
                    barcodeView.resume();
                })
                .setNegativeButton("Cancel", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeView != null && !isScanning) {
            barcodeView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }
}
