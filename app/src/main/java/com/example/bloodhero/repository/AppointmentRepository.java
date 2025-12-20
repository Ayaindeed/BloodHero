package com.example.bloodhero.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.bloodhero.database.BloodHeroDatabaseHelper;
import com.example.bloodhero.models.Appointment;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for Appointment data operations
 * Provides clean interface to SQLite database
 */
public class AppointmentRepository {
    private final BloodHeroDatabaseHelper dbHelper;
    private static AppointmentRepository instance;

    private AppointmentRepository(Context context) {
        dbHelper = BloodHeroDatabaseHelper.getInstance(context);
    }

    public static synchronized AppointmentRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AppointmentRepository(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Create a new appointment
     */
    public boolean createAppointment(Appointment appointment) {
        return dbHelper.insertAppointment(appointment) != -1;
    }

    /**
     * Get all appointments for a specific user
     */
    public List<Appointment> getAppointmentsByUserId(String userId) {
        return dbHelper.getAppointmentsByUserId(userId);
    }

    /**
     * Get all appointments (admin)
     */
    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM appointments ORDER BY date DESC", null);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                appointments.add(cursorToAppointment(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return appointments;
    }

    /**
     * Get appointments by status
     */
    public List<Appointment> getAppointmentsByStatus(String status) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM appointments WHERE status = ? ORDER BY date DESC", 
                                   new String[]{status});
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                appointments.add(cursorToAppointment(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return appointments;
    }

    /**
     * Update appointment status
     */
    public boolean updateStatus(String appointmentId, Appointment.Status status) {
        return dbHelper.updateAppointmentStatus(appointmentId, status) > 0;
    }
    
    /**
     * Assign appointment to bed
     */
    public boolean assignToBed(String appointmentId, int bedNumber) {
        return dbHelper.assignAppointmentToBed(appointmentId, bedNumber) > 0;
    }
    
    /**
     * Set appointment to pending verification with code
     */
    public boolean setPendingVerification(String appointmentId, String verificationCode) {
        return dbHelper.setAppointmentPendingVerification(appointmentId, verificationCode) > 0;
    }
    
    /**
     * Get appointments by status
     */
    public List<Appointment> getAppointmentsByStatus(Appointment.Status status) {
        return dbHelper.getAppointmentsByStatus(status);
    }
    
    /**
     * Update entire appointment object
     */
    public boolean updateAppointment(Appointment appointment) {
        return dbHelper.updateAppointmentStatus(appointment.getId(), appointment.getStatus()) > 0;
    }

    /**
     * Get appointment by ID
     */
    public Appointment getAppointmentById(String appointmentId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM appointments WHERE id = ?", 
                                   new String[]{appointmentId});
        
        Appointment appointment = null;
        if (cursor != null && cursor.moveToFirst()) {
            appointment = cursorToAppointment(cursor);
            cursor.close();
        }
        
        return appointment;
    }

    /**
     * Delete an appointment
     */
    public boolean deleteAppointment(String appointmentId) {
        return dbHelper.deleteAppointment(appointmentId) > 0;
    }

    private Appointment cursorToAppointment(Cursor cursor) {
        Appointment appointment = new Appointment(
                cursor.getString(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("user_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("campaign_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("campaign_name")),
                cursor.getString(cursor.getColumnIndexOrThrow("location")),
                cursor.getString(cursor.getColumnIndexOrThrow("date")),
                cursor.getString(cursor.getColumnIndexOrThrow("time_slot")),
                Appointment.Status.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("status")))
        );
        
        // Set additional fields
        int checkedInAtIndex = cursor.getColumnIndex("checked_in_at");
        if (checkedInAtIndex != -1 && !cursor.isNull(checkedInAtIndex)) {
            appointment.setCheckedInAt(cursor.getLong(checkedInAtIndex));
        }
        
        return appointment;
    }
}
