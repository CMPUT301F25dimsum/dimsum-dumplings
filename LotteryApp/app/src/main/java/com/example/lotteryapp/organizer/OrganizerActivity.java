package com.example.lotteryapp.organizer;

import android.os.Bundle;

import com.example.lotteryapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.lotteryapp.databinding.ActivityOrganizerBinding;

/**
 * Purpose: Activity called when logged in as an organizer. Simply configures navigation
 * <p>
 * Outstanding Issues: None
 */
public class OrganizerActivity extends AppCompatActivity {

    private ActivityOrganizerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityOrganizerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_organizer);
        NavigationUI.setupWithNavController(binding.navView, navController);
        NavigationUI.setupActionBarWithNavController(this, navController);
    }

}