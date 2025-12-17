package com.example.bloodhero.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Helper class for generating QR codes for appointments
 */
public class QRCodeHelper {
    
    private static final int QR_CODE_SIZE = 512;
    
    /**
     * Generate a QR code bitmap from appointment data
     * @param appointmentId The unique appointment ID
     * @return Bitmap of the QR code
     */
    public static Bitmap generateQRCode(String appointmentId) {
        if (appointmentId == null || appointmentId.isEmpty()) {
            return null;
        }
        
        try {
            // Create the QR code content
            String qrContent = "BLOODHERO_APPOINTMENT:" + appointmentId;
            
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 
                    QR_CODE_SIZE, QR_CODE_SIZE);
            
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            
            return bmp;
            
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Generate QR code with custom size
     * @param appointmentId The unique appointment ID
     * @param size Size of the QR code in pixels
     * @return Bitmap of the QR code
     */
    public static Bitmap generateQRCode(String appointmentId, int size) {
        if (appointmentId == null || appointmentId.isEmpty()) {
            return null;
        }
        
        try {
            String qrContent = "BLOODHERO_APPOINTMENT:" + appointmentId;
            
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, size, size);
            
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            
            return bmp;
            
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Extract appointment ID from scanned QR code content
     * @param qrContent The scanned QR code content
     * @return Appointment ID or null if invalid format
     */
    public static String extractAppointmentId(String qrContent) {
        if (qrContent == null || !qrContent.startsWith("BLOODHERO_APPOINTMENT:")) {
            return null;
        }
        
        return qrContent.substring("BLOODHERO_APPOINTMENT:".length());
    }
    
    /**
     * Validate if QR code content is a valid BloodHero appointment QR
     * @param qrContent The scanned QR code content
     * @return true if valid, false otherwise
     */
    public static boolean isValidAppointmentQR(String qrContent) {
        return qrContent != null && qrContent.startsWith("BLOODHERO_APPOINTMENT:");
    }
}
