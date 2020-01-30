package com.example.myapplication.Models;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.Nullable;

public class MyImage {

    //My custom parcelable object that defines an image(event or user image)

    private Uri imgUri;
    private boolean isSelected;
    private int progress;
    private Bitmap bitmap;
    private String name;

    public MyImage(Uri imgUri) {
        this.imgUri = imgUri;
        this.isSelected = false;
        this.setProgress(0);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Uri getImgUri() {
        return imgUri;
    }

    public void setImgUri(Uri imgUri) {
        this.imgUri = imgUri;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof MyImage){
            MyImage toCompare = (MyImage) obj;
            return this.imgUri.equals(toCompare.imgUri) || this.name.equals(toCompare.name);
        }else{
            return false;
        }
    }
}
