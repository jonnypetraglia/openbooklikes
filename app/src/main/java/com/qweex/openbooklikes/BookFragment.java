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
    int imgHeight;

    @Override
    public String getTitle() {
        return primary.getS("title");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("imgHeight", imgHeight);
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


        View cover = v.findViewById(R.id.image_view),
             title = v.findViewById(R.id.title),
             author = v.findViewById(R.id.author),
             table = v.findViewById(R.id.table);
        LayoutParams coverlp = (LayoutParams) cover.getLayoutParams(),
                titlelp = (LayoutParams) title.getLayoutParams(),
                authorlp = (LayoutParams) author.getLayoutParams(),
                tablelp = (LayoutParams) table.getLayoutParams();


        int IMG_SIZE = getResources().getDimensionPixelSize(R.dimen.book_size);

        if(config.orientation==Configuration.ORIENTATION_LANDSCAPE) {
//            mar*=2;
            coverlp.setMargins(mar, mar, 0, 0);
            titlelp.setMargins(0, 0, mar, 0);
            tablelp.setMargins(0, mar, 0, mar/2);
            coverlp.height = Math.min(
                    IMG_SIZE,
                    lHeight - mar*2
            );
            Log.d("OBL", "h=" + coverlp.height);

            coverlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            coverlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            titlelp.addRule(RelativeLayout.RIGHT_OF, cover.getId());
            titlelp.addRule(RelativeLayout.ALIGN_TOP, cover.getId());
            authorlp.addRule(ALIGN_START, title.getId());
            tablelp.addRule(RelativeLayout.ALIGN_LEFT, title.getId());

            coverlp.addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
            titlelp.addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
            authorlp.addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
            tablelp.addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
            titlelp.addRule(RelativeLayout.BELOW, 0);
        } else {
            coverlp.setMargins(mar, mar, mar, mar);
            titlelp.setMargins(mar, 0, mar, 0);
            tablelp.setMargins(mar, mar, mar, mar);
            coverlp.height = IMG_SIZE;

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
            titlelp.addRule(RelativeLayout.BELOW, cover.getId());
        }

        ((ImageView) cover).setImageBitmap(null);
        MainActivity.imageLoader.displayImage(
                primary.getS("cover").replace("300/300", IMG_SIZE + "/" + IMG_SIZE),
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

        ImageView cover = (ImageView) view.findViewById(R.id.image_view);
        MainActivity.imageLoader.displayImage(primary.getS("cover"), cover);

        setOrHide(view, R.id.title, primary.getS("title"));
        setOrHide(view, R.id.author, primary.getS("author"));
        // And this is where I'd put a Description
        //
        // IF I HAD ONE

        setOrHide(view, R.id.format, primary.getS("format")); //TODO: Figure out what the numbers map to
        setOrHide(view, R.id.isbn_13, primary.getS("isbn_13"));
        setOrHide(view, R.id.isbn_10, primary.getS("isbn_10"));
        setOrHide(view, R.id.date, primary.getS("publish_date"));
        setOrHide(view, R.id.publisher, primary.getS("publisher"));
        setOrHide(view, R.id.pages, primary.getS("pages"));
        setOrHide(view, R.id.language, primary.getS("language"));


        adjustOrientation(view, getActivity().getResources().getConfiguration());

        return super.createProgressView(inflater, container, view);
    }
}
