package com.example.lotteryapp.reusecomponent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

public class Image {
    public String encodedImage;

    // Empty constructor required for Firestore
    public Image(){}
    public Image(String encodedImage){
        this.encodedImage = encodedImage;
    }

    /**
     * Decodes a Base64 encoded String into a Bitmap image.
     * @return The decoded Bitmap, or null if an error occurred.
     */
    public Bitmap getBitmap() {
        try {
            // Decode the Base64 string into a byte array using Android's Base64 utility
            byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);

            // Use BitmapFactory to create a Bitmap from the byte array
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

        } catch (IllegalArgumentException e) {
            Log.e("Base64Decoder", "Invalid Base64 string", e);
            return null;
        }
    }
}
