package com.group5.dailyplanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SetupFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setup, container, false);

        TextInputEditText etName = view.findViewById(R.id.etName);
        MaterialButton btnGetStarted = view.findViewById(R.id.btnGetStarted);

        btnGetStarted.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            prefs.edit().putString("userName", name).apply();

            Navigation.findNavController(view).navigate(R.id.action_setup_to_dashboard);
        });

        return view;
    }
}
