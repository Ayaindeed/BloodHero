package com.example.bloodhero.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Email Helper for sending verification codes
 * Uses SMTP to send emails for password recovery
 * Note: For production, use Firebase Cloud Functions or a dedicated email service
 */
public class EmailHelper {
    private static final String TAG = "EmailHelper";
    
    // Gmail SMTP Configuration
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    
    // Gmail credentials - Replace with your values
    private static final String FROM_EMAIL = "elmehdi.eyr@gmail.com"; // Your Gmail address
    private static final String APP_PASSWORD = "eyr@@@1415"; // Your 16-character App Password (from Google Account)
    
    private Context context;
    private ScheduledExecutorService executor;
    
    public EmailHelper(Context context) {
        this.context = context;
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }
    
    /**
     * Send verification code via email
     * @param toEmail Recipient email address
     * @param userName User's name
     * @param verificationCode 6-digit verification code
     * @param onComplete Callback when email is sent (success/failure)
     */
    public void sendVerificationCode(String toEmail, String userName, String verificationCode, 
                                     EmailCallback onComplete) {
        executor.execute(() -> {
            try {
                // Setup email properties
                Properties props = new Properties();
                props.put("mail.smtp.host", SMTP_HOST);
                props.put("mail.smtp.port", SMTP_PORT);
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.starttls.required", "true");
                props.put("mail.smtp.ssl.protocols", "TLSv1.2");
                props.put("mail.smtp.connectiontimeout", "5000");
                props.put("mail.smtp.timeout", "5000");
                
                // Create session with authentication
                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
                    }
                });
                
                // Create email message
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(FROM_EMAIL));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject("BloodHero - Password Recovery Code");
                
                // Email content (HTML formatted)
                String emailBody = buildEmailBody(userName, verificationCode);
                message.setContent(emailBody, "text/html; charset=utf-8");
                
                // Send email
                Transport.send(message);
                
                Log.d(TAG, "Email sent successfully to: " + toEmail);
                if (onComplete != null) {
                    onComplete.onSuccess("Verification code sent to " + toEmail);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to send email", e);
                if (onComplete != null) {
                    onComplete.onFailure("Failed to send email: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Build HTML email body
     */
    private String buildEmailBody(String userName, String verificationCode) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; background-color: #f5f5f5;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px;'>" +
                
                "<div style='text-align: center; margin-bottom: 30px;'>" +
                "<h1 style='color: #D32F2F; margin: 0;'>BloodHero</h1>" +
                "<p style='color: #999; margin: 5px 0 0 0;'>Blood Donation Management</p>" +
                "</div>" +
                
                "<h2 style='color: #333;'>Password Recovery Code</h2>" +
                "<p style='color: #666; line-height: 1.6;'>Hi " + userName + ",</p>" +
                
                "<p style='color: #666; line-height: 1.6;'>" +
                "We received a request to reset your password. Use the code below to proceed with password recovery. " +
                "This code will expire in 15 minutes." +
                "</p>" +
                
                "<div style='background-color: #f9f9f9; padding: 20px; border-radius: 8px; text-align: center; margin: 30px 0;'>" +
                "<p style='color: #999; margin: 0 0 10px 0; font-size: 14px;'>YOUR VERIFICATION CODE</p>" +
                "<p style='color: #D32F2F; font-size: 36px; font-weight: bold; margin: 0; letter-spacing: 5px;'>" +
                verificationCode +
                "</p>" +
                "</div>" +
                
                "<p style='color: #666; line-height: 1.6;'>" +
                "If you didn't request a password reset, please ignore this email. " +
                "Do not share this code with anyone." +
                "</p>" +
                
                "<hr style='border: none; border-top: 1px solid #eee; margin: 30px 0;'>" +
                
                "<p style='color: #999; font-size: 13px; text-align: center;'>" +
                "This is an automated message from BloodHero. Please do not reply to this email." +
                "</p>" +
                
                "</div>" +
                "</body>" +
                "</html>";
    }
    
    /**
     * Callback interface for email operations
     */
    public interface EmailCallback {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }
    
    /**
     * Shutdown executor service
     */
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
