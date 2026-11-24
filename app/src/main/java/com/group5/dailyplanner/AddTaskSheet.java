package com.group5.dailyplanner;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTaskSheet extends BottomSheetDialogFragment {
    private String startTime = "", endTime = "";
    private ActivityViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_add_task, container, false);
        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(ActivityViewModel.class);

        TextInputEditText etTitle = view.findViewById(R.id.etTitle);
        TextInputEditText etStart = view.findViewById(R.id.etStartTime);
        TextInputEditText etEnd = view.findViewById(R.id.etEndTime);
        ChipGroup chipGroup = view.findViewById(R.id.chipGroupCategory);
        MaterialButton btnSave = view.findViewById(R.id.btnSaveTask);

        etStart.setOnClickListener(v -> {
            MaterialTimePicker picker = new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H).setHour(8).setMinute(0).setTitleText("Start Time").build();
            picker.show(getParentFragmentManager(), "START");
            picker.addOnPositiveButtonClickListener(d -> {
                startTime = formatTime(picker.getHour(), picker.getMinute());
                etStart.setText(startTime);
            });
        });

        etEnd.setOnClickListener(v -> {
            MaterialTimePicker picker = new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H).setHour(9).setMinute(0).setTitleText("End Time").build();
            picker.show(getParentFragmentManager(), "END");
            picker.addOnPositiveButtonClickListener(d -> {
                endTime = formatTime(picker.getHour(), picker.getMinute());
                etEnd.setText(endTime);
            });
        });

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            int selectedChipId = chipGroup.getCheckedChipId();
            String category = "";

            if (selectedChipId != View.NO_ID) {
                Chip chip = view.findViewById(selectedChipId);
                category = chip.getText().toString();
            }

            if (title.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || category.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.insert(new ActivityItem(title, startTime, endTime, category));
            scheduleAlarm(title, startTime, "Time to start: " + title);
            scheduleAlarm(title, endTime, "Time is up for: " + title);
            
            Toast.makeText(getContext(), "Activity Saved!", Toast.LENGTH_SHORT).show();
            dismiss(); // Close the bottom sheet
        });

        return view;
    }

    private String formatTime(int hour, int minute) {
        return String.format(Locale.getDefault(), "%02d:%02d %s", (hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour)), minute, (hour >= 12 ? "PM" : "AM"));
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
                if (calendar.before(now)) calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(requireContext(), AlarmReceiver.class);
            intent.putExtra("title", title);
            intent.putExtra("message", message);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            if (alarmManager != null) alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
