package com.group5.dailyplanner;

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
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

        currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);
        activityViewModel = new ViewModelProvider(this).get(ActivityViewModel.class);

        // 1. Calculate Metrics (The Coach)
        activityViewModel.getAllActivities().observe(getViewLifecycleOwner(), activities -> {
            int total = activities.size();
            int completed = 0;
            Map<String, Integer> categoryCounts = new HashMap<>();

            for (ActivityItem item : activities) {
                if (item.isCompleted) completed++;
                // Count categories to find the most active one
                categoryCounts.put(item.category, categoryCounts.getOrDefault(item.category, 0) + 1);
            }

            String summary = String.format(Locale.getDefault(), "You completed %d of %d tasks today.", completed, total);
            tvCoachSummary.setText(summary);

            String topCat = "No tasks scheduled.";
            if (total > 0) {
                int max = 0;
                for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
                    if (entry.getValue() > max) { max = entry.getValue(); topCat = "Most focus on: " + entry.getKey(); }
                }
            }
            tvCoachCategory.setText(topCat);
        });

        // 2. Load existing review
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

        // 3. Save Review
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
            
            Toast.makeText(getContext(), "Review saved to database!", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}
