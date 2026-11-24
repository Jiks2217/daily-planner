package com.group5.dailyplanner;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReviewFragment extends Fragment {
    private ReviewViewModel reviewViewModel;
    private ActivityViewModel activityViewModel;
    private String currentDate;
    private int currentScore = 0;
    private TextInputEditText etAccomplishment, etChallenges, etImprove;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review, container, false);

        etAccomplishment = view.findViewById(R.id.etAccomplishment);
        etChallenges = view.findViewById(R.id.etChallenges);
        etImprove = view.findViewById(R.id.etImprove);
        MaterialButton btnSave = view.findViewById(R.id.btnSaveReview);
        MaterialButton btnWeekly = view.findViewById(R.id.btnWeeklyRem);
        MaterialButton btnMonthly = view.findViewById(R.id.btnMonthlyRem);
        MaterialButton btnYearly = view.findViewById(R.id.btnYearlyRem);

        currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);
        activityViewModel = new ViewModelProvider(this).get(ActivityViewModel.class);

        // Silently calculate today's score behind the scenes so it saves correctly
        activityViewModel.getAllActivities().observe(getViewLifecycleOwner(), activities -> {
            int total = activities.size();
            int completed = 0;
            for (ActivityItem item : activities) if (item.isCompleted) completed++;
            currentScore = total == 0 ? 0 : (int) (((float) completed / total) * 100);
        });

        // REMINDERS LOGIC
        btnWeekly.setOnClickListener(v -> setReviewReminder("Weekly Review", 7));
        btnMonthly.setOnClickListener(v -> setReviewReminder("Monthly Review", 30));
        btnYearly.setOnClickListener(v -> setReviewReminder("Yearly Review", 365));

        // SAVE LOGIC
        btnSave.setOnClickListener(v -> {
            String acc = etAccomplishment.getText().toString().trim();
            String chal = etChallenges.getText().toString().trim();
            String imp = etImprove.getText().toString().trim();

            if (acc.isEmpty() && chal.isEmpty() && imp.isEmpty()) {
                Toast.makeText(getContext(), "Please write something before saving.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save with ID 0 (Auto-generate). No auto-loading means we just insert a new record each time for the day
            reviewViewModel.saveReview(new ReviewItem(currentDate, currentScore, acc, chal, "", imp), false);
            
            Toast.makeText(getContext(), "Review saved!", Toast.LENGTH_SHORT).show();
            
            // CLEAR THE FORM COMPLETELY
            etAccomplishment.setText(""); 
            etChallenges.setText(""); 
            etImprove.setText("");
            
            // Navigate to History
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_history);
        });

        return view;
    }

    private void setReviewReminder(String title, int daysFromNow) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, daysFromNow);
        calendar.set(Calendar.HOUR_OF_DAY, 19); // 7:00 PM
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        Intent intent = new Intent(requireContext(), AlarmReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("message", "Time to do your " + title.toLowerCase() + " and check your performance!");
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Toast.makeText(getContext(), title + " scheduled for " + daysFromNow + " days from now at 7 PM!", Toast.LENGTH_SHORT).show();
        }
    }
}
