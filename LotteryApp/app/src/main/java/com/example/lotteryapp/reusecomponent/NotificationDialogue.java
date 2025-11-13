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

/**
 * Purpose: Pop-up dialog for organizers to send a custom notification to lottery participants.
 * Adapted from Lab 5 participation exercise.
 *
 * Issues: None
 */
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
        EditText message = new EditText(requireContext()); //ChatGPT suggested just making an EditText directly instead of inflating from an XML (lab 5)
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
