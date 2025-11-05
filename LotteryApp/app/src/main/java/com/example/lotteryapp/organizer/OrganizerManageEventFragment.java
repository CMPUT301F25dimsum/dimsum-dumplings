package com.example.lotteryapp.organizer;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotteryapp.R;
import com.example.lotteryapp.reusecomponent.Event;

import java.util.ArrayList;

public class OrganizerManageEventFragment extends DialogFragment {
    private ArrayList<String> mValues;
    private OrganizerLotteryRecyclerViewAdapter adapter;

    public OrganizerManageEventFragment(Event event) {
        mValues = event.getLottery().getEntrants();
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organizer_manage_lottery, container, false);
        adapter = new OrganizerLotteryRecyclerViewAdapter(mValues);
        RecyclerView recyclerView = view.findViewById(R.id.fragment_organizer_manage_lottery_list);
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
