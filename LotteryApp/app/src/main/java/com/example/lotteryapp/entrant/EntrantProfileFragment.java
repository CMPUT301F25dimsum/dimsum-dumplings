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

/**
 * Entrant Profile screen with 3 simple clickable modules:
 * - Account Information
 * - Update Account
 * - Notification Settings
 *
 * Keep logic minimal: just click listeners + TODOs to navigate later.
 */
public class EntrantProfileFragment extends Fragment {

    public EntrantProfileFragment() { /* Required empty public constructor */ }

    public static EntrantProfileFragment newInstance() {
        return new EntrantProfileFragment();
    }

    @Override
    public @Nullable View onCreateView(@NonNull LayoutInflater inflater,
                                       @Nullable ViewGroup container,
                                       @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_entrant_profile, container, false);

        MaterialCardView cardAccountInfo = v.findViewById(R.id.card_account_info);
        MaterialCardView cardUpdate      = v.findViewById(R.id.card_update_account);
        MaterialCardView cardNotif       = v.findViewById(R.id.card_notification);

        // --- Clicks (replace Toast with real navigation when ready) ---
        cardAccountInfo.setOnClickListener(view -> {
            Toast.makeText(requireContext(), "Account Information", Toast.LENGTH_SHORT).show();
            // TODO: startActivity(new Intent(requireContext(), EntrantAccountInfoActivity.class));
            // or navigate to a fragment
        });

        cardUpdate.setOnClickListener(view -> {
            Toast.makeText(requireContext(), "Update Account", Toast.LENGTH_SHORT).show();
            // TODO: startActivity(new Intent(requireContext(), EntrantUpdateAccountActivity.class));
        });

        cardNotif.setOnClickListener(view -> {
            Toast.makeText(requireContext(), "Notification Settings", Toast.LENGTH_SHORT).show();
            // TODO: startActivity(new Intent(requireContext(), EntrantNotificationSettingsActivity.class));
        });

        return v;
    }
}
