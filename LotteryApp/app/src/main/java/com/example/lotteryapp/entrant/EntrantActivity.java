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

/**
 * EntrantActivity
 *
 * Description:
 *  Activity that hosts the entrant's main UI and bottom navigation bar.
 *  Uses a NavHostFragment and the Jetpack Navigation component to switch
 *  between the entrant's top-level screens.
 *
 * Responsibilities:
 *  - Inflate the entrant activity layout using ViewBinding.
 *  - Set up the {@link NavController} for the entrant navigation host.
 *  - Configure the {@link AppBarConfiguration} so that top-level destinations
 *    do not show a back arrow.
 *  - Connect the ActionBar and BottomNavigationView to the NavController.
 *
 * Author: Xindi Li
 */
public class EntrantActivity extends AppCompatActivity {

    /** ViewBinding for the entrant activity layout. */
    private ActivityEntrantBinding binding;
    /** Configuration that defines the top-level navigation destinations. */
    private AppBarConfiguration appBarConfig;
    /** NavController managing navigation within the entrant graph. */
    private NavController navController;

    /**
     * Called when the activity is starting.
     * @param savedInstanceState if non-null, this activity is being reinitialized
     *                           after previously being shut down; otherwise {@code null}
     */
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

    /**
     * Handles navigation when the user presses the ActionBar "up" (back) button.
     * @return {@code true} if navigation was handled by the NavController,
     *         otherwise the result of {@code super.onSupportNavigateUp()}
     */
    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfig)
                || super.onSupportNavigateUp();
    }
}
