package com.example.lotteryapp.admin;

import static android.view.View.GONE;

import android.app.Notification;
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
import android.widget.Toast;

import com.example.lotteryapp.R;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.example.lotteryapp.reusecomponent.UserProfile;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

/**
 * Purpose: Displays all profiles to the admin for deletion.
 * Deletes profiles from database, along with associated events and notifications.
 * <p>
 * Outstanding Issues: None
 */
public class AdminProfileFragment extends Fragment {
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private FirebaseFirestore db;
    private AdminProfileRecyclerViewAdapter adapter;
    private ListenerRegistration snapshotRegister;
    private ArrayList<UserProfile> allProfiles = new ArrayList<>();
    private ArrayList<UserProfile> filteredProfiles = new ArrayList<>();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AdminProfileFragment() {
    }

    public static AdminProfileFragment newInstance(int columnCount) {
        AdminProfileFragment fragment = new AdminProfileFragment();
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
        View view = inflater.inflate(R.layout.fragment_admin_profile_list, container, false);

        Context context = view.getContext();
        RecyclerView recyclerView = view.findViewById(R.id.fragment_admin_profiles_list);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        adapter = new AdminProfileRecyclerViewAdapter(filteredProfiles, getParentFragmentManager());
        recyclerView.setAdapter(adapter);

        snapshotRegister = db.collection("user")
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null) return;
                    for (DocumentChange change : snapshot.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            UserProfile newProfile = change.getDocument().toObject(UserProfile.class);
                            allProfiles.add(newProfile);
                            //if (matchesFilter(newProfile)) {
                                filteredProfiles.add(newProfile);
                                adapter.notifyItemInserted(filteredProfiles.size() - 1);
                            //}
                        }
                        else if (change.getType() == DocumentChange.Type.REMOVED) {
                            UserProfile deletedProfile = change.getDocument().toObject(UserProfile.class);
                            int index = allProfiles.indexOf(deletedProfile);
                            if (index != -1) allProfiles.remove(index); //Guaranteed in allProfiles
                            index = filteredProfiles.indexOf(deletedProfile);
                            if (index != -1) {
                                filteredProfiles.remove(index);
                                adapter.notifyItemRemoved(index);
                            }
                        }
                        else if (change.getType() == DocumentChange.Type.MODIFIED) {
                            UserProfile modifiedProfile = change.getDocument().toObject(UserProfile.class);
                            int index = allProfiles.indexOf(modifiedProfile);
                            if (index != -1) allProfiles.set(index, modifiedProfile);
                            index = filteredProfiles.indexOf(modifiedProfile);
                            if (index != -1) {
                                filteredProfiles.set(index, modifiedProfile);
                                adapter.notifyItemChanged(index);
                            }
                        }
                    }
                    view.findViewById(R.id.fragment_admin_profiles_loading).setVisibility(GONE);
                });

        return view;
    }

    @Override
    public void onDestroyView() {
        snapshotRegister.remove();
        super.onDestroyView();
    }
}