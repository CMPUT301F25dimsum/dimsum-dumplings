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
 * Entrant Profile: three tiles. Uses Navigation Component to go to Account Info.
 */
public class EntrantProfileFragment extends Fragment {

    public EntrantProfileFragment() {}

    public static EntrantProfileFragment newInstance() { return new EntrantProfileFragment(); }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_entrant_profile, container, false);

        MaterialCardView cardAccountInfo = v.findViewById(R.id.card_account_info);
        MaterialCardView cardUpdate      = v.findViewById(R.id.card_update_account);
        MaterialCardView cardNotif       = v.findViewById(R.id.card_notification);

        cardAccountInfo.setOnClickListener(view ->
                findNavController(this).navigate(R.id.action_profile_to_accountInfo)
        );

        cardUpdate.setOnClickListener(view ->
                findNavController(this).navigate(R.id.action_profile_to_updateAccount)
        );

        cardNotif.setOnClickListener(view ->
                Toast.makeText(requireContext(), "Notification Settings", Toast.LENGTH_SHORT).show());

        return v;
    }
}
