/*
* This is an all-purpose display event fragment for each role
* Entrants will see the ability to register/cancel signup
*   - If they have been invited they can accept/reject instead
* Organizers see the ability to edit/manage the event
* Admins see the ability to delete events
* */

package com.example.lotteryapp.reusecomponent;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import androidx.fragment.app.DialogFragment;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.example.lotteryapp.databinding.FragmentEventDisplayBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class EventDisplayFragment extends DialogFragment {

    private FragmentEventDisplayBinding binding;
    private Event event;
    private final DocumentReference eventDoc;

    private final String dateFormat = "yyyy/MM/dd HH:mm:ss";
    private final SimpleDateFormat formatter;
    private final ListenerRegistration eventListener;

    /**
     * Constructor that takes in an organizerID + eventID
     */
    public EventDisplayFragment(Event event) {
        this.event = event;
        formatter = new SimpleDateFormat(dateFormat, Locale.CANADA);
        eventDoc = FirebaseFirestore.getInstance().collection("events")
                .document(event.getOrganizer())
                .collection("organizer_events")
                .document(event.id);
        // Update event with entrant information in real-time
        eventListener = eventDoc.addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) dismiss();
                    Event updatedEvent = snapshot.toObject(Event.class);
                    updateEvent(updatedEvent);
                });
    }

    /**
     * Updates the fragment on entrant interaction
     */
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

    /**
     * Triggered by Admins, will require confirmation press
     * Removes event from database on completion
     */
    public void removeEventSequence(){
        binding.fragmentEventDisplayCancelButton.setText("Confirm...");
        binding.fragmentEventDisplayCancelButton.setBackgroundColor(Color.LTGRAY);
        binding.fragmentEventDisplayCancelButton.setClickable(false);
        // Wait 3 seconds and then allow deletion
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.fragmentEventDisplayCancelButton.setBackgroundColor(Color.RED);
            binding.fragmentEventDisplayCancelButton.setOnClickListener(v -> {
                eventDoc.delete().addOnSuccessListener(aVoid -> {
                    // Document successfully deleted
                    dismiss();
                }).addOnFailureListener(e -> {
                    // Handle deletion failure
                    binding.fragmentEventDisplayCancelButton.setText("Failed! Try again?");
                });
            });
            binding.fragmentEventDisplayCancelButton.setClickable(true);
        }, 3000);

    }


    /**
     * Updates display information with event information
     */
    public void updateEvent(Event updatedEvent){
        event = updatedEvent;
        // Bind data
        binding.fragmentEventDisplayTitle.setText(event.getTitle());
        binding.fragmentEventDisplayOrganizer.setText(event.getOrganizer());
        binding.fragmentEventDisplayDescription.setText(event.getDescription());
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
    }

    /**
    * Initialize buttons based on the user role
    * @param userID - String containing the users ID
    * @param role - String containing the user's role
    *
    */
    public void initalizeButtons(String userID, String role){
        if (role.equalsIgnoreCase("admin")){
            binding.fragmentEventDisplayCancelButton.setText("Remove...");
            binding.fragmentEventDisplayCancelButton.setBackgroundColor(Color.RED);
            binding.fragmentEventDisplayCancelButton.setOnClickListener(v -> {
                removeEventSequence();
            });

            // Register button click
            binding.fragmentEventDisplayRegisterButton.setVisibility(GONE);
        }
        else if (role.equalsIgnoreCase("organizer")){
            binding.fragmentEventDisplayCancelButton.setText("Edit Event");
            binding.fragmentEventDisplayCancelButton.setOnClickListener(v -> {
                // Transition to edit event fragment
            });

            // Register button click
            binding.fragmentEventDisplayRegisterButton.setText("Manage Event");
            binding.fragmentEventDisplayRegisterButton.setOnClickListener(v -> {
                // Transition to manage event fragment
            });
        }
        else if (role.equalsIgnoreCase("entrant")){
            // Allow entrant to register/cancel, if event is not closed
            // If event is closed allow entrant to interact with invitation if received
            binding.fragmentEventDisplayCancelButton.setOnClickListener(v -> {
                event.getLottery().removeEntrant(userID);
                eventDoc.set(event);
                updateFragment(false);
            });

            // Register button click
            binding.fragmentEventDisplayRegisterButton.setOnClickListener(v -> {
                event.getLottery().addEntrant(userID);
                eventDoc.set(event);
                updateFragment(true);
            });
            updateFragment(event.getLottery().containsEntrant(userID));
        }

    }

    @Nullable
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        binding = FragmentEventDisplayBinding.inflate(LayoutInflater.from(getContext()));
        dialog.setContentView(binding.getRoot());

        SharedPreferences currentUser = requireContext().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        String userID = currentUser.getString("UID", "John");
        String role = currentUser.getString("Role", "entrant");

        updateEvent(event);

        // Close dialog
        binding.fragmentEventDisplayClose.setOnClickListener(v -> dismiss());

        // Set button functionality based on role
        initalizeButtons(userID, role);
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
        if (eventListener != null) eventListener.remove();
        binding = null;
    }
}
