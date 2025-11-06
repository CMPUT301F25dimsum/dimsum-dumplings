package com.example.lotteryapp.admin;

import static android.view.View.GONE;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.lotteryapp.databinding.FragmentAdminNoticeBinding;
import com.example.lotteryapp.reusecomponent.Event;
import com.example.lotteryapp.reusecomponent.EventDisplayFragment;
import com.example.lotteryapp.reusecomponent.Notification;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Purpose: Adapter for custom Notification data type. Allows Notification (model) to be
 * displayed as a List (view).
 * <p>
 * Outstanding Issues: None
 */
public class AdminNoticeRecyclerViewAdapter extends RecyclerView.Adapter<AdminNoticeRecyclerViewAdapter.ViewHolder> {

    private List<Notification> mValues;
    private FragmentManager fragmentManager;
    private FirebaseFirestore db;

    public AdminNoticeRecyclerViewAdapter(List<Notification> items, FragmentManager fManager) {
        this.fragmentManager = fManager;
        mValues = items;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentAdminNoticeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Notification notification = mValues.get(position);

        holder.binding.reuseNotificationViewSummary.setText(notification.summary);
        holder.binding.reuseNotificationViewTitle.setText(notification.title);
        String correspondence = "f: " + notification.correspondenceMask + "\n" +
                "(" + notification.sender + ")" + "\nt: " + notification.receiver;
        holder.binding.reuseNotificationCorrespondence.setText(correspondence);
        holder.binding.reuseNotificationTime.setText(
                new SimpleDateFormat("yyyy-MM-dd\nHH:mm", Locale.CANADA).format(notification.time.toDate()));
        //Switch button callback based on notification category
        if (notification.type == Notification.Type.CUSTOM)
            holder.binding.reuseNotificationButton.setVisibility(GONE);
        else
            holder.binding.reuseNotificationButton.setOnClickListener(v -> {
                db.collection("events")
                        .document(notification.sender)
                        .collection("organizer_events")
                        .document(notification.event)
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            if (snapshot.exists()) {
                                Event event;
                                event = snapshot.toObject(Event.class);
                                assert event != null;
                                new EventDisplayFragment(event).show(fragmentManager, "event_display");
                            }
                        });
            });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final FragmentAdminNoticeBinding binding;
//        public Notification notification;

        public ViewHolder(FragmentAdminNoticeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}