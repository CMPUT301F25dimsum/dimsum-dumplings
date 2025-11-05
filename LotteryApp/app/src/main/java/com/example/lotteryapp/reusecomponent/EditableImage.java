package com.example.lotteryapp.reusecomponent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditableImage extends ConstraintLayout {
    private ImageView imageView;
    private Button button;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    public EditableImage(@NonNull Context context) {
        super(context);
        selectedImageUri = null;
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

    public void reset(){
        imageView.setImageURI(null);
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
                        selectedImageUri = result.getData().getData();
                        imageView.setImageURI(selectedImageUri);
                    }
                }
        );
    }

    /**
     * Converts an image URI to a Base64 encoded String.
     * @param context The application context (e.g., YourActivity.this).
     * @return The Base64 encoded string, or null if an error occurred.
     */
    public String encodeImageUriToBase64(Context context) {
        if (selectedImageUri == null)
            return null;

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(selectedImageUri);
            if (inputStream == null) {
                return null;
            }

            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;

            // Read the image data into a byte array
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }

            // Close the input stream
            inputStream.close();

            // Get the byte array
            byte[] imageBytes = byteBuffer.toByteArray();

            // Encode the byte array to a Base64 string using Android's Base64 utility
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);

        } catch (IOException e) {
            Log.e("Editable Image", e.getMessage());
            return null;
        }
    }
}
