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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.lotteryapp.R;
import com.example.lotteryapp.databinding.FragmentEventDisplayBinding;
import com.example.lotteryapp.organizer.OrganizerCreateFragment;
import com.example.lotteryapp.organizer.OrganizerEditEventFragment;
import com.example.lotteryapp.organizer.OrganizerManageEventFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Purpose: Display the details of an event model. This fragment is used in pop-ups, such as notification callback
 *
 * Issues: None
 */
public class EventDisplayFragment extends DialogFragment {

    private FragmentEventDisplayBinding binding;
    private Event event;
    private final DocumentReference eventDoc;
    private String userID;
    private DocumentReference profileDoc;

    private final String dateFormat = "yyyy/MM/dd HH:mm:ss";
    private final SimpleDateFormat formatter;
    private final ListenerRegistration eventListener;
    private FragmentManager manager;

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

    public EventDisplayFragment(Event event, FragmentManager fragmentManager){
        this(event);
        manager = fragmentManager;
    }

    /**
     * Updates the fragment on entrant interaction
     */
    private void updateFragment(boolean inEvent){
        if (!inEvent){
            if (!event.isOpen().equals("Open")){
                binding.fragmentEventDisplayRegisterButton.setBackgroundColor(Color.LTGRAY);
                binding.fragmentEventDisplayRegisterButton.setText("Closed");
                binding.fragmentEventDisplayRegisterButton.setClickable(false);
                binding.fragmentEventDisplayCancelButton.setVisibility(GONE);
                return;
            }
            if (event.getLottery().isFull()){
                binding.fragmentEventDisplayRegisterButton.setBackgroundColor(Color.LTGRAY);
                binding.fragmentEventDisplayRegisterButton.setText("Event Full!");
                binding.fragmentEventDisplayRegisterButton.setClickable(false);
                binding.fragmentEventDisplayCancelButton.setVisibility(GONE);
                return;
            }
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
        if (event.getBannerURL() != null)
            FirebaseStorage.getInstance().getReference()
                    .child(event.getBannerURL()).getBytes(1024*1024)
                    .addOnSuccessListener(bytes -> {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        binding.fragmentEventDisplayBanner.setImageBitmap(bitmap);
                    });
        // Bind data
        binding.fragmentEventDisplayTitle.setText(event.getTitle());
        binding.fragmentEventDisplayOrganizer.setText(event.getOrganizer());
        binding.fragmentEventDisplayDescription.setText(event.getDescription());
        binding.fragmentEventDisplayLocation.setText(event.getEventLocation());
        binding.fragmentEventDisplayDate.setText(formatter.format(event.getEventTime()));
        binding.fragmentEventDisplaySelectionCount.setText(Integer.toString(event.getMaxCapacity()));
        if (event.getLottery().registrationStart != null)
            binding.fragmentEventDisplayRegStart.setText(formatter.format(event.getLottery().registrationStart));
        if (event.getLottery().registrationEnd != null)
            binding.fragmentEventDisplayRegEnd.setText(formatter.format(event.getLottery().registrationEnd));

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
        // Set the filters
        StringBuilder filters = new StringBuilder();
        for (String filter : event.getFilters()){
            filters.append(filter);
        }
        binding.fragmentEventDisplayFilter.setText(filters);
    }

    /**
    * Initialize buttons based on the user role
    * @param role - String containing the user's role
    *
    */
    public void initalizeButtons(String role){
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
                new OrganizerEditEventFragment(event).show(manager, "event_display");
            });

            // Register button click
            binding.fragmentEventDisplayRegisterButton.setText("Manage Event");
            binding.fragmentEventDisplayRegisterButton.setOnClickListener(v -> {
                // Transition to manage event fragment
                OrganizerManageEventFragment manageEvent = new OrganizerManageEventFragment(event);
                manageEvent.show(getParentFragmentManager(), "ManageEventFragment");
            });
        }
        else if (role.equalsIgnoreCase("entrant")){
            // Allow entrant to register/cancel, if event is not closed
            binding.fragmentEventDisplayCancelButton.setOnClickListener(v -> {
                event.getLottery().removeEntrant(userID);
                eventDoc.set(event);
                updateFragment(false);
                profileDoc.update("registeredLotteries", FieldValue.arrayRemove(event.getOrganizer() + "," + event.id));
            });

            // Register button click
            binding.fragmentEventDisplayRegisterButton.setOnClickListener(v -> {
                if (event.getLottery().addEntrant(userID)){
                    eventDoc.set(event);
                    updateFragment(true);
                    profileDoc.update("registeredLotteries", FieldValue.arrayUnion(event.getOrganizer() + "," + event.id));
                }
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

        userID = currentUser.getString("UID", "John");
        String role = currentUser.getString("Role", "organizer");

        profileDoc = FirebaseFirestore.getInstance().collection("user").document(userID);

        updateEvent(event);

        // Close dialog
        binding.fragmentEventDisplayClose.setOnClickListener(v -> dismiss());

        // Set button functionality based on role
        initalizeButtons(role);
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
