package com.example.lotteryapp.admin;

import static android.view.View.GONE;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lotteryapp.databinding.FragmentAdminNoticeBinding;
import com.example.lotteryapp.reusecomponent.Event;
import com.example.lotteryapp.reusecomponent.EventDisplayFragment;
import com.example.lotteryapp.reusecomponent.Notification;
import com.example.lotteryapp.reusecomponent.UserProfile;
import com.example.lotteryapp.databinding.FragmentAdminProfileBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.auth.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Purpose: Adapter for custom profile data type displayed to the admin. Allows Profile (model) to be
 * displayed as a List (view).
 * <p>
 * Outstanding Issues:
 */
public class AdminProfileRecyclerViewAdapter extends RecyclerView.Adapter<AdminProfileRecyclerViewAdapter.ViewHolder> {

    private final List<UserProfile> mValues;
    private FragmentManager fragmentManager;
    private FirebaseFirestore db;

    public AdminProfileRecyclerViewAdapter(List<UserProfile> items, FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        mValues = items;
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentAdminProfileBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        UserProfile profile = mValues.get(position);

        holder.binding.reuseProfileViewName.setText(profile.name);
        holder.binding.reuseProfileEmail.setText(profile.email);
        if (profile.acc_type == UserProfile.Type.admin)
            holder.binding.reuseProfileRole.setText("Admin");
        else if (profile.acc_type == UserProfile.Type.organizer)
            holder.binding.reuseProfileRole.setText("Organizer");
        else holder.binding.reuseProfileRole.setText("Entrant");

        holder.binding.reuseProfileButton.setOnClickListener(v -> {
            for (String registeredLottery : profile.registeredLotteries) {
                String[] organizerEvent = registeredLottery.split(",");
                assert (organizerEvent.length == 2);

                DocumentReference docRef = db.collection("events")
                        .document(organizerEvent[0])
                        .collection("organizer_events")
                        .document(organizerEvent[1]);

                //Adapted transactional deletion from ChatGPT to prevent race conditions,
                //But the algorithm to fetch, remove and put is original.
                db.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(docRef);

                    Map<String, Object> lottery = (Map<String, Object>) snapshot.get("lottery");
                    if (lottery == null) return null;
                    ArrayList<String> entrants = (ArrayList<String>) lottery.get("entrants");
                    ArrayList<String> entrantStatus = (ArrayList<String>) lottery.get("entrantStatus");
                    if (entrants == null || entrantStatus == null) return null;

                    int idx = entrants.indexOf(profile.uid);
                    if (idx != -1) {
                        entrants.remove(idx);
                        entrantStatus.remove(idx);

                        lottery.put("entrants", entrants);
                        lottery.put("entrantStatus", entrantStatus);

                        transaction.update(docRef, "lottery", lottery);
                    }
                    return null;
                });
            }
            eventDeleteCascade(db.collection("events").document(profile.uid));        // organizer events
            notifDeleteCascade(db.collection("notifications").document(profile.uid)); // may want to keep this for security idk
            db.collection("user").document(profile.uid).delete();
            holder.binding.reuseProfileButton.setText("Deleted");
            holder.binding.reuseProfileButton.setBackgroundColor(Color.parseColor("#DD0202"));
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final FragmentAdminProfileBinding binding;

        public ViewHolder(FragmentAdminProfileBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    /**
     * Cascade delete for deleting a subcollection in a document.
     * Mostly derived, but batch deletion adapted from ChatGPT to
     * prevent race conditions.
     * @param root Root document.
     */
    private void notifDeleteCascade(DocumentReference root) {
        root.collection("userspecificnotifications")
                .get()
                .addOnSuccessListener(snapshot -> {
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : snapshot)
                        batch.delete(doc.getReference());
                    batch.commit();
                    root.delete();
                });
    }

    /**
     * Cascade delete for deleting a subcollection in a document.
     * Mostly derived, but batch deletion adapted from ChatGPT to
     * prevent race conditions.
     * @param root Root document.
     */
    private void eventDeleteCascade(DocumentReference root) {
        root.collection("organizer_events")
                .get()
                .addOnSuccessListener(snapshot -> {
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : snapshot)
                        batch.delete(doc.getReference());
                    batch.commit();
                    root.delete();
                });
    }
}