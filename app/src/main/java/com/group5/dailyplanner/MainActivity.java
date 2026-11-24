package com.group5.dailyplanner;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        
        // 1. Load Theme
        boolean isDarkMode = prefs.getBoolean("darkMode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 2. Load Custom Top Bar Color (Default to Blue)
        String topBarColor = prefs.getString("topBarColor", "#1565C0");
        getWindow().setStatusBarColor(Color.parseColor(topBarColor));

        // 3. Notification Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // 4. Navigation Setup
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.nav_setup) {
                    bottomNav.setVisibility(android.view.View.GONE);
                    findViewById(R.id.footerText).setVisibility(android.view.View.GONE);
                } else {
                    bottomNav.setVisibility(android.view.View.VISIBLE);
                    findViewById(R.id.footerText).setVisibility(android.view.View.VISIBLE);
                }
            });

            NavigationUI.setupWithNavController(bottomNav, navController);

            String savedName = prefs.getString("userName", null);
            if (savedName != null && !savedName.isEmpty()) {
                navController.navigate(R.id.nav_home);
            }
        }
    }
}
