package com.example.lotteryapp.admin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lotteryapp.R;
import com.example.lotteryapp.placeholder.PlaceholderContent;
import com.example.lotteryapp.reusecomponent.Image;
import com.example.lotteryapp.reusecomponent.ImageRecyclerViewAdapter;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * Purpose: Fragment for displaying a list of images of each event to the admin
 * <p>
 * Outstanding Issues: Not completed, may require Firebase Storage upgrade
 */
public class AdminImageFragment extends Fragment implements ImageRecyclerViewAdapter.ImageActionListener {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private StorageReference db;
    private ArrayList<Image> mValues;
    private ImageRecyclerViewAdapter adapter;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AdminImageFragment() {
    }

    public void onRemoveImage(Image image){
        int index = mValues.indexOf(image);
        if (index != -1){
            // Delete from FirebaseStorage
            FirebaseStorage.getInstance().getReference()
                    .child(image.id).delete()
                    .addOnSuccessListener(aVoid -> {
                        mValues.remove(image);
                        adapter.notifyItemRemoved(index);
                    });
        }
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static AdminImageFragment newInstance(int columnCount) {
        AdminImageFragment fragment = new AdminImageFragment();
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
        db = FirebaseStorage.getInstance().getReference().child("images");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_image_list, container, false);

        // Set the adapter
        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view;
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        recyclerView.setAdapter(new AdminImageRecyclerViewAdapter(PlaceholderContent.ITEMS));

        mValues = new ArrayList<>();
        adapter = new ImageRecyclerViewAdapter(mValues, this);
        recyclerView.setAdapter(adapter);

        db.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference item : listResult.getItems()){
                        String id = item.getPath();;
                        // Download each file as a Bitmap and store reference as Image
                        final long ONE_MEGABYTE = 1024 * 1024;
                        item.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            mValues.add(new Image(id, bitmap));

                            // Optionally notify your adapter
                            adapter.notifyItemChanged(mValues.size()-1);

                        });
                    }
                });
        return view;
    }
}