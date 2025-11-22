package com.example.lotteryapp.reusecomponent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.lotteryapp.databinding.FragmentEventMiniLayoutBinding;
import com.example.lotteryapp.databinding.ReusableImageAdapterBinding;

import java.util.List;

/**
 * Purpose: Adapter for displaying a list of images stored as Bitmap and their database id.
 */
public class ImageRecyclerViewAdapter extends RecyclerView.Adapter<ImageRecyclerViewAdapter.ViewHolder> {

    private final List<Image> mValues;
    private ImageActionListener listener;

    // Interface defined inside adapter
    public interface ImageActionListener {
        void onRemoveImage(Image image);
    }

    public ImageRecyclerViewAdapter(List<Image> items, ImageActionListener listener) {
        mValues = items;
        this.listener = listener;
    }

    @Override
    public ImageRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ImageRecyclerViewAdapter.ViewHolder(ReusableImageAdapterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Image image = mValues.get(position);

        // Set bitmap
        if (image.image != null) {
            holder.binding.imageView.setImageBitmap(image.image);
        } else {
            holder.binding.imageView.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        // Remove button callback
        holder.binding.removeImageButton.setOnClickListener(v -> {
           if (listener != null)
               listener.onRemoveImage(image);
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final ReusableImageAdapterBinding binding;
//        public Notification notification;

        public ViewHolder(ReusableImageAdapterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
