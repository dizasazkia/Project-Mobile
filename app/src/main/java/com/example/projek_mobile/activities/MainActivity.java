package com.example.projek_mobile.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.projek_mobile.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ImageButton btnToggleTheme;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AppPreferences";
    private static final String KEY_THEME = "theme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load theme preference before setContentView
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedTheme = sharedPreferences.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        btnToggleTheme = findViewById(R.id.btn_toggle_theme);

        // Set initial icon for theme button
        updateThemeButtonIcon(savedTheme);

        // Setup NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Add destination changed listener to control btn_toggle_theme visibility
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.nav_home) {
                btnToggleTheme.setVisibility(View.VISIBLE);
            } else {
                btnToggleTheme.setVisibility(View.GONE);
            }
        });

        // Theme toggle button logic
        btnToggleTheme.setOnClickListener(v -> {
            int currentMode = AppCompatDelegate.getDefaultNightMode();
            int newMode = (currentMode == AppCompatDelegate.MODE_NIGHT_YES)
                    ? AppCompatDelegate.MODE_NIGHT_NO
                    : AppCompatDelegate.MODE_NIGHT_YES;
            AppCompatDelegate.setDefaultNightMode(newMode);
            saveThemePreference(newMode);
            updateThemeButtonIcon(newMode);
        });
    }

    public void navigateToBookmarkFragment() {
        NavController navController = NavHostFragment.findNavController(
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment));
        navController.navigate(R.id.nav_bookmark);
        bottomNavigationView.setSelectedItemId(R.id.nav_bookmark);
    }

    private void saveThemePreference(int themeMode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_THEME, themeMode);
        editor.apply();
    }

    private void updateThemeButtonIcon(int themeMode) {
        int iconRes = (themeMode == AppCompatDelegate.MODE_NIGHT_YES)
                ? R.drawable.ic_sun
                : R.drawable.ic_moon;
        btnToggleTheme.setImageResource(iconRes);
    }
}