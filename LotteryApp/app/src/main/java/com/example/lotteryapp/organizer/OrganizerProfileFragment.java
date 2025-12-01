package com.example.lotteryapp.organizer;

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
 * Organizer Profile: three tiles. Uses Navigation Component to go to Account Info.
 * Author: Xindi Li
 */
public class OrganizerProfileFragment extends Fragment {

    /**
     * Default empty constructor required for Fragment instantiation.
     */
    public OrganizerProfileFragment() {}

    /**
     * Factory method to create a new instance of this fragment.
     *
     * @return a new {@link OrganizerProfileFragment} instance
     */
    public static OrganizerProfileFragment newInstance() {
        return new OrganizerProfileFragment();
    }

    /**
     * Inflates the layout for this fragment and binds click listeners
     * for the profile menu tiles.
     *
     * @param inflater           the {@link LayoutInflater} used to inflate the layout
     * @param container          the parent {@link ViewGroup} the fragment's UI will be attached to
     * @param savedInstanceState previously saved state, or {@code null} if none
     * @return the root {@link View} for this fragment's UI
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_organizer_profile, container, false);

        MaterialCardView cardAccountInfo = v.findViewById(R.id.card_account_info);
        MaterialCardView cardUpdate      = v.findViewById(R.id.card_update_account);

        cardAccountInfo.setOnClickListener(view ->
                findNavController(this).navigate(R.id.action_profile_to_accountInfo)
        );

        cardUpdate.setOnClickListener(view ->
                findNavController(this).navigate(R.id.action_profile_to_updateAccount)
        );

        return v;
    }
}
