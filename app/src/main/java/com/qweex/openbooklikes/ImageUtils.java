package com.qweex.openbooklikes;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ImageUtils {

    public static class DownloadImageTask extends AsyncTask<AndThen, Void, Bitmap> {
        String url;
        AndThen[] thens;

        public DownloadImageTask(String url) {
            this.url = url;
        }

        protected Bitmap doInBackground(AndThen... thens) {
            Log.d("OBL:DownloadImageTask", "Downloading " + url);
            this.thens = thens;
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("OBL:DownloadImageTask", e.getMessage());
                e.printStackTrace();
            }
            Log.d("OBL:DownloadImageTask", "Done is null? " + (mIcon11==null));
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            for (AndThen then : thens)
                then.call(result);
        }
    }

    public static Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,
                    encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    public static String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }
}