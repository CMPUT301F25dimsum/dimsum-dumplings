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
import com.example.lotteryapp.entrant.EntrantNoticeRecyclerViewAdapter;
import com.example.lotteryapp.placeholder.PlaceholderContent;
import com.example.lotteryapp.reusecomponent.Notification;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 */
public class OrganizerNoticeFragment extends Fragment {
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private FirebaseFirestore db;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OrganizerNoticeFragment() {
    }

    public static OrganizerNoticeFragment newInstance(int columnCount) {
        OrganizerNoticeFragment fragment = new OrganizerNoticeFragment();
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
        View view = inflater.inflate(R.layout.fragment_organizer_notice_list, container, false);

        SharedPreferences currentUser = requireContext().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            db.collection("notifications")
                    .document(currentUser.getString("UID", "John"))
                    .collection("userspecificnotifications")
                    .get()
                    .addOnSuccessListener(query -> {
                        ArrayList<Notification> mValues = new ArrayList<>();
                        for (DocumentSnapshot doc : query) mValues.add(doc.toObject(Notification.class));
                        recyclerView.setAdapter(new OrganizerNoticeRecyclerViewAdapter(mValues));
                    });
        }
        return view;
    }
}