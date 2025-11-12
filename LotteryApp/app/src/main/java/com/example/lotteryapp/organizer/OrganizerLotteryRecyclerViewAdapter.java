package com.example.lotteryapp.organizer;

import android.graphics.Color;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotteryapp.databinding.FragmentEventMiniLayoutBinding;
import com.example.lotteryapp.databinding.FragmentOrganizerManageLotteryListItemBinding;
import com.example.lotteryapp.reusecomponent.EventMiniRecyclerViewAdapter;
import com.example.lotteryapp.reusecomponent.LotteryEntrant;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class OrganizerLotteryRecyclerViewAdapter extends RecyclerView.Adapter<OrganizerLotteryRecyclerViewAdapter.ViewHolder> {
    private final ArrayList<LotteryEntrant> mValues;

    private FirebaseFirestore db;
    //private final Set<String> selectedUsers = new HashSet<>();


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final FragmentOrganizerManageLotteryListItemBinding binding;
        public ViewHolder(FragmentOrganizerManageLotteryListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
    public OrganizerLotteryRecyclerViewAdapter(ArrayList<LotteryEntrant> items) {
        mValues = items;
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new OrganizerLotteryRecyclerViewAdapter.ViewHolder(FragmentOrganizerManageLotteryListItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        LotteryEntrant entrant = mValues.get(position);
        db.collection("user")
                .document(entrant.uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        viewHolder.binding.fragmentOrganizerManageLotteryListName.setText(name);
                        viewHolder.binding.fragmentOrganizerManageLotteryListEmail.setText(email);
                    } else {
                        viewHolder.binding.fragmentOrganizerManageLotteryListName.setText("User not found");
                        viewHolder.binding.fragmentOrganizerManageLotteryListEmail.setText("User not found");
                    }
                });
        //viewHolder.binding.fragmentOrganizerManageLotteryCheck.setOnCheckedChangeListener(null);
        //viewHolder.binding.fragmentOrganizerManageLotteryCheck.setChecked(selectedUsers.contains(id));

        viewHolder.binding.fragmentOrganizerManageLotteryCheck.setChecked(entrant.bSelected);

        viewHolder.binding.fragmentOrganizerManageLotteryCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            //Log.d("Checkbox", "Added user: " + id_selected.first + ". Total selected: " + mValues.size());
            //Log.d("Checkbox", "Removed user: " + id_selected.first + ". Total selected: " + mValues.size());
            entrant.bSelected = isChecked;
        });

        int c;
        switch (entrant.status) {
            case Invited:
                c = Color.BLUE; break;
            case Accepted:
                c = Color.GREEN; break;
            case Declined:
            case Cancelled:
                c = Color.RED; break;
            case Waitlisted:
                c = Color.parseColor("#FF7A41"); break;
            default:
                c = Color.GRAY;
        }
        viewHolder.binding.fragmentOrganizerManageLotteryListButton.setText(entrant.status.toString());
        viewHolder.binding.fragmentOrganizerManageLotteryListButton.setBackgroundColor(c);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

//    public ArrayList<String> getSelectedUsers() {
//        return new ArrayList<>(selectedUsers);
//    }

}
