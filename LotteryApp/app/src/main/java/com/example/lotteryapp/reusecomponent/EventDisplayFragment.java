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

import androidx.core.content.ContextCompat;
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
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.material.color.MaterialColors;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
//    private void updateFragment(boolean inEvent){
//        if (!inEvent){
//            if (!event.isOpen().equals("Open")){
//                binding.fragmentEventDisplayRegisterButton.setBackgroundColor(Color.LTGRAY);
//                binding.fragmentEventDisplayRegisterButton.setText("Closed");
//                binding.fragmentEventDisplayRegisterButton.setClickable(false);
//                binding.fragmentEventDisplayCancelButton.setVisibility(GONE);
//                return;
//            }
//            if (event.getLottery().isFull()){
//                binding.fragmentEventDisplayRegisterButton.setBackgroundColor(Color.LTGRAY);
//                binding.fragmentEventDisplayRegisterButton.setText("Event Full!");
//                binding.fragmentEventDisplayRegisterButton.setClickable(false);
//                binding.fragmentEventDisplayCancelButton.setVisibility(GONE);
//                return;
//            }
//            binding.fragmentEventDisplayRegisterButton.setBackgroundColor(Color.YELLOW);
//            binding.fragmentEventDisplayRegisterButton.setText("Register");
//            binding.fragmentEventDisplayRegisterButton.setClickable(true);
//            binding.fragmentEventDisplayCancelButton.setVisibility(GONE);
//            return;
//        }
//        binding.fragmentEventDisplayRegisterButton.setBackgroundColor(Color.LTGRAY);
//        binding.fragmentEventDisplayRegisterButton.setText("Awaiting Result...");
//        binding.fragmentEventDisplayRegisterButton.setClickable(false);
//        binding.fragmentEventDisplayCancelButton.setVisibility(VISIBLE);
//
//    }

    /**
     * Triggered by Admins, will require confirmation press
     * Removes event from database on completion
     */
    public void removeEventSequence(Button deleteButton){
        deleteButton.setText("Confirm...");
        deleteButton.setBackgroundColor(Color.LTGRAY);
        deleteButton.setClickable(false);
        // Wait 3 seconds and then allow deletion
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            deleteButton.setBackgroundColor(Color.RED);
            deleteButton.setOnClickListener(v -> {
                eventDoc.delete().addOnSuccessListener(aVoid -> {
                    // Document successfully deleted
                    dismiss();
                }).addOnFailureListener(e -> {
                    // Handle deletion failure
                    deleteButton.setText("Failed! Try again?");
                });
            });
            deleteButton.setClickable(true);
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
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true);
        int defColor = typedValue.data;

        if (role.equalsIgnoreCase("admin")){
            binding.fragmentEventDisplayTopButton.setVisibility(VISIBLE);
            binding.fragmentEventDisplayTopButton.setText("Remove...");
            binding.fragmentEventDisplayTopButton.setBackgroundColor(Color.RED);
            binding.fragmentEventDisplayTopButton.setClickable(true);
            binding.fragmentEventDisplayTopButton.setOnClickListener(v -> {
                removeEventSequence((Button) v);
            });

            binding.fragmentEventDisplayBottomButton.setVisibility(GONE);
        }
        else if (role.equalsIgnoreCase("organizer")){
            binding.fragmentEventDisplayTopButton.setVisibility(VISIBLE);
            binding.fragmentEventDisplayTopButton.setText("Edit Event");
            binding.fragmentEventDisplayTopButton.setBackgroundColor(defColor);
            binding.fragmentEventDisplayTopButton.setClickable(true);
            binding.fragmentEventDisplayTopButton.setOnClickListener(v -> {
                new OrganizerEditEventFragment(event).show(manager, "event_display");
            });

            binding.fragmentEventDisplayBottomButton.setVisibility(VISIBLE);
            binding.fragmentEventDisplayBottomButton.setText("Manage Event");
            binding.fragmentEventDisplayBottomButton.setBackgroundColor(defColor);
            binding.fragmentEventDisplayBottomButton.setClickable(true);
            binding.fragmentEventDisplayBottomButton.setOnClickListener(v -> {
                OrganizerManageEventFragment manageEvent = new OrganizerManageEventFragment(event);
                manageEvent.show(getParentFragmentManager(), "ManageEventFragment");
            });
        }
        else if (role.equalsIgnoreCase("entrant")){
            if (!event.getLottery().containsEntrant(userID)) {
                binding.fragmentEventDisplayTopButton.setVisibility(VISIBLE);
                binding.fragmentEventDisplayTopButton.setText("Register");
                binding.fragmentEventDisplayTopButton.setBackgroundColor(defColor);
                binding.fragmentEventDisplayTopButton.setClickable(true);
                binding.fragmentEventDisplayTopButton.setOnClickListener(v -> {
                    if (event.getLottery().addEntrant(userID)){
                        eventDoc.set(event);
                        profileDoc.update("registeredLotteries", FieldValue.arrayUnion(event.getOrganizer() + "," + event.id));
                        initalizeButtons(role);
                    }
                });

                binding.fragmentEventDisplayBottomButton.setVisibility(GONE);
                if (!event.isOpen().equals("Open")) {
                    binding.fragmentEventDisplayTopButton.setBackgroundColor(Color.LTGRAY);
                    binding.fragmentEventDisplayTopButton.setText("Closed");
                    binding.fragmentEventDisplayTopButton.setClickable(false);
                } else if (event.getLottery().isFull()) {
                    binding.fragmentEventDisplayTopButton.setBackgroundColor(Color.LTGRAY);
                    binding.fragmentEventDisplayTopButton.setText("Event Full!");
                    binding.fragmentEventDisplayTopButton.setClickable(false);
                } else {
                    binding.fragmentEventDisplayTopButton.setBackgroundColor(Color.YELLOW);
                    binding.fragmentEventDisplayTopButton.setText("Register");
                    binding.fragmentEventDisplayTopButton.setClickable(true);
                }

            } else {
                switch (event.getLottery().entrantStatus.get(event.getLottery().getEntrants().indexOf(userID))) {
                    case Registered:
                        binding.fragmentEventDisplayTopButton.setVisibility(VISIBLE);
                        binding.fragmentEventDisplayTopButton.setText("Awaiting Results...");
                        binding.fragmentEventDisplayTopButton.setBackgroundColor(Color.LTGRAY);
                        binding.fragmentEventDisplayTopButton.setClickable(false);

                        binding.fragmentEventDisplayBottomButton.setVisibility(VISIBLE);
                        binding.fragmentEventDisplayBottomButton.setText("Cancel");
                        binding.fragmentEventDisplayBottomButton.setBackgroundColor(Color.RED);
                        binding.fragmentEventDisplayBottomButton.setClickable(true);
                        binding.fragmentEventDisplayBottomButton.setOnClickListener(v -> {
                            event.getLottery().removeEntrant(userID);
                            eventDoc.set(event);
                            profileDoc.update("registeredLotteries", FieldValue.arrayRemove(event.getOrganizer() + "," + event.id));
                            initalizeButtons(role);
                        });
                        break;
                    case Waitlisted:
                        binding.fragmentEventDisplayTopButton.setVisibility(VISIBLE);
                        binding.fragmentEventDisplayTopButton.setText("Waitlisted");
                        binding.fragmentEventDisplayTopButton.setBackgroundColor(Color.parseColor("#FF7A41"));
                        binding.fragmentEventDisplayTopButton.setClickable(false);

                        binding.fragmentEventDisplayBottomButton.setVisibility(VISIBLE);
                        binding.fragmentEventDisplayBottomButton.setText("Cancel");
                        binding.fragmentEventDisplayBottomButton.setBackgroundColor(Color.RED);
                        binding.fragmentEventDisplayBottomButton.setClickable(true);
                        binding.fragmentEventDisplayBottomButton.setOnClickListener(v -> {
                            event.getLottery().entrantStatus.set(event.getLottery().getEntrants().indexOf(userID), LotteryEntrant.Status.Cancelled);
                            eventDoc.set(event);
                            initalizeButtons(role);
                        });
                        break;
                    case Cancelled:
                        binding.fragmentEventDisplayTopButton.setVisibility(VISIBLE);
                        binding.fragmentEventDisplayTopButton.setText("Cancelled");
                        binding.fragmentEventDisplayTopButton.setBackgroundColor(Color.RED);
                        binding.fragmentEventDisplayTopButton.setClickable(false);

                        binding.fragmentEventDisplayBottomButton.setVisibility(GONE);
                        break;
                    case Declined:
                        binding.fragmentEventDisplayTopButton.setVisibility(VISIBLE);
                        binding.fragmentEventDisplayTopButton.setText("Declined");
                        binding.fragmentEventDisplayTopButton.setBackgroundColor(Color.RED);
                        binding.fragmentEventDisplayTopButton.setClickable(false);

                        binding.fragmentEventDisplayBottomButton.setVisibility(GONE);
                        break;
                    case Accepted:
                        binding.fragmentEventDisplayTopButton.setVisibility(VISIBLE);
                        binding.fragmentEventDisplayTopButton.setText("Accepted");
                        binding.fragmentEventDisplayTopButton.setBackgroundColor(Color.GREEN);
                        binding.fragmentEventDisplayTopButton.setClickable(false);

                        binding.fragmentEventDisplayBottomButton.setVisibility(GONE);
                        break;
                    case Invited:
                        binding.fragmentEventDisplayTopButton.setVisibility(VISIBLE);
                        binding.fragmentEventDisplayTopButton.setText("Accept");
                        binding.fragmentEventDisplayTopButton.setBackgroundColor(Color.GREEN);
                        binding.fragmentEventDisplayTopButton.setClickable(true);
                        binding.fragmentEventDisplayTopButton.setOnClickListener(v -> {
                            event.getLottery().entrantStatus.set(event.getLottery().getEntrants().indexOf(userID), LotteryEntrant.Status.Accepted);
                            eventDoc.set(event);
                            initalizeButtons(role);
                        });

                        binding.fragmentEventDisplayBottomButton.setVisibility(VISIBLE);
                        binding.fragmentEventDisplayBottomButton.setText("Decline");
                        binding.fragmentEventDisplayBottomButton.setBackgroundColor(Color.RED);
                        binding.fragmentEventDisplayBottomButton.setClickable(true);
                        binding.fragmentEventDisplayBottomButton.setOnClickListener(v -> {
                            event.getLottery().entrantStatus.set(event.getLottery().getEntrants().indexOf(userID), LotteryEntrant.Status.Declined);
                            draw();
                            eventDoc.set(event);
                            initalizeButtons(role);
                        });
                        break;
                }
            }
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

    private void draw() {
        ArrayList<LotteryEntrant.Status> entrantStatus = event.getLottery().entrantStatus;
        ArrayList<Integer> RegOrWaitlistedEntrants = new ArrayList<>();
        for (int i = 0; i < entrantStatus.size(); ++i) {
            LotteryEntrant.Status status = entrantStatus.get(i);
            if (status == LotteryEntrant.Status.Registered || status == LotteryEntrant.Status.Waitlisted)
                RegOrWaitlistedEntrants.add(i);
        }
        if (RegOrWaitlistedEntrants.isEmpty()) return;

        // Unique random number generator from: https://www.baeldung.com/java-unique-random-numbers
        List<Integer> uniqueRandom = new ArrayList<>();
        for (int i = 0; i < RegOrWaitlistedEntrants.size(); ++i)
            uniqueRandom.add(i);
        Collections.shuffle(uniqueRandom);

        Notification nInvitation = Notification.constructSuccessNotification(event.getTitle(), event.getOrganizer(), Notification.SenderRole.ORGANIZER, event.id);
        //nInvitation.maskCorrespondence(currentUser.getString("name", "John"));
        if (uniqueRandom.size() > event.getMaxCapacity() - (event.getNentrants() - RegOrWaitlistedEntrants.size()))
            uniqueRandom = uniqueRandom.subList(0, event.getMaxCapacity() - (event.getNentrants() - RegOrWaitlistedEntrants.size()));

        uniqueRandom.forEach(unique -> {
            int unpackedIDX = RegOrWaitlistedEntrants.get(unique);
            nInvitation.sendNotification(event.getLottery().getEntrants().get(unpackedIDX));
            entrantStatus.set(unpackedIDX, LotteryEntrant.Status.Invited);
        });
    }
}
