package com.example.lotteryapp.admin;

import static android.view.View.GONE;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lotteryapp.databinding.FragmentAdminNoticeBinding;
import com.example.lotteryapp.reusecomponent.Event;
import com.example.lotteryapp.reusecomponent.EventDisplayFragment;
import com.example.lotteryapp.reusecomponent.Notification;
import com.example.lotteryapp.reusecomponent.UserProfile;
import com.example.lotteryapp.databinding.FragmentAdminProfileBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Purpose: Adapter for custom profile data type displayed to the admin. Allows Profile (model) to be
 * displayed as a List (view).
 * <p>
 * Outstanding Issues:
 */
public class AdminProfileRecyclerViewAdapter extends RecyclerView.Adapter<AdminProfileRecyclerViewAdapter.ViewHolder> {

    private final List<UserProfile> mValues;
    private FragmentManager fragmentManager;
    private FirebaseFirestore db;

    public AdminProfileRecyclerViewAdapter(List<UserProfile> items, FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        mValues = items;
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentAdminProfileBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        UserProfile profile = mValues.get(position);

        holder.binding.reuseProfileViewName.setText(profile.name);
        holder.binding.reuseProfileEmail.setText(profile.email);
        if (profile.acc_type == UserProfile.Type.admin)
            holder.binding.reuseProfileRole.setText("Admin");
        else if (profile.acc_type == UserProfile.Type.organizer)
            holder.binding.reuseProfileRole.setText("Organizer");
        else holder.binding.reuseProfileRole.setText("Entrant");

        holder.binding.reuseProfileButton.setOnClickListener(v -> {
            db.collection("events").document(profile.uid).delete();         // organizer events
            db.collection("notifications").document(profile.uid).delete();  // may want to keep this for security idk
            db.collection("user").document(profile.uid).delete();
            holder.binding.reuseProfileButton.setText("Deleted");
            holder.binding.reuseProfileButton.setBackgroundColor(Color.parseColor("#DD0202"));
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final FragmentAdminProfileBinding binding;

        public ViewHolder(FragmentAdminProfileBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}