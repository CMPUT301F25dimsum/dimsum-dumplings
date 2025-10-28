package com.example.lotteryapp.reusecomponent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LifecycleOwner;

import com.example.lotteryapp.R;

public class EditableImage extends ConstraintLayout {
    private ImageView imageView;
    private Button button;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    public EditableImage(@NonNull Context context) {
        super(context);
        create(context);
    }

    public EditableImage(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        create(context);
    }

    public EditableImage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        create(context);
    }

    public EditableImage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        create(context);
    }

    private void create(@NonNull Context context) {
        LayoutInflater.from(context).inflate(R.layout.reuse_edittable_image, this, true);

        imageView = findViewById(R.id.reuse_editable_image_view);
        button = findViewById(R.id.reuse_editable_image_button);

        button.setOnClickListener(v -> {
            pickImageLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        });
    }

    /// ChatGPT generated lines 60-70 for registration of the swappable image activity
    public void registerLauncher(@NonNull ActivityResultCaller caller) {
        pickImageLauncher = caller.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        imageView.setImageURI(selectedImageUri);
                    }
                }
        );
    }
}
