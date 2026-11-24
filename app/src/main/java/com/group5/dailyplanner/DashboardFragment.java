package com.group5.dailyplanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        TextView tvGreeting = view.findViewById(R.id.tvGreeting);
        TextView tvDate = view.findViewById(R.id.tvDate);
        TextView tvProgress = view.findViewById(R.id.tvProgress);
        TextView tvStreak = view.findViewById(R.id.tvStreak);
        TextView tvWeeklyAvg = view.findViewById(R.id.tvWeeklyAvg);
        TextView tvTopFocus = view.findViewById(R.id.tvTopFocus);
        TextView tvNextUp = view.findViewById(R.id.tvNextUp);
        TextView tvReviewStatus = view.findViewById(R.id.tvReviewStatus);
        
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);

        // Setup Header
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        tvDate.setText(sdf.format(new Date()));
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String name = prefs.getString("userName", "User");
        int hour = new Date().getHours();
        String greeting = (hour < 12) ? "Good morning, " : (hour < 18) ? "Good afternoon, " : "Good evening, ";
        tvGreeting.setText(greeting + name);

        // Card Click Listeners
        view.findViewById(R.id.cardProgress).setOnClickListener(v -> bottomNav.setSelectedItemId(R.id.nav_schedule));
        view.findViewById(R.id.cardTopFocus).setOnClickListener(v -> bottomNav.setSelectedItemId(R.id.nav_schedule));
        view.findViewById(R.id.cardNextUp).setOnClickListener(v -> bottomNav.setSelectedItemId(R.id.nav_schedule));
        view.findViewById(R.id.cardReview).setOnClickListener(v -> bottomNav.setSelectedItemId(R.id.nav_review));
        view.findViewById(R.id.cardStreak).setOnClickListener(v -> bottomNav.setSelectedItemId(R.id.nav_history));
        view.findViewById(R.id.cardWeeklyAvg).setOnClickListener(v -> bottomNav.setSelectedItemId(R.id.nav_history));

        ActivityViewModel activityViewModel = new ViewModelProvider(this).get(ActivityViewModel.class);
        ReviewViewModel reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);
        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Observe Tasks (Progress, Focus, Next Up)
        activityViewModel.getAllActivities().observe(getViewLifecycleOwner(), activities -> {
            int total = activities.size();
            int completed = 0;
            Map<String, Integer> categoryCounts = new HashMap<>();
            List<ActivityItem> pending = new ArrayList<>();

            for (ActivityItem item : activities) {
                if (item.isCompleted) {
                    completed++;
                    categoryCounts.put(item.category, categoryCounts.getOrDefault(item.category, 0) + 1);
                } else {
                    pending.add(item);
                }
            }

            if (total > 0) {
                tvProgress.setText((int)(((float)completed / total) * 100) + "%");
            } else {
                tvProgress.setText("0%");
            }

            // Top Focus
            String topFocus = "None";
            int max = 0;
            for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
                if (entry.getValue() > max) { max = entry.getValue(); topFocus = entry.getKey(); }
            }
            tvTopFocus.setText(topFocus);

            // Next Up (Sort pending by parsed time)
            if (pending.isEmpty()) {
                tvNextUp.setText("All Done");
            } else {
                pending.sort((a, b) -> {
                    try {
                        SimpleDateFormat timeSdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                        return timeSdf.parse(a.startTime).compareTo(timeSdf.parse(b.startTime));
                    } catch (Exception e) { return 0; }
                });
                tvNextUp.setText(pending.get(0).title);
            }
        });

        // Observe Reviews (Streak, Weekly Avg, Status)
        reviewViewModel.getAllReviews().observe(getViewLifecycleOwner(), reviews -> {
            boolean reviewedToday = false;
            int totalScore = 0;
            int weeklyCount = 0;
            
            Calendar cutoff = Calendar.getInstance();
            cutoff.add(Calendar.DAY_OF_YEAR, -7);
            String cutoffStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cutoff.getTime());

            for (ReviewItem item : reviews) {
                if (item.date.equals(todayStr)) reviewedToday = true;
                if (item.date.compareTo(cutoffStr) >= 0) {
                    totalScore += item.score;
                    weeklyCount++;
                }
            }

            // Review Status
            if (reviewedToday) {
                tvReviewStatus.setText("Completed");
                tvReviewStatus.setTextColor(Color.parseColor("#2E7D32"));
            } else {
                tvReviewStatus.setText("Pending");
                tvReviewStatus.setTextColor(Color.parseColor("#C62828"));
            }

            // Weekly Avg
            if (weeklyCount > 0) {
                tvWeeklyAvg.setText((totalScore / weeklyCount) + "%");
            } else {
                tvWeeklyAvg.setText("--");
            }

            // Streak Calculation (Consecutive days with score >= 80)
            int streak = 0;
            // For a real app, you'd iterate day by day, but this is a good heuristic
            for (ReviewItem item : reviews) {
                if (item.score >= 80) streak++;
                else break;
            }
            tvStreak.setText(streak + " Days");
        });

        return view;
    }
}
