package com.example.lotteryapp.reusecomponent;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.lotteryapp.placeholder.PlaceholderContent.PlaceholderItem;
import com.example.lotteryapp.databinding.FragmentEventMiniLayoutBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceholderItem}.
 * TODO: Replace the implementation with code for your data type.
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
        holder.binding.entrantEventDeadline.setText(formatter.format(event.getLotteryEndDate()));
        String countCapacity = event.getNentrants() + "/";
        if (event.getRegistrationLimit() == -1)
            countCapacity += "-";
        else
            countCapacity += event.getRegistrationLimit();

        holder.binding.eventMiniEntrantSignupCount.setText(countCapacity);
        // Set event banner
        // holder.binding.entrantEventImage
        //Switch button callback based on notification category
        holder.binding.entrantEventView.setOnClickListener( v -> {
            new EventDisplayFragment(event).show(manager, "event_display");
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