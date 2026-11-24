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
                    ActivityItem itemToDelete = adapter.getActivityAt(position);
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

    private void showAddDialog() {
        new AddTaskSheet().show(getParentFragmentManager(), "AddTask");
        if(true) return;
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_activity, null);
        TextInputEditText etTitle = dialogView.findViewById(R.id.etTitle);
        TextInputEditText etStartTime = dialogView.findViewById(R.id.etStartTime);
        TextInputEditText etEndTime = dialogView.findViewById(R.id.etEndTime);
        AutoCompleteTextView dropdown = dialogView.findViewById(R.id.dropdownCategory);

        // Reset times for new dialog
        selectedStartTime = "";
        selectedEndTime = "";

        String[] categories = {"Education", "Software Dev", "Business", "Personal", "Fitness", "Meetings", "Rest"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        dropdown.setAdapter(adapter);

        etStartTime.setOnClickListener(v -> {
            MaterialTimePicker picker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(8).setMinute(0)
                    .setTitleText("Select Start Time")
                    .build();
            picker.show(getParentFragmentManager(), "START_TIME");
            picker.addOnPositiveButtonClickListener(dialog -> {
                int hour = picker.getHour();
                int minute = picker.getMinute();
                selectedStartTime = String.format(Locale.getDefault(), "%02d:%02d %s", 
                        (hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour)), 
                        minute, (hour >= 12 ? "PM" : "AM"));
                etStartTime.setText(selectedStartTime);
            });
        });

        etEndTime.setOnClickListener(v -> {
            MaterialTimePicker picker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(9).setMinute(0)
                    .setTitleText("Select End Time")
                    .build();
            picker.show(getParentFragmentManager(), "END_TIME");
            picker.addOnPositiveButtonClickListener(dialog -> {
                int hour = picker.getHour();
                int minute = picker.getMinute();
                selectedEndTime = String.format(Locale.getDefault(), "%02d:%02d %s", 
                        (hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour)), 
                        minute, (hour >= 12 ? "PM" : "AM"));
                etEndTime.setText(selectedEndTime);
            });
        });

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String category = dropdown.getText().toString().trim();

                    if (title.isEmpty() || selectedStartTime.isEmpty() || selectedEndTime.isEmpty() || category.isEmpty()) {
                        Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    viewModel.insert(new ActivityItem(title, selectedStartTime, selectedEndTime, category));

                    scheduleAlarm(title, selectedStartTime, "Time to start: " + title);
                    scheduleAlarm(title, selectedEndTime, "Time is up for: " + title);
                    
                    Toast.makeText(getContext(), "Task and Alarms Saved!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void scheduleAlarm(String title, String timeString, String message) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a", Locale.getDefault());
            java.util.Date date = sdf.parse(timeString);
            
            Calendar calendar = Calendar.getInstance();
            Calendar now = Calendar.getInstance();
            if (date != null) {
                Calendar timeCal = Calendar.getInstance();
                timeCal.setTime(date);
                calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
                calendar.set(Calendar.SECOND, 0);

                if (calendar.before(now)) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                }
            }

            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(requireContext(), AlarmReceiver.class);
            intent.putExtra("title", title);
            intent.putExtra("message", message);
            
            int requestCode = (int) System.currentTimeMillis(); 
            PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
