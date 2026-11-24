package com.group5.dailyplanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        MaterialSwitch switchDarkMode = view.findViewById(R.id.switchDarkMode);
        TextInputEditText etName = view.findViewById(R.id.etSettingsName);
        MaterialButton btnSaveName = view.findViewById(R.id.btnSaveName);

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // Theme Switch
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switchDarkMode.setChecked(currentNightMode == Configuration.UI_MODE_NIGHT_YES);
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("darkMode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // Name Change
        etName.setText(prefs.getString("userName", "User"));
        btnSaveName.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            if (!newName.isEmpty()) {
                prefs.edit().putString("userName", newName).apply();
                Toast.makeText(getContext(), "Name updated!", Toast.LENGTH_SHORT).show();
            }
        });

        // COLOR PICKER LOGIC
        setupColorPicker(view.findViewById(R.id.colorBlue), prefs, "#1565C0");
        setupColorPicker(view.findViewById(R.id.colorGreen), prefs, "#2E7D32");
        setupColorPicker(view.findViewById(R.id.colorPurple), prefs, "#6A1B9A");
        setupColorPicker(view.findViewById(R.id.colorRed), prefs, "#C62828");
        setupColorPicker(view.findViewById(R.id.colorBlack), prefs, "#1E1E1E");

        return view;
    }

    private void setupColorPicker(View colorView, SharedPreferences prefs, String hexColor) {
        // Make the square rounded for a better look
        colorView.setBackgroundResource(android.R.drawable.btn_default_small);
        colorView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor(hexColor)));

        colorView.setOnClickListener(v -> {
            // Save the color
            prefs.edit().putString("topBarColor", hexColor).apply();
            
            // Apply immediately to the Activity window
            if (getActivity() != null) {
                getActivity().getWindow().setStatusBarColor(Color.parseColor(hexColor));
            }
            
            Toast.makeText(getContext(), "Top Bar Color Updated!", Toast.LENGTH_SHORT).show();
        });
    }
}
