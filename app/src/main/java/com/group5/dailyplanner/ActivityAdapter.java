package com.group5.dailyplanner;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {
    private List<ActivityItem> activities = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onCheckBoxClick(ActivityItem item, boolean isChecked);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setActivities(List<ActivityItem> activities) {
        this.activities = activities;
        notifyDataSetChanged();
    }

    // Needed for Swipe-to-Delete
    public ActivityItem getActivityAt(int position) {
        return activities.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityItem current = activities.get(position);
        holder.tvTitle.setText(current.title);
        holder.tvDetails.setText(current.startTime + " - " + current.endTime + " • " + current.category);
        
        holder.cbComplete.setOnCheckedChangeListener(null);
        holder.cbComplete.setChecked(current.isCompleted);

        if (current.isCompleted) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setTextColor(Color.parseColor("#999999"));
            holder.tvDetails.setTextColor(Color.parseColor("#BBBBBB"));
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvTitle.setTextColor(Color.parseColor("#1A1A1A"));
            holder.tvDetails.setTextColor(Color.parseColor("#666666"));
        }

        holder.cbComplete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCheckBoxClick(current, holder.cbComplete.isChecked());
            }
        });
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDetails;
        CheckBox cbComplete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDetails = itemView.findViewById(R.id.tvTaskDetails);
            cbComplete = itemView.findViewById(R.id.cbComplete);
        }
    }
}
