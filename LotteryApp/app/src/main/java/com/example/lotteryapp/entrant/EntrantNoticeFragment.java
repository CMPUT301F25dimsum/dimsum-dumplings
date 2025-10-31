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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class EntrantNoticeFragment extends Fragment {
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private FirebaseFirestore db;
    //2 new lists
    private List<Notification> allNotices = new ArrayList<>();
    private List<Notification> filteredNotices = new ArrayList<>();

    //recycler view adapter
    private RecyclerView recyclerView;
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

        adapter = new EntrantNoticeRecyclerViewAdapter(filteredNotices);
        recyclerView.setAdapter(adapter);

        db.collection("notifications")
                .document(currentUser.getString("UID", "Burnice"))
                .collection("userspecificnotifications")
                .get()
                .addOnSuccessListener(query -> {

                    //New: add data to allNotices and filteredNotices(Eric)
                    allNotices.clear();
                    for (DocumentSnapshot doc : query) {
                        Notification n = doc.toObject(Notification.class);
                        if (n != null) allNotices.add(n);
                    }

                    filteredNotices.clear();
                    filteredNotices.addAll(allNotices);

                    adapter.notifyDataSetChanged();
                    view.findViewById(R.id.fragment_entrant_notifications_loading).setVisibility(GONE);
                });

        //link to the new filter bar(Eric)
        EntrantNoticeFilterBar filterBar = view.findViewById(R.id.entrant_filterbar);
        if (filterBar != null) {
            filterBar.initFilter(this::applyFilter);
        }

        return view;
    }

    private void applyFilter(String filterType) {
        filteredNotices.clear();

        for (Notification n : allNotices) {
            if (filterType.equals("All") ||
                    n.type.name().equalsIgnoreCase(filterType)) {
                filteredNotices.add(n);
            }
        }
        adapter.notifyDataSetChanged();
    }
}