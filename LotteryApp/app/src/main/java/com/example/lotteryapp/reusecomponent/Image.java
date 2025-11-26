package com.example.lotteryapp.reusecomponent;

import android.graphics.Bitmap;

/**
 * Holds an image in the form of a bitmap and its representative id in the database.
 *
 */
public class Image {
    public String id;
    public Bitmap image;

    public Image(String id, Bitmap image){
        this.id = id;
        this.image = image;
    }

}
