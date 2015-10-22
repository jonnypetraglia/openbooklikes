package com.qweex.openbooklikes;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TableLayout;

import com.qweex.openbooklikes.model.Book;


public class BookFragment extends FragmentBase<Book> {

    static final int IMG_SIZE_PX = 700;
    int imgHeight;

    @Override
    public String getTitle() {
        return primary.getS("title");
    }

    @Override
    public void setArguments(Bundle a) {
        Log.d("OBL", "setArguments " + a.getBundle("book").getString("cover"));
        primary = new Book(a);
        Log.d("OBL", "setArguments " + primary.id() + " | " + primary.getS("cover"));
        imgHeight = a.getInt("imgHeight");
        super.setArguments(a);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        adjustOrientation(getView(), newConfig);
    }

    public void adjustOrientation(View v, Configuration config) {

        int ALIGN_START = RelativeLayout.ALIGN_LEFT;
        if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN)
            ALIGN_START = RelativeLayout.ALIGN_START;

        int mar = MainActivity.dpToPx(20);
        int lHeight = MainActivity.dpToPx(config.screenHeightDp)
                - ((MainActivity)getActivity()).getStatusBarHeight()
                - ((MainActivity)getActivity()).getActionBarHeight();


        View cover = v.findViewById(R.id.cover),
             title = v.findViewById(R.id.title),
                author = v.findViewById(R.id.author),
             table = v.findViewById(R.id.table);
        LayoutParams coverlp = (LayoutParams) cover.getLayoutParams(),
                titlelp = (LayoutParams) title.getLayoutParams(),
                authorlp = (LayoutParams) author.getLayoutParams(),
                tablelp = (LayoutParams) table.getLayoutParams();


        ((TableLayout)table).setColumnStretchable(0, true);
        ((TableLayout)table).setColumnStretchable(1, true);

        if(config.orientation==Configuration.ORIENTATION_LANDSCAPE) {
//            mar*=2;
            coverlp.setMargins(mar, mar, 0, 0);
            titlelp.setMargins(0, 0, mar, 0);
            tablelp.setMargins(0, mar, 0, mar/2);
            coverlp.height = (int) Math.min(
                    IMG_SIZE_PX,
                    lHeight - mar*2
            );
            Log.d("OBL", "h=" + coverlp.height);

            coverlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            coverlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            titlelp.addRule(RelativeLayout.RIGHT_OF, R.id.cover);
            titlelp.addRule(RelativeLayout.ALIGN_TOP, R.id.cover);
            authorlp.addRule(ALIGN_START, R.id.title);
            tablelp.addRule(RelativeLayout.ALIGN_LEFT, R.id.title);

            coverlp.addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
            titlelp.addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
            authorlp.addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
            tablelp.addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
            titlelp.addRule(RelativeLayout.BELOW, 0);
        } else {
            coverlp.setMargins(mar, mar, mar, mar);
            titlelp.setMargins(mar, 0, mar, 0);
            tablelp.setMargins(mar, mar, mar, mar);
            coverlp.height = IMG_SIZE_PX;

            coverlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            coverlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            titlelp.addRule(RelativeLayout.RIGHT_OF, 0);
            titlelp.addRule(RelativeLayout.ALIGN_TOP, 0);
            authorlp.addRule(ALIGN_START, 0);
            tablelp.addRule(RelativeLayout.ALIGN_LEFT, 0);

            coverlp.addRule(RelativeLayout.CENTER_HORIZONTAL);
            titlelp.addRule(RelativeLayout.CENTER_HORIZONTAL);
            authorlp.addRule(RelativeLayout.CENTER_HORIZONTAL);
            tablelp.addRule(RelativeLayout.CENTER_HORIZONTAL);
            titlelp.addRule(RelativeLayout.BELOW, R.id.cover);
        }

        ((ImageView) cover).setImageBitmap(null);
        MainActivity.imageLoader.displayImage(
                primary.getS("cover").replace("300/300", IMG_SIZE_PX + "/" + IMG_SIZE_PX),
                (ImageView) cover);
        cover.requestLayout();
        title.requestLayout();
        author.requestLayout();
        table.requestLayout();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book, container, false);

        ImageView cover = (ImageView) view.findViewById(R.id.cover);
        MainActivity.imageLoader.displayImage(primary.getS("cover"), cover);

        setOrHide(view, R.id.title, primary.getS("title"));
        setOrHide(view, R.id.author, primary.getS("author"));
        // And this is where I'd put a Description
        //
        // IF I HAD ONE

        setOrHide(view, R.id.format, primary.getS("format")); //TODO: Figure out what the numbers map to
        setOrHide(view, R.id.isbn_13, primary.getS("isbn_13"));
        setOrHide(view, R.id.isbn_10, primary.getS("isbn_10"));
        setOrHide(view, R.id.publishDate, primary.getS("publish_date"));
        setOrHide(view, R.id.publisher, primary.getS("publisher"));
        setOrHide(view, R.id.pageCount, primary.getS("pages"));
        setOrHide(view, R.id.language, primary.getS("language"));


        adjustOrientation(view, getActivity().getResources().getConfiguration());

        return super.createProgressView(inflater, container, view);
    }
}
