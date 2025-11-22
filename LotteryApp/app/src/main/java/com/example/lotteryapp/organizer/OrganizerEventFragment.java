package com.example.lotteryapp.organizer;

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
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Purpose: Show the organizer a list of their own events and allow them to modify them accordingly.
 * <p>
 * Outstanding Issues: Images not done
 */
public class OrganizerEventFragment extends Fragment {
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private FirebaseFirestore db;
    private ArrayList<Event> mValues;
    private EventMiniRecyclerViewAdapter adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OrganizerEventFragment() {
    }
    
    public static OrganizerEventFragment newInstance(int columnCount) {
        OrganizerEventFragment fragment = new OrganizerEventFragment();
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
        View view = inflater.inflate(R.layout.fragment_organizer_event_list, container, false);
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
        adapter = new EventMiniRecyclerViewAdapter(mValues, getParentFragmentManager());
        recyclerView.setAdapter(adapter);

        db.collection("events")
                .document(currentUser.getString("UID", "John"))
                .collection("organizer_events")
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null) return;
                    for (DocumentChange change : snapshot.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            Event newEvent = change.getDocument().toObject(Event.class);
                            mValues.add(newEvent);
                            adapter.notifyItemInserted(change.getNewIndex());
                        }
                        else if (change.getType() == DocumentChange.Type.MODIFIED) {
                            Event modifiedEvent = change.getDocument().toObject(Event.class);
                            mValues.set(change.getOldIndex(), modifiedEvent);
                            adapter.notifyItemChanged(change.getOldIndex());
                        }
                        else if (change.getType() == DocumentChange.Type.REMOVED){
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
                    }
                });
        return view;
    }
}