package com.group5.dailyplanner;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import java.util.Calendar;
import java.util.Locale;

public class ScheduleFragment extends Fragment {
    private ActivityViewModel viewModel;
    private ActivityAdapter adapter;
    private TextView tvEmpty;
    private RecyclerView recyclerView;

    private String selectedStartTime = "";
    private String selectedEndTime = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);

        adapter = new ActivityAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ActivityViewModel.class);
        
        viewModel.getAllActivities().observe(getViewLifecycleOwner(), activities -> {
            adapter.setActivities(activities);
            tvEmpty.setVisibility(activities.isEmpty() ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(activities.isEmpty() ? View.GONE : View.VISIBLE);
        });

        adapter.setOnItemClickListener((item, isChecked) -> {
            item.isCompleted = isChecked;
            viewModel.update(item);
        });

        // SWIPE TO DELETE LOGIC
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    ActivityItem itemToDelete = ((ActivityAdapter.ViewHolder) viewHolder).getBindingAdapterPosition() != -1 ? 
                            adapter.getActivityAt(position) : null;
                    if (itemToDelete != null) {
                        viewModel.delete(itemToDelete);
                        Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }).attachToRecyclerView(recyclerView);

        fabAdd.setOnClickListener(v -> showAddDialog());

        return view;
    }
    
    // ... The rest of the showAddDialog and scheduleAlarm methods remain the same as the previous phase ...
    // For brevity, if you already have the previous ScheduleFragment, you only need to copy the 
    // SWIPE TO DELETE block and the getActivityAt method inside the Adapter.
