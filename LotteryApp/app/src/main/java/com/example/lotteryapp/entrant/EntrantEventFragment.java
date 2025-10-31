package com.example.lotteryapp.entrant;

import static android.view.View.GONE;

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
import com.example.lotteryapp.reusecomponent.Notification;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 */
public class EntrantEventFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private FirebaseFirestore db;
    private ArrayList<Event> mValues;
    private EntrantEventRecyclerViewAdapter adapter;

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
        adapter = new EntrantEventRecyclerViewAdapter(mValues);
        recyclerView.setAdapter(adapter);

        db.collection("events")
//                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null) return;
                    for (DocumentChange change : snapshot.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            Event newEvent = change.getDocument().toObject(Event.class);
                            mValues.add(newEvent);
                        }
//                        view.findViewById(R.id.fragment_entrant_notifications_loading).setVisibility(GONE);
                        adapter.notifyItemInserted(change.getNewIndex());
                    }
                });
        return view;
    }
}