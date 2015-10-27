package com.qweex.openbooklikes;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

public class FullDrawerLayout extends DrawerLayout{
    final int MIN_MARGIN = dpToPx(64); // Defined in DrawerLayout

    final int TRUE_MARGIN = dpToPx(295);

    public FullDrawerLayout(Context context) {
        super(context);
    }

    public FullDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FullDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if(child.getVisibility()==GONE || !isDrawerView(child))
                continue;

            if(getGrav(child) != Gravity.RIGHT) //FIXME: This just uses the default margin in DrawerLayout for the START drawer
                continue;

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            final int margin = (getGrav(child) == Gravity.LEFT) ? TRUE_MARGIN : 0;


            Log.d("MARGIN", margin + " >_<");
            final int drawerWidthSpec = getChildMeasureSpec(widthMeasureSpec,
                    margin + lp.leftMargin + lp.rightMargin,
                    lp.width);

            final int drawerHeightSpec = getChildMeasureSpec(heightMeasureSpec,
                    lp.topMargin + lp.bottomMargin,
                    lp.height);

            child.measure(drawerWidthSpec, drawerHeightSpec);
        }
    }

    int getGrav(View child) {
        final int gravity = ((LayoutParams) child.getLayoutParams()).gravity;
        return GravityCompat.getAbsoluteGravity(gravity,
                ViewCompat.getLayoutDirection(child));
    }

    boolean isDrawerView(View child) {
        return (getGrav(child) & (Gravity.LEFT | Gravity.RIGHT)) != 0;
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
