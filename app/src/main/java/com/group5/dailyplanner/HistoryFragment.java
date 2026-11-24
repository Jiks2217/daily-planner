package com.group5.dailyplanner;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButtonToggleGroup;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {
    private ReviewViewModel viewModel;
    private HistoryAdapter adapter;
    private TextView tvAverageScore;
    private List<ReviewItem> allReviewsCache = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        tvAverageScore = view.findViewById(R.id.tvAverageScore);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewHistory);
        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroup);

        adapter = new HistoryAdapter();
        adapter.setOnItemClickListener(item -> new ReviewDetailSheet(item).show(getParentFragmentManager(), "Detail"));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ReviewViewModel.class);

        viewModel.getAllReviews().observe(getViewLifecycleOwner(), reviews -> {
            allReviewsCache = reviews;
            // Default to Week view
            toggleGroup.check(R.id.btnWeek);
            filterData(7);
        });

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnWeek) filterData(7);
                else if (checkedId == R.id.btnMonth) filterData(30);
                else if (checkedId == R.id.btnYear) filterData(365);
            }
        });

        return view;
    }

    private void filterData(int daysBack) {
        List<ReviewItem> filteredList = new ArrayList<>();
        int totalScore = 0;

        Calendar cutoff = Calendar.getInstance();
        cutoff.add(Calendar.DAY_OF_YEAR, -daysBack);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String cutoffStr = sdf.format(cutoff.getTime());

        for (ReviewItem item : allReviewsCache) {
            // Simple string comparison works because format is yyyy-MM-dd
            if (item.date.compareTo(cutoffStr) >= 0) {
                filteredList.add(item);
                totalScore += item.score;
            }
        }

        adapter.setReviews(filteredList);

        if (filteredList.isEmpty()) {
            tvAverageScore.setText("--");
            tvAverageScore.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            int average = totalScore / filteredList.size();
            tvAverageScore.setText(String.valueOf(average));
            
            if (average >= 80) tvAverageScore.setTextColor(Color.parseColor("#81C784")); // Green
            else if (average >= 50) tvAverageScore.setTextColor(Color.parseColor("#FFB74D")); // Orange
            else tvAverageScore.setTextColor(Color.parseColor("#E57373")); // Red
        }
    }
}
