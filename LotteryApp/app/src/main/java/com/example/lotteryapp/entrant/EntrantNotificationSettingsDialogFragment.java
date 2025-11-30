package com.example.lotteryapp.entrant;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.lotteryapp.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class EntrantNotificationSettingsDialogFragment extends DialogFragment {

    private static final String PREF_FILE = "loginInfo";
    private static final String KEY_ENABLE_ENTRANT_NOTIF = "enableEntrantNotif";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_entrant_notification_settings, null);

        SwitchMaterial switchNotifications = view.findViewById(R.id.switch_notifications);

        SharedPreferences prefs = requireActivity().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        boolean areNotificationsEnabled = prefs.getBoolean(KEY_ENABLE_ENTRANT_NOTIF, true);
        switchNotifications.setChecked(areNotificationsEnabled);

        builder.setView(view)
                .setTitle("Notification Settings")
                .setPositiveButton("Save", (dialog, id) -> {
                    boolean isEnabled = switchNotifications.isChecked();
                    prefs.edit().putBoolean(KEY_ENABLE_ENTRANT_NOTIF, isEnabled).apply();
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    if (getDialog() != null) {
                        getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
