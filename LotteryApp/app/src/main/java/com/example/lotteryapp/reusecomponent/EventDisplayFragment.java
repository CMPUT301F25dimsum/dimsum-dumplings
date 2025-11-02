package com.example.lotteryapp.reusecomponent;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import androidx.fragment.app.DialogFragment;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lotteryapp.R;
import com.example.lotteryapp.databinding.FragmentEventDisplayBinding;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class EventDisplayFragment extends DialogFragment {

    private FragmentEventDisplayBinding binding;
    private Event event;

    private final String dateFormat = "yyyy/MM/dd HH:mm:ss";
    private final SimpleDateFormat formatter;


    public EventDisplayFragment(){
        formatter = new SimpleDateFormat(dateFormat, Locale.CANADA);
    }
    public EventDisplayFragment(Event event) {
        this.event = event;
        formatter = new SimpleDateFormat(dateFormat, Locale.CANADA);
    }

    private void updateFragment(boolean inEvent){
        if (!inEvent){
            binding.fragmentEventDisplayRegisterButton.setBackgroundColor(Color.YELLOW);
            binding.fragmentEventDisplayRegisterButton.setText("Register");
            binding.fragmentEventDisplayRegisterButton.setClickable(true);
            binding.fragmentEventDisplayCancelButton.setVisibility(GONE);
            return;
        }
        binding.fragmentEventDisplayRegisterButton.setBackgroundColor(Color.LTGRAY);
        binding.fragmentEventDisplayRegisterButton.setText("Awaiting Result...");
        binding.fragmentEventDisplayRegisterButton.setClickable(false);
        binding.fragmentEventDisplayCancelButton.setVisibility(VISIBLE);

    }

    @Nullable
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        binding = FragmentEventDisplayBinding.inflate(LayoutInflater.from(getContext()));
        dialog.setContentView(binding.getRoot());

        SharedPreferences currentUser = requireContext().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        String userID = currentUser.getString("UID", "John");

        // Bind data
        binding.fragmentEventDisplayTitle.setText(event.getTitle());
        binding.fragmentEventDisplayOrganizer.setText(event.getOrganizer());
        binding.fragmentOrganizerCreateDescription.setText(event.getDescription());
        binding.fragmentEventDisplayLocation.setText(event.getEventLocation());
        binding.fragmentEventDisplayRegDeadline.setText(formatter.format(event.getLotteryEndDate()));

        binding.fragmentEventDisplayStatus.setText(event.isOpen());
        if (event.isOpen().equals("Open"))
            binding.fragmentEventDisplayStatus.setTextColor(Color.GREEN);
        else
            binding.fragmentEventDisplayStatus.setTextColor(Color.RED);

        String countCapacity = event.getNentrants() + "/";
        if (event.getRegistrationLimit() != -1){
            countCapacity += event.getRegistrationLimit();
        }
        else{
            countCapacity += "-";
        }
        binding.fragmentEventDisplayCapacity.setText(countCapacity);
        // Set event banner
        // binding.entrantEventImage

        // Close dialog
        binding.fragmentEventDisplayClose.setOnClickListener(v -> dismiss());

        binding.fragmentEventDisplayCancelButton.setOnClickListener(v -> {
            event.getLottery().removeEntrant(userID);
            updateFragment(false);
        });

        // Register button click
        binding.fragmentEventDisplayRegisterButton.setOnClickListener(v -> {
            event.getLottery().addEntrant(userID);
            updateFragment(true);
        });

        // Set buttons to proper layout
        updateFragment(event.getLottery().containsEntrant(userID));
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
