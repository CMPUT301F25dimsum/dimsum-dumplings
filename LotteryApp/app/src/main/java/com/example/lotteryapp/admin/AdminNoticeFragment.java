package com.example.lotteryapp.admin;

import static android.view.View.GONE;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lotteryapp.R;
import com.example.lotteryapp.placeholder.PlaceholderContent;
import com.example.lotteryapp.reusecomponent.Notification;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Purpose: The fragment for displaying admin notifications with filtering by organizer and time.
 * Outstanding Issues: None
 * Author: Will, Eric
 */
public class AdminNoticeFragment extends Fragment {
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private FirebaseFirestore db;
    private AdminNoticeRecyclerViewAdapter adapter;
    private ListenerRegistration snapshotRegister;

    private ArrayList<Notification> allNotices = new ArrayList<>();
    private ArrayList<Notification> filteredNotices = new ArrayList<>();
    private String organizerFilter = "All Organizers";
    private String timeFilter = "All Time";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AdminNoticeFragment() {
    }

    @SuppressWarnings("unused")
    public static AdminNoticeFragment newInstance(int columnCount) {
        AdminNoticeFragment fragment = new AdminNoticeFragment();
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
        View view = inflater.inflate(R.layout.fragment_admin_notice_list, container, false);

        Context context = view.getContext();
        RecyclerView recyclerView = view.findViewById(R.id.fragment_admin_notifications_list);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        adapter = new AdminNoticeRecyclerViewAdapter(filteredNotices, getParentFragmentManager());
        recyclerView.setAdapter(adapter);

        snapshotRegister = db.collectionGroup("userspecificnotifications")
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null) return;
                    for (DocumentChange change : snapshot.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            Notification newNotif = change.getDocument().toObject(Notification.class);
                            allNotices.add(newNotif);
                            if (matchesFilter(newNotif)) {
                                filteredNotices.add(newNotif);
                                adapter.notifyItemInserted(filteredNotices.size() - 1);
                            }
                        }
                    }
                    view.findViewById(R.id.fragment_admin_notifications_loading).setVisibility(GONE);
                });

        AdminNoticeFilterBar filterBar = view.findViewById(R.id.fragment_admin_notice_filter_bar);
        if (filterBar != null) {
            filterBar.initFilter((organizer, timeRange) -> {
                organizerFilter = organizer;
                timeFilter = timeRange;
                applyFilter();
            });
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        snapshotRegister.remove();
        super.onDestroyView();
    }
    //filtering depends on conditions (Eric)
    private void applyFilter() {
        filteredNotices.clear();

        for (Notification n : allNotices) {
            if (matchesFilter(n)) {
                filteredNotices.add(n);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private boolean matchesFilter(Notification n) {
        boolean organizerMatch = organizerFilter.equals("All Organizers") || (n.sender != null && n.sender.equalsIgnoreCase(organizerFilter));
        boolean timeMatch = true;

        //Time filtering logic
        if (n.time != null) {
            Date notifDate = n.time.toDate();

            Calendar now = Calendar.getInstance();
            Calendar notifCal = Calendar.getInstance();
            notifCal.setTime(notifDate);

            //This day
            Calendar startOfDay = (Calendar) now.clone();
            startOfDay.set(Calendar.HOUR_OF_DAY, 0);
            startOfDay.set(Calendar.MINUTE, 0);
            startOfDay.set(Calendar.SECOND, 0);
            startOfDay.set(Calendar.MILLISECOND, 0);
            Calendar endOfDay = (Calendar) startOfDay.clone();
            endOfDay.add(Calendar.DAY_OF_MONTH, 1);

            //This week
            Calendar startOfWeek = (Calendar) now.clone();
            startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.getFirstDayOfWeek());
            startOfWeek.set(Calendar.HOUR_OF_DAY, 0);
            startOfWeek.set(Calendar.MINUTE, 0);
            startOfWeek.set(Calendar.SECOND, 0);
            startOfWeek.set(Calendar.MILLISECOND, 0);
            Calendar endOfWeek = (Calendar) startOfWeek.clone();
            endOfWeek.add(Calendar.WEEK_OF_YEAR, 1);

            switch (timeFilter) {
                case "Today":
                    timeMatch = notifDate.after(startOfDay.getTime()) && notifDate.before(endOfDay.getTime());
                    break;
                case "This Week":
                    timeMatch = notifDate.after(startOfWeek.getTime()) && notifDate.before(endOfWeek.getTime());
                    break;
                default:
                    timeMatch = true; // "All Time"
            }
        }

        return organizerMatch && timeMatch;
    }
}
