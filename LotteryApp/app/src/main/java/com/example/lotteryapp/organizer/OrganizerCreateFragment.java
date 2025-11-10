package com.example.lotteryapp.organizer;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lotteryapp.R;
import com.example.lotteryapp.reusecomponent.EditableImage;
import com.example.lotteryapp.reusecomponent.Event;
import com.example.lotteryapp.reusecomponent.Image;
import com.example.lotteryapp.reusecomponent.QR;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.WriterException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Purpose: To configure event creation, which is uploaded to Firebase. Also generates a QR code
 * <p>
 * Outstanding Issues: Images not saved to database
 */

public class OrganizerCreateFragment extends Fragment {

    private EditableImage banner;
    private FirebaseFirestore db;
    private CollectionReference db_events;
    String dateFormat = "yyyy/MM/dd HH:mm:ss";

    Calendar eventDate;
    Calendar registrationStart;
    Calendar registrationEnd;

    public OrganizerCreateFragment() {

    }

    private void fillEvent(View root, Event e) throws IllegalStateException {
        // Load text inputs
        e.setTitle(
                ((TextView) root.findViewById(R.id.fragment_organizer_create_title))
                        .getText().toString());
        e.setDescription(
                ((TextView) root.findViewById(R.id.fragment_organizer_create_description))
                        .getText().toString());

        e.setEventLocation(
                ((TextView) root.findViewById(R.id.fragment_organizer_create_location))
                        .getText().toString());
        // Load date inputs
        e.setEventTime(eventDate.getTime());
        e.getLottery().registrationStart = registrationStart.getTime();
        e.getLottery().registrationEnd = registrationEnd.getTime();

        // Load integer inputs (ensure do not parse null)
        String maxCapacity = ((TextView) root.findViewById(R.id.fragment_organizer_create_lottery_size)).getText().toString();
        String limitedWaiting = ((TextView) root.findViewById(R.id.fragment_organizer_create_lim_waiting_size)).getText().toString();
        if (!maxCapacity.isEmpty())
            e.setMaxCapacity(Integer.parseInt(maxCapacity));
        else
            e.setMaxCapacity(-1);
        if (((SwitchCompat) root.findViewById(R.id.fragment_organizer_create_limited_waiting)).isChecked()){
            if(!limitedWaiting.isEmpty())
                e.setRegistrationLimit(Integer.parseInt(limitedWaiting));
            else
                throw new IllegalStateException("Limit size should be set if Limitation is set to on");
        }


        // Require Location?
        e.setValidateLocation(((SwitchCompat) root.findViewById(R.id.fragment_organizer_create_require_location)).isChecked());

        // Load filters
        String filters = ((TextView) root.findViewById(R.id.fragment_organizer_create_filter)).getText().toString();
        if (!filters.contains("#"))
            return;
        for (String filter : filters.split("#")){
            if (!filter.isEmpty())
                e.addFilter("#" + filter);
        }
    }

    private void clearAllFields(View root) {
        // EditTexts
        ((EditText) root.findViewById(R.id.fragment_organizer_create_title)).setText("");
        ((EditText) root.findViewById(R.id.fragment_organizer_create_description)).setText("");
        ((EditText) root.findViewById(R.id.fragment_organizer_create_location)).setText("");
        ((EditText) root.findViewById(R.id.fragment_organizer_create_lottery_size)).setText("");
        ((EditText) root.findViewById(R.id.fragment_organizer_create_lim_waiting_size)).setText("");
        ((EditText) root.findViewById(R.id.fragment_organizer_create_filter)).setText("");
        ((EditText) root.findViewById(R.id.fragment_organizer_create_date)).setText("");
        ((EditText) root.findViewById(R.id.fragment_organizer_create_deadline_start)).setText("");
        ((EditText) root.findViewById(R.id.fragment_organizer_create_deadline_end)).setText("");

        // Switches
        ((SwitchCompat) root.findViewById(R.id.fragment_organizer_create_limited_waiting)).setChecked(false);

        // Optionally hide dependent views
        root.findViewById(R.id.fragment_organizer_create_lim_waiting_size_text).setVisibility(View.GONE);
        root.findViewById(R.id.fragment_organizer_create_lim_waiting_size).setVisibility(View.GONE);

        // Reset ImageViews (e.g., EditableImage banners)
        ((EditableImage) root.findViewById(R.id.fragment_organizer_create_banner)).reset();

        // Reset invalid text
        root.findViewById(R.id.fragment_organizer_create_invalid_event_text).setVisibility(GONE);
    }

