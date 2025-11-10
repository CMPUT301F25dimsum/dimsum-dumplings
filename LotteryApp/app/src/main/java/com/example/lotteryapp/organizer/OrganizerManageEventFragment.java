package com.example.lotteryapp.organizer;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotteryapp.R;
import com.example.lotteryapp.reusecomponent.Event;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class OrganizerManageEventFragment extends DialogFragment {
    private ArrayList<String> mValues;
    private static final String ARG_COLUMN_COUNT = "column-count";

    private OrganizerLotteryRecyclerViewAdapter adapter;
    private Integer mColumnCount = 1;

    public OrganizerManageEventFragment(Event event) {
        event.getLottery().addEntrant("Fuj5jv4dqJmPSfWYyKZj");
        event.getLottery().addEntrant("2e36ad92ba24527b");
        event.getLottery().addEntrant("42a45e0c25868261");
        event.getLottery().addEntrant("57ed08dc161ab6a3");


        mValues = event.getLottery().getEntrants();

    }

    /***
    public static OrganizerManageEventFragment newInstance(int columnCount) {
        OrganizerManageEventFragment fragment = new OrganizerManageEventFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }
     ***/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organizer_manage_lottery, container, false);
        adapter = new OrganizerLotteryRecyclerViewAdapter(mValues);
        RecyclerView recyclerView = view.findViewById(R.id.fragment_organizer_manage_lottery_list);
        Context context = view.getContext();

        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        recyclerView.setAdapter(adapter);
        return view;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView closeButton = view.findViewById(R.id.fragment_organizer_manage_lottery_close);
        closeButton.setOnClickListener(v -> {
            dismiss();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }


}
