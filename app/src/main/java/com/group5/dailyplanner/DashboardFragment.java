package com.group5.dailyplanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DashboardFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        TextView tvGreeting = view.findViewById(R.id.tvGreeting);
        TextView tvDate = view.findViewById(R.id.tvDate);
        TextView tvProgress = view.findViewById(R.id.tvProgress);
        TextView tvStreak = view.findViewById(R.id.tvStreak);
        TextView tvStats = view.findViewById(R.id.tvStats);
        MaterialCardView cardProgress = view.findViewById(R.id.cardProgress);
        MaterialCardView cardStreak = view.findViewById(R.id.cardStreak);
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        tvDate.setText(sdf.format(new Date()));

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String name = prefs.getString("userName", "User");
        int hour = new Date().getHours();
        String greeting = (hour < 12) ? "Good morning, " : (hour < 18) ? "Good afternoon, " : "Good evening, ";
        tvGreeting.setText(greeting + name);

        // Make cards clickable
        cardProgress.setOnClickListener(v -> { if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_schedule); });
        cardStreak.setOnClickListener(v -> { if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_history); });

        ActivityViewModel viewModel = new ViewModelProvider(this).get(ActivityViewModel.class);
        viewModel.getAllActivities().observe(getViewLifecycleOwner(), activities -> {
            int total = activities.size();
            int completed = 0;
            for (ActivityItem item : activities) if (item.isCompleted) completed++;

            if (total == 0) {
                tvStats.setText("No activities planned yet. Tap the Schedule tab to add one.");
                tvProgress.setText("0%");
            } else {
                int percent = (int) (((float) completed / total) * 100);
                tvStats.setText("You have " + total + " tasks planned. (" + completed + " completed)");
                tvProgress.setText(percent + "%");
            }
            
            // Mock Streak Logic (In a real app, this checks dates in DB)
            tvStreak.setText(total > 0 && completed == total ? "1 Day" : "0 Days");
        });

        return view;
    }
}
