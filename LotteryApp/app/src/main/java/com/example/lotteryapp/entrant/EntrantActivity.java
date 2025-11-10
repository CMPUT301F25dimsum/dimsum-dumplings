package com.example.lotteryapp.entrant;

import android.os.Bundle;

import com.example.lotteryapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.lotteryapp.databinding.ActivityEntrantBinding;

import java.util.Objects;

/**
 * Purpose: Main activity displayed to the entrant on login. Simply configures navigation.
 * <p>
 * Outstanding Issues: None
 */
public class EntrantActivity extends AppCompatActivity {

    private ActivityEntrantBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEntrantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_entrant);
        NavigationUI.setupWithNavController(binding.navView, navController);
        NavigationUI.setupActionBarWithNavController(this, navController);
    }

}