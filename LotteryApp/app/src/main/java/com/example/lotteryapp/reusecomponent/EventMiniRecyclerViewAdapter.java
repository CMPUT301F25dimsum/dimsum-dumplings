package com.example.lotteryapp.reusecomponent;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.lotteryapp.placeholder.PlaceholderContent.PlaceholderItem;
import com.example.lotteryapp.databinding.FragmentEventMiniLayoutBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Purpose: Adapter for custom events data type.
 * <p>
 * Outstanding Issues: None
 */
public class EventMiniRecyclerViewAdapter extends RecyclerView.Adapter<EventMiniRecyclerViewAdapter.ViewHolder> {

    private final List<Event> mValues;
    private final String dateFormat = "dd/MM/yyyy";
    private final SimpleDateFormat formatter;
    private final FragmentManager manager;

    public EventMiniRecyclerViewAdapter(List<Event> items, FragmentManager fragmentManager) {
        mValues = items;
        manager = fragmentManager;
        formatter = new SimpleDateFormat(dateFormat, Locale.CANADA);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentEventMiniLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Event event = mValues.get(position);

        holder.binding.entrantEventTitle.setText(event.getTitle());
        holder.binding.entrantEventDescription.setText(event.getDescription());
        holder.binding.entrantEventOrganizer.setText(event.getOrganizer());
        holder.binding.entrantEventStatus.setText(event.isOpen());
        if (event.isOpen().equals("Open"))
            holder.binding.entrantEventStatus.setTextColor(Color.GREEN);
        else
            holder.binding.entrantEventStatus.setTextColor(Color.RED);
        holder.binding.entrantEventDeadline.setText(formatter.format(event.getLottery().registrationEnd));
        String countCapacity = event.getNentrants() + "/";
        if (event.getRegistrationLimit() == -1)
            countCapacity += "-";
        else
            countCapacity += event.getRegistrationLimit();

        holder.binding.eventMiniEntrantSignupCount.setText(countCapacity);
        // Set event banner
        if (event.getBannerURL() != null)
            FirebaseStorage.getInstance().getReference()
                    .child(event.getBannerURL()).getBytes(1024*1024)
                            .addOnSuccessListener(bytes -> {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                holder.binding.entrantEventImage.setImageBitmap(bitmap);
                            });
        //Switch button callback based on notification category
        holder.binding.entrantEventView.setOnClickListener( v -> {
            new EventDisplayFragment(event, manager).show(manager, "event_display");
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final FragmentEventMiniLayoutBinding binding;
//        public Notification notification;

        public ViewHolder(FragmentEventMiniLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}