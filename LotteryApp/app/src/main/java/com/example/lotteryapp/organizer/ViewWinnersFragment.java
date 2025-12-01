package com.example.lotteryapp.organizer;

import android.app.Dialog;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotteryapp.R;
import com.example.lotteryapp.reusecomponent.LotteryEntrant;
import com.example.lotteryapp.reusecomponent.UserProfile;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ViewWinnersFragment
 *
 * Description:
 *  Frgament tha allows oranizers to view individual entrants who have
 *  accepted their invitations and are now locked in for the event.
 *  Author: John Alva
 */

public class ViewWinnersFragment extends DialogFragment {

    private ArrayList<LotteryEntrant> winners;
    private String eventName;
    private FirebaseFirestore db;

    public ViewWinnersFragment(ArrayList<LotteryEntrant> winners, String eventName) {
        this.winners = winners;
        this.eventName = eventName;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_winners, container, false);

        Button closeButton = view.findViewById(R.id.view_winners_close_button);
        closeButton.setOnClickListener(v -> dismiss());

        Button downloadButton = view.findViewById(R.id.download_to_file_button);
        downloadButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Preparing download...", Toast.LENGTH_SHORT).show();
            downloadWinnersToFile();
        });

        RecyclerView recyclerView = view.findViewById(R.id.winners_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ViewWinnersAdapter adapter = new ViewWinnersAdapter(winners);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void downloadWinnersToFile() {
        if (winners == null || winners.isEmpty()) {
            Toast.makeText(getContext(), "No winners to download.", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        final StringBuilder content = new StringBuilder();
        content.append("Lottery Winners List\n\n");
        content.append("Event: ").append(eventName).append("\n");
        content.append("Date Generated: ").append(currentDate).append("\n\n");


        final AtomicInteger counter = new AtomicInteger(winners.size());

        for (LotteryEntrant entrant : winners) {
            db.collection("user").document(entrant.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
                        if (userProfile != null) {
                            content.append("Name: ").append(userProfile.name).append("\n");
                            content.append("Email: ").append(userProfile.email).append("\n");
                            content.append("Phone: ").append(userProfile.phone_num != null && !userProfile.phone_num.isEmpty() ? userProfile.phone_num : "N/A").append("\n");
                            content.append("Status: ").append(entrant.getStatus()).append("\n\n");
                        }
                    }
                    if (counter.decrementAndGet() == 0) {
                        saveContentToFile(content.toString());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("ViewWinnersFragment", "Error fetching user details for " + entrant.getUid(), e);
                    if (counter.decrementAndGet() == 0) {
                        saveContentToFile(content.toString());
                    }
                });
        }
    }

    private void saveContentToFile(String content) {
        if (getActivity() == null) return;

        String fileName = eventName.replaceAll("\\s+", "_") + "_winners_list.txt";

        getActivity().runOnUiThread(() -> {
            try {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                }

                Uri uri = requireContext().getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
                if (uri == null) throw new Exception("Failed to create new MediaStore record.");

                try (OutputStream os = requireContext().getContentResolver().openOutputStream(uri)) {
                    if (os == null) throw new Exception("Failed to open output stream.");
                    os.write(content.getBytes());
                }

                Toast.makeText(getContext(), "Saved to Downloads folder", Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                Log.e("ViewWinnersFragment", "Error saving file: " + e.getMessage());
                Toast.makeText(getContext(), "Error saving file", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }
}
