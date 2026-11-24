package com.group5.dailyplanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;

public class ScheduleFragment extends Fragment {
    private ActivityViewModel viewModel;
    private ActivityAdapter adapter;
    private TextView tvEmpty;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);

        // Setup RecyclerView
        adapter = new ActivityAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Setup ViewModel
        viewModel = new ViewModelProvider(this).get(ActivityViewModel.class);
        
        // Observe Database Changes
        viewModel.getAllActivities().observe(getViewLifecycleOwner(), activities -> {
            adapter.setActivities(activities);
            if (activities.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        // Handle Checkbox clicks
        adapter.setOnItemClickListener((item, isChecked) -> {
            item.isCompleted = isChecked;
            viewModel.update(item);
        });

        // Handle FAB click
        fabAdd.setOnClickListener(v -> showAddDialog());

        return view;
    }

    private void showAddDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_activity, null);
        TextInputEditText etTitle = dialogView.findViewById(R.id.etTitle);
        TextInputEditText etTime = dialogView.findViewById(R.id.etTime);
        AutoCompleteTextView dropdown = dialogView.findViewById(R.id.dropdownCategory);

        // Setup Category Dropdown
        String[] categories = {"Education", "Software Dev", "Business", "Personal", "Fitness", "Meetings", "Rest"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        dropdown.setAdapter(adapter);

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String time = etTime.getText().toString().trim();
                    String category = dropdown.getText().toString().trim();

                    if (title.isEmpty() || time.isEmpty() || category.isEmpty()) {
                        Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    viewModel.insert(new ActivityItem(title, time, category));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
