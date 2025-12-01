package com.example.lotteryapp.entrant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lotteryapp.R;
import com.google.android.material.card.MaterialCardView;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

/**
 * EntrantProfileFragment
 * Description:
 *  Fragment that displays the entrant's profile menu with three navigation tiles:
 *  - Account Info
 *  - Update Account
 *  - Notification Settings (placeholder)
 * Responsibilities:
 *  - Inflate the profile menu UI.
 *  - Navigate to the Account Info screen.
 *  - Navigate to the Update Account screen.
 *  - Show a placeholder message for Notification Settings.
 * Author: Xindi Li
 */
public class EntrantProfileFragment extends Fragment {

    /**
     * Default empty constructor required for Fragment instantiation.
     */
    public EntrantProfileFragment() {}

    /**
     * Factory method to create a new instance of this fragment.
     *
     * @return a new {@link EntrantProfileFragment} instance
     */
    public static EntrantProfileFragment newInstance() {
        return new EntrantProfileFragment();
    }

    /**
     * Inflates the layout for this fragment and binds click listeners
     * to each profile menu tile.
     *
     * @param inflater  the {@link LayoutInflater} used to inflate the UI
     * @param container the parent view for this fragment's layout
     * @param savedInstanceState previously saved state, or {@code null} if none
     * @return the root {@link View} representing this fragment's UI
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_entrant_profile, container, false);

        MaterialCardView cardAccountInfo = v.findViewById(R.id.card_account_info);
        MaterialCardView cardUpdate      = v.findViewById(R.id.card_update_account);
        MaterialCardView cardNotif       = v.findViewById(R.id.card_notification);

        // Navigate to Account Info
        cardAccountInfo.setOnClickListener(view ->
                findNavController(this).navigate(R.id.action_profile_to_accountInfo)
        );

        // Navigate to Update Account
        cardUpdate.setOnClickListener(view ->
                findNavController(this).navigate(R.id.action_profile_to_updateAccount)
        );

        cardNotif.setOnClickListener(view -> {
            new EntrantNotificationSettingsDialogFragment().show(getParentFragmentManager(), "EntrantNotificationSettingsDialogFragment");
        });

        return v;
    }
}