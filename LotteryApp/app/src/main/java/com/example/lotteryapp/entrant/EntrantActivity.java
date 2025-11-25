package com.example.lotteryapp.entrant;

import android.os.Bundle;

import com.example.lotteryapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.lotteryapp.databinding.ActivityEntrantBinding;

public class EntrantActivity extends AppCompatActivity {

    private ActivityEntrantBinding binding;
    private AppBarConfiguration appBarConfig;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEntrantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_entrant);

        // 1) Declare TOP-LEVEL destinations (no back arrow on these)
        appBarConfig = new AppBarConfiguration.Builder(
                R.id.entrantEventFragment,
                R.id.entrantSavedEventFragment,
                R.id.entrantNoticeFragment,
                R.id.entrantProfileFragment
        ).build();

        // 2) Hook ActionBar to NavController with that AppBarConfiguration
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfig);

        // 3) Hook BottomNavigationView to NavController
        NavigationUI.setupWithNavController(navView, navController);
    }

    // Make the ActionBar back arrow actually navigate up
    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfig) || super.onSupportNavigateUp();
    }
}
