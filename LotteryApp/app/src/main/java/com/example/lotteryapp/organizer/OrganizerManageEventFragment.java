package com.example.lotteryapp.organizer;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotteryapp.R;
import com.example.lotteryapp.reusecomponent.Event;
import com.example.lotteryapp.reusecomponent.Lottery;
import com.example.lotteryapp.reusecomponent.LotteryEntrant;
import com.example.lotteryapp.reusecomponent.Notification;
import com.example.lotteryapp.reusecomponent.NotificationDialogue;
import com.example.lotteryapp.reusecomponent.UserProfile;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class OrganizerManageEventFragment extends DialogFragment {
    private ArrayList<LotteryEntrant> mValues;
    private Event event;
    private static final String ARG_COLUMN_COUNT = "column-count";

    private OrganizerLotteryRecyclerViewAdapter adapter;
    private Integer mColumnCount = 1;
    private FirebaseFirestore db;
    private SharedPreferences currentUser;
    //private int checkCounter;
    private ListenerRegistration snapshotRegister;
    private DocumentReference eventDocument;

    public OrganizerManageEventFragment(Event event) {
//        event.getLottery().addEntrant("Fuj5jv4dqJmPSfWYyKZj");
//        event.getLottery().addEntrant("2e36ad92ba24527b");
//        event.getLottery().addEntrant("42a45e0c25868261");
//        event.getLottery().addEntrant("57ed08dc161ab6a3");

        this.event = event;
        // checkCounter = 0;
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
        db = FirebaseFirestore.getInstance();
        currentUser = requireContext().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        mValues = new ArrayList<>();
        eventDocument = db.collection("events").document(event.getOrganizer()).collection("organizer_events").document(event.id);
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organizer_manage_lottery, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.fragment_organizer_manage_lottery_list);
        Context context = view.getContext();
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        //mapEntrantsAndStatus(event.getLottery().getEntrants(), event.getLottery().entrantStatus);

        adapter = new OrganizerLotteryRecyclerViewAdapter(mValues);
        recyclerView.setAdapter(adapter);

        snapshotRegister = db.collection("events")
                .document(event.getOrganizer())
                .collection("organizer_events")
                .document(event.id)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) return;
                    Event ev = snapshot.toObject(Event.class);
                    assert ev != null;
                    mapEntrantsAndStatus(ev.getLottery().getEntrants(), ev.getLottery().entrantStatus);
                    adapter.notifyDataSetChanged();
                });

        return view;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView closeButton = view.findViewById(R.id.fragment_organizer_manage_lottery_close);
        closeButton.setOnClickListener(v -> {
            dismiss();
        });

        CheckBox select_all = view.findViewById(R.id.fragment_organizer_manage_lottery_check_all);
        select_all.setOnCheckedChangeListener((compoundbutton, isChecked) -> {
            if (isChecked)
                mValues.forEach(entrant -> {
                    entrant.bSelected = true;
                });
            else
                mValues.forEach(entrant -> {
                    entrant.bSelected = false;
                });
            adapter.notifyItemRangeChanged(0, mValues.size());
        });

        view.findViewById(R.id.fragment_organizer_manage_lottery_draw).setOnClickListener(v -> {
//            for (LotteryEntrant le : mValues) {
//                db.collection("user")
//                        .document(le.uid)
//                        .get()
//                        .addOnSuccessListener(snapshot -> {
//                            if (!snapshot.exists()) le.bValidUID = false;
//                            checkCounter += 1;
//                            draw();
//                        });
//            }

            ArrayList<Pair<LotteryEntrant, Integer>> RegOrWaitlistedEntrants = new ArrayList<>();
            for (int i = 0; i < mValues.size(); ++i) {
                mValues.get(i).bSelected = false;
                if (mValues.get(i).status == LotteryEntrant.Status.Registered || mValues.get(i).status == LotteryEntrant.Status.Waitlisted)
                    RegOrWaitlistedEntrants.add(new Pair<>(mValues.get(i), i));
            }
            if (RegOrWaitlistedEntrants.isEmpty()) return;

            List<Integer> uniqueRandom = new ArrayList<>();
            for (int i = 0; i < RegOrWaitlistedEntrants.size(); ++i)
                uniqueRandom.add(i);
            Collections.shuffle(uniqueRandom);

            Notification nInvitation = Notification.constructSuccessNotification(event.getTitle(), currentUser.getString("UID", "John"), Notification.SenderRole.ORGANIZER, event.id);
            nInvitation.maskCorrespondence(currentUser.getString("name", "John"));
            Notification nWaitlist = Notification.constructFailureNotification(event.getTitle(), currentUser.getString("UID", "John"), Notification.SenderRole.ORGANIZER, event.id);
            nWaitlist.maskCorrespondence(currentUser.getString("name", "John"));

            if (uniqueRandom.size() > event.getMaxCapacity() - (mValues.size() - RegOrWaitlistedEntrants.size()))
                uniqueRandom = uniqueRandom.subList(0, event.getMaxCapacity() - (mValues.size() - RegOrWaitlistedEntrants.size()));

            uniqueRandom.forEach(unique -> {
                int unpackedIDX = RegOrWaitlistedEntrants.get(unique).second;
                nInvitation.sendNotification(mValues.get(unpackedIDX).uid);
                mValues.get(unpackedIDX).status = LotteryEntrant.Status.Invited;
                event.getLottery().entrantStatus.set(unpackedIDX, LotteryEntrant.Status.Invited);
                adapter.notifyItemChanged(unpackedIDX);
            });
            RegOrWaitlistedEntrants.forEach(entrantIntegerPair -> {
                if (entrantIntegerPair.first.status != LotteryEntrant.Status.Invited) {
                    int unpackedIDX = entrantIntegerPair.second;
                    nWaitlist.sendNotification(mValues.get(unpackedIDX).uid);
                    mValues.get(unpackedIDX).status = LotteryEntrant.Status.Waitlisted;
                    event.getLottery().entrantStatus.set(entrantIntegerPair.second, LotteryEntrant.Status.Waitlisted);
                    adapter.notifyItemChanged(unpackedIDX);
                }
            });
            eventDocument.set(event);
        });

        view.findViewById(R.id.fragment_organizer_manage_lottery_notify).setOnClickListener(v -> {
            NotificationDialogue nd = new NotificationDialogue();
            nd.setListener(value -> {
                Notification n = Notification.constructCustomNotification(value, event.getTitle(), currentUser.getString("UID", "John"), Notification.SenderRole.ORGANIZER);
                mValues.forEach(lotteryEntrant -> {
                    if (lotteryEntrant.bSelected) n.sendNotification(lotteryEntrant.uid);
                });
            });
            nd.show(getParentFragmentManager(), "NotifDialogue");
        });

        view.findViewById(R.id.fragment_organizer_manage_lottery_cancel).setOnClickListener(v -> {
            for (int i = 0; i < mValues.size(); ++i)
                if (mValues.get(i).bSelected) {
                    mValues.get(i).status = LotteryEntrant.Status.Cancelled;
                    event.getLottery().entrantStatus.set(i, LotteryEntrant.Status.Cancelled);
                }
            eventDocument.set(event);
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

//    private void draw() {
//        if (checkCounter < mValues.size()) return;
//        checkCounter = 0;
//        mValues.removeIf(le -> !le.bValidUID);
//
//        ArrayList<Pair<LotteryEntrant, Integer>> RegOrWaitlistedEntrants = new ArrayList<>();
//        for (int i = 0; i < mValues.size(); ++i) {
//
//            if (mValues.get(i).status == LotteryEntrant.Status.Registered || mValues.get(i).status == LotteryEntrant.Status.Waitlisted)
//                RegOrWaitlistedEntrants.add(new Pair<>(mValues.get(i), i));
//        }
//        if (RegOrWaitlistedEntrants.isEmpty()) return;
//
//        Random r = new Random();
//        for (int i = 0; i < event.getMaxCapacity() - (mValues.size() - RegOrWaitlistedEntrants.size()); ++i) {
//            int num = r.nextInt(RegOrWaitlistedEntrants.size());
//            RegOrWaitlistedEntrants.get(num).first.bSelected = true;
//            adapter.notifyItemChanged(RegOrWaitlistedEntrants.get(num).second);
//        }
//    }

    @Override
    public void onDestroyView() {
        snapshotRegister.remove();
        super.onDestroyView();
    }

    private ArrayList<LotteryEntrant> mapEntrantsAndStatus(ArrayList<String> entrants, ArrayList<LotteryEntrant.Status> status) throws IllegalArgumentException {
        if (entrants.size() != status.size()) throw new IllegalArgumentException("Size mismatch. Size of entrants: " + entrants.size() + " Size of status: " + status.size());

        mValues.clear();
        for (int i = 0; i < entrants.size(); ++i)
            mValues.add(new LotteryEntrant(entrants.get(i), false, status.get(i)));
        return mValues;
    }
}
