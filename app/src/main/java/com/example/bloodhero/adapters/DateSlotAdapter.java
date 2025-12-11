package com.example.bloodhero.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodhero.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateSlotAdapter extends RecyclerView.Adapter<DateSlotAdapter.DateSlotViewHolder> {

    private List<Date> dateList;
    private int selectedPosition = -1;
    private OnDateSelectedListener listener;

    public interface OnDateSelectedListener {
        void onDateSelected(Date date, int position);
    }

    public DateSlotAdapter(List<Date> dateList, OnDateSelectedListener listener) {
        this.dateList = dateList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DateSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_date_slot, parent, false);
        return new DateSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateSlotViewHolder holder, int position) {
        Date date = dateList.get(position);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dayNumberFormat = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());

        holder.tvDayName.setText(dayNameFormat.format(date));
        holder.tvDayNumber.setText(dayNumberFormat.format(date));
        holder.tvMonth.setText(monthFormat.format(date));

        boolean isSelected = position == selectedPosition;
        holder.dateCard.setSelected(isSelected);
        
        if (isSelected) {
            holder.dateCard.setCardBackgroundColor(
                    holder.itemView.getContext().getColor(R.color.primary));
            holder.tvDayName.setTextColor(
                    holder.itemView.getContext().getColor(android.R.color.white));
            holder.tvDayNumber.setTextColor(
                    holder.itemView.getContext().getColor(android.R.color.white));
            holder.tvMonth.setTextColor(
                    holder.itemView.getContext().getColor(R.color.white_70));
        } else {
            holder.dateCard.setCardBackgroundColor(
                    holder.itemView.getContext().getColor(R.color.surface));
            holder.tvDayName.setTextColor(
                    holder.itemView.getContext().getColor(R.color.text_secondary));
            holder.tvDayNumber.setTextColor(
                    holder.itemView.getContext().getColor(R.color.text_primary));
            holder.tvMonth.setTextColor(
                    holder.itemView.getContext().getColor(R.color.text_secondary));
        }

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onDateSelected(date, selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dateList != null ? dateList.size() : 0;
    }

    public Date getSelectedDate() {
        if (selectedPosition >= 0 && selectedPosition < dateList.size()) {
            return dateList.get(selectedPosition);
        }
        return null;
    }

    static class DateSlotViewHolder extends RecyclerView.ViewHolder {
        CardView dateCard;
        TextView tvDayName;
        TextView tvDayNumber;
        TextView tvMonth;

        DateSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            dateCard = itemView.findViewById(R.id.dateCard);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            tvMonth = itemView.findViewById(R.id.tvMonth);
        }
    }
}