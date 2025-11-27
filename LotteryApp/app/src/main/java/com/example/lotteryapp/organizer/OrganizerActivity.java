package com.example.lotteryapp.organizer;

import android.os.Bundle;

import com.example.lotteryapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.lotteryapp.databinding.ActivityOrganizerBinding;

/**
 * OrganizerActivity
 *
 * Description:
 *  Activity that hosts the organizer's main UI and bottom navigation bar.
 *  Uses a NavHostFragment and the Jetpack Navigation component to switch
 *  between the organizer's top-level screens.
 *
 * Responsibilities:
 *  - Inflate the organizer activity layout using ViewBinding.
 *  - Set up the {@link NavController} for the organizer navigation host.
 *  - Configure the {@link AppBarConfiguration} so that top-level destinations
 *    do not show a back arrow.
 *  - Connect the ActionBar and BottomNavigationView to the NavController.
 *
 * Author: Xindi Li
 */
public class OrganizerActivity extends AppCompatActivity {

    /** ViewBinding for the organizer activity layout. */
    private ActivityOrganizerBinding binding;
    /** Configuration that defines the top-level navigation destinations. */
    private AppBarConfiguration appBarConfig;
    /** NavController managing navigation within the organizer graph. */
    private NavController navController;

    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState if non-null, this activity is being reinitialized
     *                           after previously being shut down; otherwise {@code null}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrganizerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_organizer);

        // 1) Declare TOP-LEVEL destinations (no back arrow on these)
        appBarConfig = new AppBarConfiguration.Builder(
                R.id.organizerEventFragment,
                R.id.organizerCreateFragment,
                R.id.organizerNoticeFragment,
                R.id.organizerProfileFragment
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
