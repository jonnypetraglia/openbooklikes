package com.qweex.openbooklikes.model;

import android.graphics.Bitmap;
import android.util.Log;

import com.qweex.openbooklikes.AndThen;
import com.qweex.openbooklikes.ImageUtils;

abstract public class Base {

    // Non-api entities
    public Bitmap bitmap;

    abstract public void persist();

    private AndThen<Bitmap> setImage = new AndThen<Bitmap>() {
        @Override
        public void call(Bitmap b) {
            Log.d("OBL:User", "bitmap is null: " + (b == null));
            bitmap = b;
        }
    };

    protected void downloadImage(String imageUrl, AndThen then) {
        Log.d("OBL:downloadImage", "imageurl is " + imageUrl);
        if(imageUrl!=null)
            new ImageUtils.DownloadImageTask(imageUrl).execute(setImage, then);
        else
            then.call(null);
    }
}
