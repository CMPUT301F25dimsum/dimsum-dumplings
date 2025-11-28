package com.example.lotteryapp.entrant;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lotteryapp.placeholder.PlaceholderContent.PlaceholderItem;
import com.example.lotteryapp.databinding.FragmentEntrantSavedEventBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Purpose: Adapter for custom saved events data type displayed to the entrant. Allows Events (model) to be
 * displayed as a List (view).
 * <p>
 * Outstanding Issues: Cannot accept until lottery implemented.
 */
public class EntrantSavedEventRecyclerViewAdapter extends RecyclerView.Adapter<EntrantSavedEventRecyclerViewAdapter.ViewHolder> {

    private final List<PlaceholderItem> mValues;

    public EntrantSavedEventRecyclerViewAdapter(List<PlaceholderItem> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentEntrantSavedEventBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).id);
        holder.mContentView.setText(mValues.get(position).content);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mIdView;
        public final TextView mContentView;
        public PlaceholderItem mItem;

        public ViewHolder(FragmentEntrantSavedEventBinding binding) {
            super(binding.getRoot());
            mIdView = binding.itemNumber;
            mContentView = binding.content;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}