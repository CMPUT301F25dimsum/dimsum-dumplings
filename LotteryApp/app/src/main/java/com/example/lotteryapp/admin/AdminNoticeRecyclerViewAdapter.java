package com.example.lotteryapp.admin;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.lotteryapp.placeholder.PlaceholderContent.PlaceholderItem;
import com.example.lotteryapp.databinding.FragmentAdminNoticeBinding;
import com.example.lotteryapp.reusecomponent.Notification;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceholderItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class AdminNoticeRecyclerViewAdapter extends RecyclerView.Adapter<AdminNoticeRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Notification> mValues;

    public AdminNoticeRecyclerViewAdapter(Task<QuerySnapshot> tsk) {
        mValues = new ArrayList<>();
        tsk.addOnSuccessListener(snapshot -> {
            for (DocumentSnapshot doc : snapshot) mValues.add(doc.toObject(Notification.class));
        });
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
        holder.binding.reuseNotificationCorrespondence.setText(notification.correspondence);
        holder.binding.reuseNotificationTime.setText(notification.time.toString());

        Log.d("MainActivity", String.valueOf(getItemCount()));
        //Switch button callback based on notification category

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
//        public final Button callback_btn;
        public final FragmentAdminNoticeBinding binding;
//        public Notification notification;

        public ViewHolder(FragmentAdminNoticeBinding binding) {
            super(binding.getRoot());
//            callback_btn = binding.reuseNotificationButton;
            this.binding = binding;
        }
    }
}