package com.group5.dailyplanner;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReviewFragment extends Fragment {
    private ReviewViewModel reviewViewModel;
    private ActivityViewModel activityViewModel;
    private ReviewItem existingReview;
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
        
        TextView tvCoachSummary = view.findViewById(R.id.tvCoachSummary);
        TextView tvCoachCategory = view.findViewById(R.id.tvCoachCategory);
        TextView tvAiJudgment = view.findViewById(R.id.tvAiJudgment);

        currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);
        activityViewModel = new ViewModelProvider(this).get(ActivityViewModel.class);

        // AI Logic (Same as before)
        activityViewModel.getAllActivities().observe(getViewLifecycleOwner(), activities -> {
            int total = activities.size();
            if (total == 0) {
                tvCoachSummary.setText("You completed 0 of 0 tasks today.");
                tvCoachCategory.setText("No tasks scheduled.");
                tvAiJudgment.setText("You didn't schedule any tasks today. A blank canvas! Take a moment to plan for tomorrow.");
                tvAiJudgment.setTextColor(Color.parseColor("#B3E5FC"));
                currentScore = 0; return;
            }
            int completed = 0;
            Map<String, Integer> scheduledCategories = new HashMap<>();
            Map<String, Integer> completedCategories = new HashMap<>();
            for (ActivityItem item : activities) {
                scheduledCategories.put(item.category, scheduledCategories.getOrDefault(item.category, 0) + 1);
                if (item.isCompleted) { completed++; completedCategories.put(item.category, completedCategories.getOrDefault(item.category, 0) + 1); }
            }
            String dominantFocus = "None"; int maxCompleted = 0;
            for (Map.Entry<String, Integer> entry : completedCategories.entrySet()) { if (entry.getValue() > maxCompleted) { maxCompleted = entry.getValue(); dominantFocus = entry.getKey(); } }
            String neglectedArea = ""; int maxMissed = 0;
            for (Map.Entry<String, Integer> entry : scheduledCategories.entrySet()) { int missed = entry.getValue() - completedCategories.getOrDefault(entry.getKey(), 0); if (missed > maxMissed) { maxMissed = missed; neglectedArea = entry.getKey(); } }

            currentScore = (int) (((float) completed / total) * 100);
            tvCoachSummary.setText(String.format(Locale.getDefault(), "You completed %d of %d tasks (%d%%).", completed, total, currentScore));
            if (!dominantFocus.equals("None")) tvCoachCategory.setText("Most focus on: " + dominantFocus);

            String judgment; int color;
            if (completed == total) { color = Color.parseColor("#81C784"); judgment = "Perfect score! You completed everything you set out to do today."; }
            else if (currentScore >= 70) { color = Color.parseColor("#81C784"); judgment = "Strong performance. You nailed " + dominantFocus + ", but fell short on " + neglectedArea + "."; }
            else if (currentScore >= 30) { color = Color.parseColor("#FFB74D"); judgment = "You made progress. " + neglectedArea + " was left behind. Try time-blocking tomorrow."; }
            else { color = Color.parseColor("#E57373"); judgment = "A tough day. Only " + completed + " out of " + total + " tasks done. Reflect and try again tomorrow."; }
            tvAiJudgment.setText(judgment); tvAiJudgment.setTextColor(color);
        });

        reviewViewModel.getReview(currentDate, item -> {
            if (item != null) {
                existingReview = item;
                requireActivity().runOnUiThread(() -> { etAccomplishment.setText(item.accomplishment); etChallenges.setText(item.challenges); etImprove.setText(item.improve); });
            }
        });

        // REMINDERS LOGIC
        btnWeekly.setOnClickListener(v -> setReviewReminder("Weekly Review", 7, 19, 0)); // 7 days from now, 7:00 PM
        btnMonthly.setOnClickListener(v -> setReviewReminder("Monthly Review", 30, 19, 0)); // 30 days, 7:00 PM
        btnYearly.setOnClickListener(v -> setReviewReminder("Yearly Review", 365, 19, 0)); // 365 days, 7:00 PM

        // SAVE LOGIC
        btnSave.setOnClickListener(v -> {
            String acc = etAccomplishment.getText().toString().trim();
            String chal = etChallenges.getText().toString().trim();
            String imp = etImprove.getText().toString().trim();

            if (acc.isEmpty() && chal.isEmpty() && imp.isEmpty()) {
                Toast.makeText(getContext(), "Please write something before saving.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (existingReview == null) reviewViewModel.saveReview(new ReviewItem(currentDate, currentScore, acc, chal, "", imp), false);
            else { existingReview.score = currentScore; existingReview.accomplishment = acc; existingReview.challenges = chal; existingReview.improve = imp; reviewViewModel.saveReview(existingReview, true); }
            
            Toast.makeText(getContext(), "Review saved!", Toast.LENGTH_SHORT).show();
            
            // CLEAR THE FORM
            etAccomplishment.setText(""); etChallenges.setText(""); etImprove.setText("");
            
            // Navigate to History
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_history);
        });

        return view;
    }

    private void setReviewReminder(String title, int daysFromNow, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, daysFromNow);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        Intent intent = new Intent(requireContext(), AlarmReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("message", "Time to do your " + title.toLowerCase() + " and check your performance!");
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Toast.makeText(getContext(), title + " scheduled for " + daysFromNow + " days from now!", Toast.LENGTH_SHORT).show();
        }
    }
}
