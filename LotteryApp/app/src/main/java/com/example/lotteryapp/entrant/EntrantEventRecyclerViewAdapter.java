package com.example.lotteryapp.entrant;

import static android.view.View.GONE;

import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lotteryapp.databinding.FragmentEntrantNoticeBinding;
import com.example.lotteryapp.placeholder.PlaceholderContent.PlaceholderItem;
import com.example.lotteryapp.databinding.FragmentEntrantEventBinding;
import com.example.lotteryapp.reusecomponent.Event;
import com.example.lotteryapp.reusecomponent.Notification;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceholderItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class EntrantEventRecyclerViewAdapter extends RecyclerView.Adapter<EntrantEventRecyclerViewAdapter.ViewHolder> {

    private final List<Event> mValues;
    private final String dateFormat = "dd/MM/yyyy";
    private final SimpleDateFormat formatter;
    public EntrantEventRecyclerViewAdapter(ArrayList<Event> items) {
        mValues = items;
        formatter = new SimpleDateFormat(dateFormat, Locale.CANADA);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentEntrantEventBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

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
        String countCapacity = event.getNentrants() + "/" + event.getRegistrationLimit();
        holder.binding.eventMiniEntrantSignupCount.setText(countCapacity);
        // Set event banner
        // holder.binding.entrantEventImage
        //Switch button callback based on notification category
        holder.binding.entrantEventRegister.setOnClickListener( v -> {
                // call the API function to open the event
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final FragmentEntrantEventBinding binding;
//        public Notification notification;

        public ViewHolder(FragmentEntrantEventBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}