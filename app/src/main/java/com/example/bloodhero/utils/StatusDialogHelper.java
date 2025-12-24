package com.example.bloodhero.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.example.bloodhero.R;

/**
 * Reusable status dialog with a card-like layout for success/info/warning/error messages.
 */
public class StatusDialogHelper {

    public enum StatusType {
        SUCCESS,
        INFO,
        WARNING,
        ERROR
    }

    public static void showStatusDialog(Context context,
                                        StatusType type,
                                        String title,
                                        String message,
                                        String actionText,
                                        Runnable onAction) {
        if (context == null) return;

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_status_card);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvBadge = dialog.findViewById(R.id.tvStatusBadge);
        TextView tvTitle = dialog.findViewById(R.id.tvStatusTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvStatusMessage);
        TextView btnPrimary = dialog.findViewById(R.id.btnPrimary);
        TextView btnSecondary = dialog.findViewById(R.id.btnSecondary);

        tvTitle.setText(title);
        tvMessage.setText(message);

        // Style by type
        switch (type) {
            case SUCCESS:
                tvBadge.setText("Congrats");
                tvBadge.setBackgroundResource(R.drawable.bg_status_success);
                break;
            case WARNING:
                tvBadge.setText("Wait");
                tvBadge.setBackgroundResource(R.drawable.bg_status_warning);
                break;
            case ERROR:
                tvBadge.setText("Oops");
                tvBadge.setBackgroundResource(R.drawable.bg_status_error);
                break;
            default:
                tvBadge.setText("Info");
                tvBadge.setBackgroundResource(R.drawable.bg_status_info);
        }

        btnPrimary.setText(actionText == null ? "OK" : actionText);
        btnPrimary.setOnClickListener(v -> {
            dialog.dismiss();
            if (onAction != null) onAction.run();
        });

        btnSecondary.setOnClickListener(v -> dialog.dismiss());

        dialog.setCancelable(true);
        dialog.show();
    }
}
