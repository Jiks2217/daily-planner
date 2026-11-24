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
import com.google.android.material.progressindicator.LinearProgressIndicator;
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
        TextView tvStats = view.findViewById(R.id.tvStats);
        LinearProgressIndicator progressBar = view.findViewById(R.id.progressBar);

        // 1. Set the Date (Using the phone/emulator's current time)
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        tvDate.setText(sdf.format(new Date()));

        // 2. Get the Name from SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String name = prefs.getString("userName", "User");
        
        // 3. Time-based Greeting
        int hour = new Date().getHours();
        String greeting;
        if (hour < 12) greeting = "Good morning, ";
        else if (hour < 18) greeting = "Good afternoon, ";
        else greeting = "Good evening, ";

        tvGreeting.setText(greeting + name);

        // 4. Mock Progress (Will be replaced by Room Database in Phase 4)
        int mockProgress = 0; 
        tvProgress.setText(mockProgress + "%");
        progressBar.setProgressCompat(mockProgress, true);
        
        // 5. Set Empty State text
        tvStats.setText("No activities planned yet. Tap the Schedule tab to add one.");

        return view;
    }
}
