package com.example.lotteryapp.reusecomponent;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.lotteryapp.R;

import java.util.Objects;

public class NotificationDialogue extends DialogFragment {
    public interface Listener {
        void confirm(String value);
    }
    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        EditText message = new EditText(requireContext());
        message.setInputType(InputType.TYPE_CLASS_TEXT);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        return builder
                .setView(message)
                .setTitle("Notification")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Notify", (dialog, which) -> {
                    listener.confirm(message.getText().toString());
                })
                .create();
    }
}
