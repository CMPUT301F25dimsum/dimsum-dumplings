package com.example.lotteryapp.entrant;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lotteryapp.R;
import com.example.lotteryapp.placeholder.PlaceholderContent;
import com.example.lotteryapp.reusecomponent.Event;
import com.example.lotteryapp.reusecomponent.EventMiniRecyclerViewAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Purpose: Manage a list of events that entrant has subscribed to.
 * <p>
 * Outstanding Issues: Acceptance incomplete until lottery completes
 */
public class EntrantSavedEventFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    private FirebaseFirestore db;

    private ArrayList<Event> mValues;

    private EventMiniRecyclerViewAdapter adapter;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EntrantSavedEventFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static EntrantSavedEventFragment newInstance(int columnCount) {
        EntrantSavedEventFragment fragment = new EntrantSavedEventFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entrant_saved_event_list, container, false);
        SharedPreferences currentUser = requireContext().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        String userID = currentUser.getString("UID", "John");

        db = FirebaseFirestore.getInstance();

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = view.findViewById(R.id.list);
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mValues = new ArrayList<>();
            adapter = new EventMiniRecyclerViewAdapter(mValues, getParentFragmentManager());
            recyclerView.setAdapter(adapter);
        }

        db.collection("user").document(userID).addSnapshotListener((snapshot, e) -> {
            if (snapshot != null && snapshot.exists()) {
                mValues.clear();
                ArrayList<String> registeredLotteries = (ArrayList<String>) snapshot.get("registeredLotteries");

                if (registeredLotteries != null && !registeredLotteries.isEmpty()) {
                    for (String lott : registeredLotteries) {
                        String[] lottSplit = lott.split(",");
                        String organizerId = lottSplit[0];
                        String eventId = lottSplit[1];

                        db.collection("events")
                                .document(organizerId)
                                .collection("organizer_events")
                                .document(eventId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        Event event = documentSnapshot.toObject(Event.class);
                                        mValues.add(event);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }
                }
            }
        });
        return view;
    }
}