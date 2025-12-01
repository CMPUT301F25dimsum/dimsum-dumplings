package com.example.lotteryapp.organizer;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.example.lotteryapp.R;
import com.example.lotteryapp.databinding.FragmentOrganizerCreateBinding;
import com.example.lotteryapp.reusecomponent.EditableImage;
import com.example.lotteryapp.reusecomponent.Event;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * OrganizerEditEventFragment
 *
 * Description:
 *  Fragment that displays the create event fragment but as a pop-up
 *  and modifies it slightly for editing of an existing event.
 *  Author: Ben Fisher
 */

public class OrganizerEditEventFragment extends DialogFragment {

    private FragmentOrganizerCreateBinding binding;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private CollectionReference db_events;
    private String organizerID;

    private Event editingEvent;

    private Calendar eventDate;
    private Calendar registrationStart;
    private Calendar registrationEnd;
    private SimpleDateFormat simpleDateFormat;

    private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

    public OrganizerEditEventFragment(Event event) {
        simpleDateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.CANADA);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        editingEvent = event;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        binding = FragmentOrganizerCreateBinding.inflate(LayoutInflater.from(getContext()));
        dialog.setContentView(binding.getRoot());

        organizerID = requireContext().getSharedPreferences("loginInfo", Context.MODE_PRIVATE)
                .getString("UID", "John");
        db_events = db.collection("events")
                .document(organizerID).collection("organizer_events");

        // Preload fields if editingEvent exists
        if (editingEvent != null) {
            preloadEventData(editingEvent);
        }

        SwitchCompat limSizeSwitch = binding.fragmentOrganizerCreateLimitedWaiting;
        TextView limitedSizeAmount = binding.fragmentOrganizerCreateLimWaitingSize;

        limSizeSwitch.setOnCheckedChangeListener((bv, isChecked) -> {
            if (isChecked) {
                limitedSizeAmount.setVisibility(VISIBLE);
            } else {
                limitedSizeAmount.setVisibility(GONE);
            }
        });

        // Save button
        binding.fragmentOrganizerCreateConfButton.setText("Update");
        binding.fragmentOrganizerCreateConfButton.setOnClickListener(v -> onSave());

        binding.fragmentOrganizerCreateBanner.registerLauncher(this);
        return dialog;
    }

    private void preloadEventData(Event event) {
        // Reset invalid text
        binding.fragmentOrganizerCreateInvalidEventText.setVisibility(GONE);

        binding.fragmentOrganizerCreateTitle.setText(event.getTitle());
        binding.fragmentOrganizerCreateDescription.setText(event.getDescription());
        binding.fragmentOrganizerCreateLocation.setText(event.getEventLocation());
        binding.fragmentOrganizerCreateLotterySize.setText(String.valueOf(event.getMaxCapacity()));

        // Preload dates
        eventDate = Calendar.getInstance();
        eventDate.setTime(event.getEventTime());
        registrationStart = Calendar.getInstance();
        registrationStart.setTime(event.getLottery().registrationStart);
        registrationEnd = Calendar.getInstance();
        registrationEnd.setTime(event.getLottery().registrationEnd);

        binding.fragmentOrganizerCreateDate.setText(simpleDateFormat.format(event.getEventTime()));
        binding.fragmentOrganizerCreateDeadlineStart.setText(simpleDateFormat.format(registrationStart.getTime()));
        binding.fragmentOrganizerCreateDeadlineEnd.setText(simpleDateFormat.format(registrationEnd.getTime()));

        binding.fragmentOrganizerCreateLimitedWaiting.setChecked((editingEvent.getRegistrationLimit() >= 0));
        binding.fragmentOrganizerCreateLimWaitingSize.setText(String.valueOf(editingEvent.getLottery().getMaxEntrants()));
        binding.fragmentOrganizerCreateRequireLocation.setChecked(editingEvent.isValidateLocation());
        if((editingEvent.getRegistrationLimit() >= 0))
            binding.fragmentOrganizerCreateLimWaitingSize.setVisibility(VISIBLE);

        // Reset ImageViews (e.g., EditableImage banners)
        if (editingEvent.getBannerURL() != null)
            FirebaseStorage.getInstance().getReference()
                    .child(editingEvent.getBannerURL()).getBytes(1024*1024)
                    .addOnSuccessListener(bytes -> {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        binding.fragmentOrganizerCreateBanner.setImage(bitmap);
                    });

        StringBuilder filters = new StringBuilder();
        for (String filter : editingEvent.getFilters()){
            filters.append(filter);
        }
        binding.fragmentOrganizerCreateFilter.setText(filters);
    }

    private void fillEvent() throws IllegalStateException {
        Uri image = binding.fragmentOrganizerCreateBanner.getImage();
        if (image != null){
            // if image is null, image has not been changed
            String filePath = "images/" + organizerID + System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = storage.getReference().child(filePath);
            imageRef.putFile(image);

            editingEvent.setBannerURL(filePath);
        }
        // Load text inputs
        editingEvent.setTitle(binding.fragmentOrganizerCreateTitle.getText().toString());
        editingEvent.setDescription(binding.fragmentOrganizerCreateDescription.getText().toString());
        editingEvent.setEventLocation(binding.fragmentOrganizerCreateLocation.getText().toString());

        // Load date inputs
        editingEvent.setEventTime(eventDate.getTime());
        editingEvent.getLottery().registrationStart = registrationStart.getTime();
        editingEvent.getLottery().registrationEnd = registrationEnd.getTime();

        // Load integer inputs (ensure do not parse null)
        String maxCapacity = binding.fragmentOrganizerCreateLotterySize.getText().toString();
        String limitedWaiting = binding.fragmentOrganizerCreateLimWaitingSize.getText().toString();

        if (!maxCapacity.isEmpty()) {
            editingEvent.setMaxCapacity(Integer.parseInt(maxCapacity));
        } else {
            editingEvent.setMaxCapacity(-1);
        }

        if (binding.fragmentOrganizerCreateLimitedWaiting.isChecked()) {
            if (!limitedWaiting.isEmpty()) {
                editingEvent.setRegistrationLimit(Integer.parseInt(limitedWaiting));
            } else {
                throw new IllegalStateException("Limit size should be set if Limitation is set to on");
            }
        }

        // Require Location?
        editingEvent.setValidateLocation(binding.fragmentOrganizerCreateRequireLocation.isChecked());

        // Load filters
        String filters = binding.fragmentOrganizerCreateFilter.getText().toString();
        if (!filters.contains("#")) return;

        for (String filter : filters.split("#")) {
            if (!filter.isEmpty()) editingEvent.addFilter("#" + filter);
        }
    }
    private void onSave() {
        if (editingEvent == null) return;
        try {
            fillEvent();
            editingEvent.isValid();
            // Save to Firestore
            db_events.document(editingEvent.id)
                    .set(editingEvent)
                    .addOnSuccessListener(aVoid -> {
                        dismiss();
                    })
                    .addOnFailureListener(e -> {
                        binding.fragmentOrganizerCreateInvalidEventText.setText(e.getMessage());
                        binding.fragmentOrganizerCreateInvalidEventText.setVisibility(VISIBLE);
                    });
        }
        catch (IllegalStateException e){
            // Display error message
            binding.fragmentOrganizerCreateInvalidEventText.setText(e.getMessage());
            binding.fragmentOrganizerCreateInvalidEventText.setVisibility(VISIBLE);
        }
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
