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
import java.util.Locale;

public class ReviewFragment extends Fragment {
    private ReviewViewModel viewModel;
    private ReviewItem existingReview;
    private String currentDate;

    private TextInputEditText etAccomplishment, etChallenges, etLearned, etImprove;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review, container, false);

        TextView tvReviewDate = view.findViewById(R.id.tvReviewDate);
        etAccomplishment = view.findViewById(R.id.etAccomplishment);
        etChallenges = view.findViewById(R.id.etChallenges);
        etLearned = view.findViewById(R.id.etLearned);
        etImprove = view.findViewById(R.id.etImprove);
        MaterialButton btnSave = view.findViewById(R.id.btnSaveReview);

        // Setup Date
        Date now = new Date();
        SimpleDateFormat sdfDisplay = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        SimpleDateFormat sdfDb = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        currentDate = sdfDb.format(now);
        tvReviewDate.setText(sdfDisplay.format(now));

        viewModel = new ViewModelProvider(this).get(ReviewViewModel.class);

        // Load existing review for today
        viewModel.getReview(currentDate, item -> {
            if (item != null) {
                existingReview = item;
                requireActivity().runOnUiThread(() -> {
                    etAccomplishment.setText(item.accomplishment);
                    etChallenges.setText(item.challenges);
                    etLearned.setText(item.learned);
                    etImprove.setText(item.improve);
                });
            }
        });

        btnSave.setOnClickListener(v -> {
            String acc = etAccomplishment.getText().toString().trim();
            String chal = etChallenges.getText().toString().trim();
            String learn = etLearned.getText().toString().trim();
            String imp = etImprove.getText().toString().trim();

            if (acc.isEmpty() && chal.isEmpty() && learn.isEmpty() && imp.isEmpty()) {
                Toast.makeText(getContext(), "Please write something before saving.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (existingReview == null) {
                // Create new
                ReviewItem newReview = new ReviewItem(currentDate, acc, chal, learn, imp);
                viewModel.saveReview(newReview, false);
            } else {
                // Update existing
                existingReview.accomplishment = acc;
                existingReview.challenges = chal;
                existingReview.learned = learn;
                existingReview.improve = imp;
                viewModel.saveReview(existingReview, true);
            }
            
            Toast.makeText(getContext(), "Review saved successfully!", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}
