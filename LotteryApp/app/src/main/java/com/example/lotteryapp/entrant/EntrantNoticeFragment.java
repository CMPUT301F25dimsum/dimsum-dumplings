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
import com.example.lotteryapp.admin.AdminNoticeRecyclerViewAdapter;
import com.example.lotteryapp.placeholder.PlaceholderContent;
import com.example.lotteryapp.reusecomponent.Notification;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * Purpose: the fragment shown in the entrants' notification tab.
 *
 * Outstanding Issues: None
 */
public class EntrantNoticeFragment extends Fragment {
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private FirebaseFirestore db;
    private ArrayList<Notification> mValues;
    private EntrantNoticeRecyclerViewAdapter adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EntrantNoticeFragment() {
    }

    public static EntrantNoticeFragment newInstance(int columnCount) {
        EntrantNoticeFragment fragment = new EntrantNoticeFragment();
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
        View view = inflater.inflate(R.layout.fragment_entrant_notice_list, container, false);

        SharedPreferences currentUser = requireContext().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);

        Context context = view.getContext();
        RecyclerView recyclerView = view.findViewById(R.id.fragment_entrant_notifications_list);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        mValues = new ArrayList<>();
        adapter = new EntrantNoticeRecyclerViewAdapter(mValues);
        recyclerView.setAdapter(adapter);

        db.collection("notifications")
                .document(currentUser.getString("UID", "Burnice"))
                .collection("userspecificnotifications")
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null) return;
                    for (DocumentChange change : snapshot.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            Notification newNotification = change.getDocument().toObject(Notification.class);
                            if ((currentUser.getBoolean("enableOrganizerNotif", true) && newNotification.senderRole == Notification.SenderRole.ORGANIZER)
                                    || (currentUser.getBoolean("enableAdminNotif", true) && newNotification.senderRole == Notification.SenderRole.ADMIN))
                                mValues.add(newNotification);
                        }
                        view.findViewById(R.id.fragment_entrant_notifications_loading).setVisibility(GONE);
                        adapter.notifyItemInserted(change.getNewIndex());
                    }
                });

        return view;
    }
}