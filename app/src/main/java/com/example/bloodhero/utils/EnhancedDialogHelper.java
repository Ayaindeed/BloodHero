package com.example.bloodhero.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.bloodhero.R;

public class EnhancedDialogHelper {

    /**
     * Show a generic info dialog with title and message
     */
    public static void showInfoDialog(Context context, String title, String message, String positiveText, Runnable onPositive) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_generic, null);
        
        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        Button btnNegative = dialogView.findViewById(R.id.btnNegative);
        Button btnPositive = dialogView.findViewById(R.id.btnPositive);
        
        tvTitle.setText(title);
        tvMessage.setText(message);
        btnNegative.setVisibility(View.GONE);
        btnPositive.setText(positiveText);
        
        AlertDialog dialog = builder.setView(dialogView).show();
        
        btnPositive.setOnClickListener(v -> {
            dialog.dismiss();
            if (onPositive != null) onPositive.run();
        });
    }

    /**
     * Show a confirmation dialog with title and message
     */
    public static void showConfirmationDialog(Context context, String title, String message, String positiveText, String negativeText, Runnable onPositive) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_confirmation, null);
        
        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        Button btnNegative = dialogView.findViewById(R.id.btnNegative);
        Button btnPositive = dialogView.findViewById(R.id.btnPositive);
        
        tvTitle.setText(title);
        tvMessage.setText(message);
        btnNegative.setText(negativeText);
        btnPositive.setText(positiveText);
        
        AlertDialog dialog = builder.setView(dialogView).show();
        
        btnNegative.setOnClickListener(v -> dialog.dismiss());
        btnPositive.setOnClickListener(v -> {
            dialog.dismiss();
            if (onPositive != null) onPositive.run();
        });
    }

    /**
     * Show a critical action dialog (for delete, logout, etc)
     */
    public static void showCriticalActionDialog(Context context, String title, String message, String positiveText, Runnable onPositive) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_critical_action, null);
        
        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        Button btnNegative = dialogView.findViewById(R.id.btnNegative);
        Button btnPositive = dialogView.findViewById(R.id.btnPositive);
        
        tvTitle.setText(title);
        tvMessage.setText(message);
        btnPositive.setText(positiveText);
        
        AlertDialog dialog = builder.setView(dialogView).show();
        
        btnNegative.setOnClickListener(v -> dialog.dismiss());
        btnPositive.setOnClickListener(v -> {
            dialog.dismiss();
            if (onPositive != null) onPositive.run();
        });
    }
}
