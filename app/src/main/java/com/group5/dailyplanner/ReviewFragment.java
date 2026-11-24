package com.group5.dailyplanner;

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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

public class ReviewFragment extends Fragment {
    private ReviewViewModel reviewViewModel;
    private ActivityViewModel activityViewModel;
    private ReviewItem existingReview;
    private String currentDate;
    private int currentScore = 0;
    private TextInputEditText etAccomplishment, etChallenges, etImprove;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review, container, false);

        etAccomplishment = view.findViewById(R.id.etAccomplishment);
        etChallenges = view.findViewById(R.id.etChallenges);
        etImprove = view.findViewById(R.id.etImprove);
        MaterialButton btnSave = view.findViewById(R.id.btnSaveReview);
        TextView tvCoachSummary = view.findViewById(R.id.tvCoachSummary);
        TextView tvCoachCategory = view.findViewById(R.id.tvCoachCategory);
        TextView tvAiJudgment = view.findViewById(R.id.tvAiJudgment);

        currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);
        activityViewModel = new ViewModelProvider(this).get(ActivityViewModel.class);

        activityViewModel.getAllActivities().observe(getViewLifecycleOwner(), activities -> {
            int total = activities.size();
            if (total == 0) {
                tvCoachSummary.setText("You completed 0 of 0 tasks today.");
                tvCoachCategory.setText("No tasks scheduled.");
                tvAiJudgment.setText("You didn't schedule any tasks today. A blank canvas! Take a moment to plan for tomorrow.");
                tvAiJudgment.setTextColor(Color.parseColor("#B3E5FC"));
                currentScore = 0;
                return;
            }

            int completed = 0;
            Map<String, Integer> scheduledCategories = new HashMap<>();
            Map<String, Integer> completedCategories = new HashMap<>();

            for (ActivityItem item : activities) {
                scheduledCategories.put(item.category, scheduledCategories.getOrDefault(item.category, 0) + 1);
                if (item.isCompleted) {
                    completed++;
                    completedCategories.put(item.category, completedCategories.getOrDefault(item.category, 0) + 1);
                }
            }

            String dominantFocus = "None";
            int maxCompleted = 0;
            for (Map.Entry<String, Integer> entry : completedCategories.entrySet()) {
                if (entry.getValue() > maxCompleted) { maxCompleted = entry.getValue(); dominantFocus = entry.getKey(); }
            }

            String neglectedArea = "";
            int maxMissed = 0;
            for (Map.Entry<String, Integer> entry : scheduledCategories.entrySet()) {
                int missed = entry.getValue() - completedCategories.getOrDefault(entry.getKey(), 0);
                if (missed > maxMissed) { maxMissed = missed; neglectedArea = entry.getKey(); }
            }

            currentScore = (int) (((float) completed / total) * 100);
            tvCoachSummary.setText(String.format(Locale.getDefault(), "You completed %d of %d tasks (%d%%).", completed, total, currentScore));
            if (!dominantFocus.equals("None")) tvCoachCategory.setText("Most focus on: " + dominantFocus);

            String judgment;
            int color;

            if (completed == total) {
                color = Color.parseColor("#81C784");
                judgment = total >= 5 ? "Outstanding! You crushed all " + total + " tasks today, primarily focusing on " + dominantFocus + ". This is elite discipline." : "Perfect score! You completed everything you set out to do today.";
            } else if (currentScore >= 70) {
                color = Color.parseColor("#81C784");
                judgment = "Strong performance. You completed " + currentScore + "% of your tasks. You nailed your " + dominantFocus + " goals, but fell short on " + neglectedArea + ". Let's prioritize " + neglectedArea + " tomorrow.";
            } else if (currentScore >= 30) {
                color = Color.parseColor("#FFB74D");
                judgment = !neglectedArea.isEmpty() ? "You made decent progress. However, " + neglectedArea + " was left entirely behind. Try time-blocking for " + neglectedArea + " tomorrow." : "You completed " + currentScore + "% of your goals. It's a slow burn, but progress is progress.";
            } else {
                color = Color.parseColor("#E57373");
                judgment = total > 5 ? "It looks like you over-planned today. You scheduled " + total + " tasks but only completed " + completed + ". Tomorrow, schedule a maximum of 3 core tasks." : "A tough day. You only completed " + completed + " out of " + total + " tasks. Reflect on what blocked you, and try again tomorrow.";
            }
            
            tvAiJudgment.setText(judgment);
            tvAiJudgment.setTextColor(color);
        });

        reviewViewModel.getReview(currentDate, item -> {
            if (item != null) {
                existingReview = item;
                requireActivity().runOnUiThread(() -> {
                    etAccomplishment.setText(item.accomplishment);
                    etChallenges.setText(item.challenges);
                    etImprove.setText(item.improve);
                });
            }
        });

        btnSave.setOnClickListener(v -> {
            String acc = etAccomplishment.getText().toString().trim();
            String chal = etChallenges.getText().toString().trim();
            String imp = etImprove.getText().toString().trim();

            if (acc.isEmpty() && chal.isEmpty() && imp.isEmpty()) {
                Toast.makeText(getContext(), "Please write something before saving.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (existingReview == null) {
                reviewViewModel.saveReview(new ReviewItem(currentDate, currentScore, acc, chal, "", imp), false);
            } else {
                existingReview.score = currentScore;
                existingReview.accomplishment = acc;
                existingReview.challenges = chal;
                existingReview.improve = imp;
                reviewViewModel.saveReview(existingReview, true);
            }
            
            Toast.makeText(getContext(), "Review saved! Navigating to Report Card...", Toast.LENGTH_SHORT).show();
            
            // THE FIX: Force the Bottom Navigation to switch to History tab
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_history);
            }
        });

        return view;
    }
}