    /* Creates a chain of views to pick date and time
    * Referenced from https://stackoverflow.com/a/35745881
     */
    public void showDateTimePicker(Context context, Calendar date, EditText textView) {
        final Calendar currentDate = Calendar.getInstance();
        new DatePickerDialog(context,
                (DatePickerDialog.OnDateSetListener) (view, year, monthOfYear, dayOfMonth) -> {
            date.set(year, monthOfYear, dayOfMonth);
            new TimePickerDialog(context,
                    (TimePickerDialog.OnTimeSetListener) (view1, hourOfDay, minute) -> {
                date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                date.set(Calendar.MINUTE, minute);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.CANADA);
                textView.setText(simpleDateFormat.format(date.getTime()));
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();

        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }

    private void showQrPopup(Bitmap qrBitmap) {
        // Create an ImageView and set the bitmap
        ImageView imageView = new ImageView(getContext());
        imageView.setImageBitmap(qrBitmap);
        imageView.setAdjustViewBounds(true); // scale correctly
        imageView.setPadding(50, 50, 50, 50);

        // Build the AlertDialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Event QR Code")
                .setView(imageView)
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        SharedPreferences currentUser = requireContext().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);

        View ret = inflater.inflate(R.layout.fragment_organizer_create, container, false);

        String organizerID = currentUser.getString("UID", "John");
        Event event = new Event(organizerID);
        db = FirebaseFirestore.getInstance();
        db_events = db.collection("events");
        eventDate = Calendar.getInstance();
        registrationStart = Calendar.getInstance();
        registrationEnd = Calendar.getInstance();

        banner = ret.findViewById(R.id.fragment_organizer_create_banner);

        SwitchCompat limSizeSwitch = ret.findViewById(R.id.fragment_organizer_create_limited_waiting);
        TextView limitedSizeText = ret.findViewById(R.id.fragment_organizer_create_lim_waiting_size_text);
        TextView limitedSizeAmount = ret.findViewById(R.id.fragment_organizer_create_lim_waiting_size);

        TextView invalidText = ret.findViewById(R.id.fragment_organizer_create_invalid_event_text);
        invalidText.setVisibility(GONE);

        limSizeSwitch.setOnCheckedChangeListener((bv, isChecked) -> {
            if (isChecked) {
                limitedSizeText.setVisibility(VISIBLE);
                limitedSizeAmount.setVisibility(VISIBLE);
            } else {
                limitedSizeText.setVisibility(GONE);
                limitedSizeAmount.setVisibility(GONE);
            }
        });

        EditText eventDateView = ret.findViewById(R.id.fragment_organizer_create_date);
        eventDateView.setOnClickListener( v -> {
            showDateTimePicker(getContext(), eventDate, eventDateView);
        });

        EditText startDateView = ret.findViewById(R.id.fragment_organizer_create_deadline_start);
        startDateView.setOnClickListener( v -> {
            showDateTimePicker(getContext(), registrationStart, startDateView);
        });

        EditText endDateView = ret.findViewById(R.id.fragment_organizer_create_deadline_end);
        endDateView.setOnClickListener( v -> {
            showDateTimePicker(getContext(), registrationEnd, endDateView);
        });

        // Snapshot listener not required for creation, only for viewing
        Button conf_button = ret.findViewById(R.id.fragment_organizer_create_conf_button);
        conf_button.setOnClickListener(v -> {
            try {
                fillEvent(ret, event);
                event.isValid(); // Throws IllegalStateException when invalid
                // Upload image
                String encodedImage = banner.encodeImageUriToBase64(getContext());

                if (encodedImage != null){
                    DocumentReference imageRef = db.collection("images").document();
                    Image im = new Image(encodedImage);
//                    imageRef.set(im).addOnSuccessListener(
//                            aVoid -> {
//
//                            }
//                    ).addOnFailureListener( aVoid -> {
//                        // Display error message
//                        invalidText.setText("Failed to push image to database");
//                        invalidText.setVisibility(VISIBLE);
//                        }
//
//                    );
//                    String imageId = imageRef.getId();
//                    event.setBannerURL(imageId);

                    // Upload event
                    DocumentReference docRef = db_events.document(organizerID)
                            .collection("organizer_events").document();

                    event.id = docRef.getId();
                    docRef.set(event)
                            .addOnSuccessListener(
                                    eventVoid -> {
                                        try {
                                            Bitmap qrCode = QR.generateQrCode("lotteryapp://event?eid=" + event.id,  512);
                                            showQrPopup(qrCode);
                                            clearAllFields(ret);
                                            // TODO: Link Event ID to organizer profile
                                        } catch (WriterException e) {
                                            // Display error message
                                            invalidText.setText("Event pushed to database, failed to create QE code");
                                            invalidText.setVisibility(VISIBLE);
                                        }
                                    })
                            .addOnFailureListener(
                                    eventVoid -> {
                                        // Display error message
                                        invalidText.setText("Failed to push event to database");
                                        invalidText.setVisibility(VISIBLE);
                                    });
                }
                else{
                    throw new IllegalStateException("Please select a banner image from your device");
                }
            } catch (IllegalStateException e) {
                // Display error message
                invalidText.setText(e.getMessage());
                invalidText.setVisibility(VISIBLE);
            }
        });
        return ret;
    }

    /// ChatGPT generated lines 63-70 for swappable images
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        banner.registerLauncher(this);
    }
}