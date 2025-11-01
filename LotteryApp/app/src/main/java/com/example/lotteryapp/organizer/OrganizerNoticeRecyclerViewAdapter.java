package com.example.lotteryapp.organizer;

import static android.view.View.GONE;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lotteryapp.databinding.FragmentEntrantNoticeBinding;
import com.example.lotteryapp.placeholder.PlaceholderContent.PlaceholderItem;
import com.example.lotteryapp.databinding.FragmentOrganizerNoticeBinding;
import com.example.lotteryapp.reusecomponent.Notification;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Purpose: Adapter for custom Notification data type. Allows Notification (model) to be
 * displayed as a List (view).
 *
 * Outstanding Issues: None
 */
public class OrganizerNoticeRecyclerViewAdapter extends RecyclerView.Adapter<OrganizerNoticeRecyclerViewAdapter.ViewHolder> {

    private final List<Notification> mValues;

    public OrganizerNoticeRecyclerViewAdapter(List<Notification> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentOrganizerNoticeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Notification notification = mValues.get(position);

        holder.binding.reuseNotificationViewSummary.setText(notification.summary);
        holder.binding.reuseNotificationViewTitle.setText(notification.title);
        holder.binding.reuseNotificationCorrespondence.setText(notification.correspondenceMask);
        holder.binding.reuseNotificationTime.setText(
                new SimpleDateFormat("yyyy-MM-dd\nHH:mm", Locale.CANADA).format(notification.time.toDate()));
        //Switch button callback based on notification category
        if (notification.type == Notification.Type.CUSTOM)
            holder.binding.reuseNotificationButton.setVisibility(GONE);
        else
            holder.binding.reuseNotificationButton.setOnClickListener( v -> {
                // call the API function to open the event
            });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final FragmentOrganizerNoticeBinding binding;
//        public Notification notification;

        public ViewHolder(FragmentOrganizerNoticeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}