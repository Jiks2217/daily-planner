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

        // THE REASONING ENGINE
        activityViewModel.getAllActivities().observe(getViewLifecycleOwner(), activities -> {
            int total = activities.size();
            if (total == 0) {
                tvCoachSummary.setText("You completed 0 of 0 tasks today.");
                tvCoachCategory.setText("No tasks scheduled.");
                tvAiJudgment.setText("You didn't schedule any tasks today. A blank canvas! Take a moment to plan for tomorrow so we can build momentum.");
                tvAiJudgment.setTextColor(Color.parseColor("#B3E5FC"));
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
                if (entry.getValue() > maxCompleted) {
                    maxCompleted = entry.getValue();
                    dominantFocus = entry.getKey();
                }
            }

            String neglectedArea = "";
            int maxMissed = 0;
            for (Map.Entry<String, Integer> entry : scheduledCategories.entrySet()) {
                String cat = entry.getKey();
                int scheduled = entry.getValue();
                int done = completedCategories.getOrDefault(cat, 0);
                int missed = scheduled - done;
                if (missed > maxMissed) {
                    maxMissed = missed;
                    neglectedArea = cat;
                }
            }

            int percent = (int) (((float) completed / total) * 100);
            tvCoachSummary.setText(String.format(Locale.getDefault(), "You completed %d of %d tasks (%d%%).", completed, total, percent));
            
            if (!dominantFocus.equals("None")) {
                tvCoachCategory.setText("Most focus on: " + dominantFocus);
            }

            // AI LOGIC
            String judgment;
            int color;

            if (completed == total) {
                color = Color.parseColor("#81C784");
                if (total >= 5) {
                    judgment = "Outstanding! You crushed all " + total + " tasks today, primarily focusing on " + dominantFocus + ". This is elite discipline. Be careful of burnout—make sure to schedule rest tomorrow.";
                } else {
                    judgment = "Perfect score! You completed everything you set out to do today, focusing on " + dominantFocus + ". A flawless execution.";
                }
            } else if (percent >= 70) {
                color = Color.parseColor("#81C784");
                judgment = "Strong performance. You completed " + percent + "% of your tasks. You nailed your " + dominantFocus + " goals, but fell short on " + neglectedArea + ". Let's prioritize " + neglectedArea + " tomorrow to achieve 100%.";
            } else if (percent >= 30) {
                color = Color.parseColor("#FFB74D");
                if (!neglectedArea.isEmpty()) {
                    judgment = "You made decent progress, completing " + completed + " tasks. However, " + neglectedArea + " was left entirely behind. It looks like your time allocation needs adjustment. Try time-blocking for " + neglectedArea + " tomorrow.";
                } else {
                    judgment = "You completed " + percent + "% of your goals. It's a slow burn, but progress is progress. You focused heavily on " + dominantFocus + ". Consider breaking your remaining tasks into smaller, easier chunks tomorrow.";
                }
            } else {
                color = Color.parseColor("#E57373");
                if (total > 5) {
                    judgment = "It looks like you over-planned today. You scheduled " + total + " tasks but only completed " + completed + ". Overwhelming yourself causes paralysis. Tomorrow, try scheduling a maximum of 3 core tasks.";
                } else if (!neglectedArea.isEmpty()) {
                    judgment = "A tough day. You only completed " + completed + " out of " + total + " tasks, and completely missed your " + neglectedArea + " goal. Don't beat yourself up. Reflect on what blocked you, and try again tomorrow.";
                } else {
                    judgment = "A very unproductive day. You only finished " + completed + " tasks. Let's do a hard reset tomorrow. Set just one small goal for " + dominantFocus + " and build momentum from there.";
                }
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
                reviewViewModel.saveReview(new ReviewItem(currentDate, acc, chal, "", imp), false);
            } else {
                existingReview.accomplishment = acc;
                existingReview.challenges = chal;
                existingReview.improve = imp;
                reviewViewModel.saveReview(existingReview, true);
            }
            Toast.makeText(getContext(), "Review saved!", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}
