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

import com.example.lotteryapp.MainActivity;
import com.example.lotteryapp.R;
import com.example.lotteryapp.reusecomponent.Event;
import com.example.lotteryapp.reusecomponent.EventMiniRecyclerViewAdapter;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Purpose: Displays events to the entrant in the form of a list.
 * <p>
 * Outstanding Issues: Images not complete
 */
public class EntrantEventFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private FirebaseFirestore db;
    private ArrayList<Event> mValues;

    //New: For filter
    private ArrayList<Event> filteredEvents;
    private String tagFilter = "All Tags";
    private String organizerFilter = "All Organizers";
    private EntrantEventFilterBar filterBar;


    private EventMiniRecyclerViewAdapter adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EntrantEventFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static EntrantEventFragment newInstance(int columnCount) {
        EntrantEventFragment fragment = new EntrantEventFragment();
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
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entrant_event_list, container, false);
        SharedPreferences currentUser = requireContext().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);

        // Set the adapter
        Context context = view.getContext();
        RecyclerView recyclerView = view.findViewById(R.id.list);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        mValues = new ArrayList<>();
        filteredEvents = new ArrayList<>();
        adapter = new EventMiniRecyclerViewAdapter(filteredEvents, getParentFragmentManager()); // Changed for implementing filter
        recyclerView.setAdapter(adapter);

        filterBar = view.findViewById(R.id.entrant_event_filter_bar);
        if (filterBar != null) {
            filterBar.initFilter((selectedTag, selectedOrganizer) -> {
                tagFilter = selectedTag;
                organizerFilter = selectedOrganizer;
                applyEventFilter();
            });
        }

        db.collectionGroup("organizer_events")
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null) return;
                    for (DocumentChange change : snapshot.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            Event newEvent = change.getDocument().toObject(Event.class);
                            int newIndex = change.getNewIndex();

                            // Defensive check: Firestore indices can be -1 on first snapshot
                            if (newIndex >= 0 && newIndex <= mValues.size()) {
                                mValues.add(newIndex, newEvent);
                                adapter.notifyItemInserted(newIndex);
                            } else {
                                mValues.add(newEvent);
                                adapter.notifyDataSetChanged();
                            }
                        }
                        else if(change.getType() == DocumentChange.Type.REMOVED){
                            int oldIndex = change.getOldIndex();
                            if (oldIndex >= 0 && oldIndex < mValues.size()) {
                                mValues.remove(oldIndex);
                                adapter.notifyItemRemoved(oldIndex);
                            } else {
                                // fallback: full refresh to resync
                                mValues.clear();
                                for (var doc : snapshot.getDocuments()) {
                                    mValues.add(doc.toObject(Event.class));
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }
                        else if (change.getType() == DocumentChange.Type.MODIFIED){
                            Event updatedEvent = change.getDocument().toObject(Event.class);
                            int oldIndex = change.getOldIndex();
                            int newIndex = change.getNewIndex();

                            if (oldIndex == newIndex) {
                                // Same position: simple update
                                mValues.set(oldIndex, updatedEvent);
                                adapter.notifyItemChanged(oldIndex);
                            } else {
                                // Item moved (rare, but Firestore allows it)
                                mValues.remove(oldIndex);
                                mValues.add(newIndex, updatedEvent);
                                adapter.notifyItemMoved(oldIndex, newIndex);
                                adapter.notifyItemChanged(newIndex);
                            }
                        }
                    }
                    rebuildFilterOptions();
                    applyEventFilter();
                });
        // Launch event if QR code was loaded
        if (MainActivity.deepEventId != null && MainActivity.deepOrganizerId != null){
            MainActivity.load_event(MainActivity.deepEventId, MainActivity.deepOrganizerId, getChildFragmentManager());
        }
        return view;
    }

    //Extract tags/organizers from mvalue, update filter bar
    private void rebuildFilterOptions() {
        if (filterBar == null) return;

        Set<String> tagSet = new HashSet<>();
        Set<String> orgSet = new HashSet<>();

        for (Event e : mValues) {
            if (e.getFilters() != null) {
                tagSet.addAll(e.getFilters());
            }
            if (e.getOrganizer() != null && !e.getOrganizer().isEmpty()) {
                orgSet.add(e.getOrganizer());
            }
        }

        filterBar.setTagOptions(new ArrayList<>(tagSet));
        filterBar.setOrganizerOptions(new ArrayList<>(orgSet));
    }

    private void applyEventFilter() {
        filteredEvents.clear();

        for (Event e : mValues) {
            if (!matchesTagFilter(e)) continue;
            if (!matchesOrganizerFilter(e)) continue;
            filteredEvents.add(e);
        }

        adapter.notifyDataSetChanged();
    }
    private boolean matchesTagFilter(Event e) {
        if ("All Tags".equals(tagFilter)) {
            return true;
        }
        if (e.getFilters() == null || e.getFilters().isEmpty()) {
            return false;
        }
        return e.getFilters().contains(tagFilter);
    }

    private boolean matchesOrganizerFilter(Event e) {
        if ("All Organizers".equals(organizerFilter)) {
            return true;
        }
        String org = e.getOrganizer();
        return org != null && org.equalsIgnoreCase(organizerFilter);
    }

}