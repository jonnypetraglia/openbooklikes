package com.qweex.openbooklikes.notmine;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Misc {

    public static int convertDpToPixel(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int) px;
    }

    public static int convertPixelsToDp(float px){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return (int) dp;
    }

    public static Drawable resizeDrawable(VectorDrawable vectorDrawable, int width, int height) {
        if(width==0)
            width = vectorDrawable.getIntrinsicWidth();
        if(height==0)
            height = vectorDrawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return new BitmapDrawable(bitmap);
    }

    // http://stackoverflow.com/a/24048309/1526210

    public static String extractYTId(String ytUrl) {
        //^https?://.*(?:youtu(be\.com|\.be)\/|v/|u\/|w\/|embed\/|watch\?v=)([^#&?]*).*$
        Pattern pattern = Pattern.compile(
                "^https?://.*(?:youtu(be\\.com|\\.be)\\/|v/|u\\/|w\\/|embed\\/|watch\\?v=)([^#&?]*).*$",
//              "^https?://.*(?:youtu.be/|v/|u/\\w/|embed/|watch?v=)([^#&?]*).*$",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(ytUrl);
        if (matcher.matches())
            return matcher.group(2);
        return null;
    }
}
