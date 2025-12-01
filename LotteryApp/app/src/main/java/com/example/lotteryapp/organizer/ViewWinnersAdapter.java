package com.example.lotteryapp.organizer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotteryapp.R;
import com.example.lotteryapp.reusecomponent.LotteryEntrant;
import com.example.lotteryapp.reusecomponent.UserProfile;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ViewWinnersAdapter extends RecyclerView.Adapter<ViewWinnersAdapter.ViewHolder> {

    private final ArrayList<LotteryEntrant> mValues;
    private FirebaseFirestore db;

    public ViewWinnersAdapter(ArrayList<LotteryEntrant> items) {
        mValues = items;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_winners_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        String uid = mValues.get(position).getUid();
        db.collection("user").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
                if (userProfile != null) {
                    holder.mWinnerName.setText(userProfile.name);
                    holder.mWinnerEmail.setText(userProfile.email);
                    holder.mWinnerStatus.setText(mValues.get(position).getStatus());
                    if (userProfile.phone_num != null && !userProfile.phone_num.isEmpty()) {
                        holder.mWinnerPhone.setText(userProfile.phone_num);
                        holder.mWinnerPhone.setVisibility(View.VISIBLE);
                    } else {
                        holder.mWinnerPhone.setVisibility(View.GONE);
                    }

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mWinnerName;
        public final TextView mWinnerEmail;
        public final TextView mWinnerPhone;
        public final TextView mWinnerStatus;

        public ViewHolder(View view) {
            super(view);
            mWinnerName = view.findViewById(R.id.winner_name);
            mWinnerEmail = view.findViewById(R.id.winner_email);
            mWinnerPhone = view.findViewById(R.id.winner_phone);
            mWinnerStatus = view.findViewById(R.id.winner_status);
        }
    }
}
