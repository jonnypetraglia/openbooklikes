package com.qweex.openbooklikes.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.loopj.android.http.RequestParams;
import com.qweex.openbooklikes.ApiClient;
import com.qweex.openbooklikes.LoadingViewManagerDialog;
import com.qweex.openbooklikes.R;
import com.qweex.openbooklikes.SettingsManager;
import com.qweex.openbooklikes.activity.MainActivity;
import com.qweex.openbooklikes.handler.LoadingResponseHandler;
import com.qweex.openbooklikes.model.Book;
import com.qweex.openbooklikes.model.BookListPartial;
import com.qweex.openbooklikes.model.Shelf;
import com.qweex.openbooklikes.notmine.Misc;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class BookFragment extends FragmentBase<Book> {
    int imgHeight;
    NumberProgressBar bookProgress;
    MenuItem addToShelfMenuItem;

    @Override
    public String getTitle(Resources r) {
        return primary.getS("title");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("imgHeight", imgHeight);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        responseHandler = new PageResponseHandler() {
            @Override
            protected String urlPath() {
                return "book/GetPageCurrent";
            }
        };
        reload();
    }

    abstract class PageResponseHandler extends ApiClient.ApiResponseHandler {
        @Override
        protected String countFieldName() {
            return null;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);
            addToShelfMenuItem.setEnabled(true);
            try {
                if(!response.has("book_page_currently") || response.getString("book_page_currently").equals("0"))
                    return;
                Log.d("Hello", response.getString("book_page_max") + "!");
                bookProgress.setMax(Integer.parseInt(response.getString("book_page_max")));
                bookProgress.setProgress(Integer.parseInt(response.getString("book_page_currently")));
                bookProgress.setVisibility(View.VISIBLE);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bookProgress = (NumberProgressBar) view.findViewById(R.id.number_progress_bar);
    }

    void reload() {
        addToShelfMenuItem.setEnabled(false);
        RequestParams params = new RequestParams();
        params.put("bid", primary.id());
        ApiClient.get(params, responseHandler);
    }

    void update(int current, int max) {
        RequestParams params = new RequestParams();
        params.put("bid", primary.id());
        params.put("PageCurrently", current);
        params.put("PageMax", max);
        ApiClient.get(params, new PageResponseHandler() {
            @Override
            protected String urlPath() {
                return "book/SetPageCurrent";
            }
        });
    }

    void addToShelf(AddChoices choices) {
        RequestParams params = new RequestParams();
        params.put("bid", primary.id());
        if(choices.wish)
            params.put("BookIsWish", choices.wish);
        if(choices.fav)
            params.put("Favourite", choices.fav);
        if(!choices.shelfId.equals(BookListPartial.NO_SHELF_ID))
            params.put("Cat", choices.wish);
        if(choices.status!=null)
            params.put("BookStatus", choices.status);
        if(choices.pageMax > 0)
            params.put("PageMax", choices.pageMax);
        if(choices.pageCurrent > 0)
            params.put("PageCurrently", choices.pageCurrent);
        if(choices.rating!=null)
            params.put("BookUserRating", choices.rating);
        if(choices.priv)
            params.put("Private", choices.priv);

        Log.d("Test", choices.toString());

        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "Loading", null, false, false);
        final LoadingViewManagerDialog loadingManager = new LoadingViewManagerDialog(
                getMainActivity().findViewById(R.id.side_fragment),
                R.string.successfully_updated
        );

        ApiClient.get(params, new LoadingResponseHandler(loadingManager) {
            @Override
            protected String urlPath() {
                return "book/AddBookToShelfASDF";
            }

            @Override
            protected String countFieldName() {
                return null;
            }
        });
    }

    void showUpdateDialog() {
        View layout = getActivity().getLayoutInflater().inflate(R.layout.update_page, null);
        final TextView current = (TextView) layout.findViewById(R.id.current_of_total),
                max = (TextView) layout.findViewById(R.id.count);
        current.setText(Integer.toString(bookProgress.getProgress()));
        max.setText(Integer.toString(bookProgress.getMax()));
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.update_progress)
                        //.setMessage(primary.getS("title"))
                .setView(layout)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        bookProgress.setVisibility(View.INVISIBLE);
                        update(
                                Integer.parseInt(current.getText().toString()),
                                Integer.parseInt(max.getText().toString())
                        );
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    void showAddDialog() {
        final AddChoices result = new AddChoices();

        final int resid = android.R.layout.simple_list_item_single_choice;
        final ArrayAdapter<Shelf> adapter = new ArrayAdapter<Shelf>(getActivity(), resid, MainActivity.shelves) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null)
                    convertView = getActivity().getLayoutInflater().inflate(resid, null);
                Shelf shelf = getItem(position);
                CheckedTextView ctv = ((CheckedTextView) convertView.findViewById(android.R.id.text1));
                if(shelf.isAllBooks())
                    ctv.setText(R.string.none);
                else
                    ctv.setText(shelf.getTitle(getResources()));
                if(shelf.id().equals(result.shelfId) || result.shelfId == null) {
                    ctv.setChecked(true);
                    result.shelfId = shelf.id();
                }
                return convertView;
            }
        };


        final RelativeLayout rl = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.add_book, null);

        // Set current page and max page
        ((TextView) rl.findViewById(R.id.current_of_total)).setText(
                Integer.toString(bookProgress.getProgress())
        );
        ((TextView) rl.findViewById(R.id.count)).setText(primary.getS("pages"));

        ListView listView = new ListView(getActivity());
        listView.setVerticalFadingEdgeEnabled(true);
        listView.setFadingEdgeLength(Misc.convertPixelsToDp(25, getActivity()));
        listView.addFooterView(rl);
        listView.setAdapter(adapter);
        listView.setItemsCanFocus(true);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setItemChecked(0, true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((CheckedTextView) view).setChecked(true);
                result.shelfId = adapter.getItem(i).id();
            }
        });
        Log.d("Herp", "!" + adapter.getCount());


        AlertDialog alert = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.add_to_shelf)
                        .setView(listView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //TODO: result.status
                        int statusId = ((RadioGroup) rl.findViewById(R.id.status)).getCheckedRadioButtonId();
                        switch (statusId) {
                            case R.id.filter_planning:
                                result.status = "planing"; /* sic */
                                break;
                            case R.id.filter_reading:
                                result.status = "currently";
                                break;
                            case R.id.filter_read:
                                result.status = "read";
                                break;
                            case R.id.filter_all:
                            default:
                        }
                        result.rating = String.format("%.1f", ((RatingBar) rl.findViewById(R.id.rating)).getRating());
                        result.fav = ((CheckBox) rl.findViewById(R.id.filter_favourite)).isChecked();
                        result.wish = ((CheckBox) rl.findViewById(R.id.filter_wishlist)).isChecked();
                        result.priv = ((CheckBox) rl.findViewById(R.id.filter_private)).isChecked();
                        result.priv = ((CheckBox) rl.findViewById(R.id.filter_private)).isChecked();
                        result.pageCurrent = Integer.parseInt(((TextView) rl.findViewById(R.id.current_of_total)).getText().toString());
                        result.pageMax = Integer.parseInt(((TextView) rl.findViewById(R.id.count)).getText().toString());

                        addToShelf(result);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        alert.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }

    class AddChoices {
        public String shelfId = null, status = null, rating = null /* example: "4.5" */;
        boolean fav = false, wish = false, priv = false;
        int pageMax = 0, pageCurrent = 0;

        @Override
        public String toString() {
            return "shelf=" + shelfId + "; "
                    + "status=" + status + "; "
                    + "rating=" + rating + "; "
                    + "fav=" + fav + "; "
                    + "wish=" + wish + "; "
                    + "priv=" + priv + "; "
                    + "pageCurrent=" + pageCurrent + "; "
                    + "pageMax=" + pageMax + ";";
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem mi;

        mi = menu.add(Menu.NONE, R.id.option_update, Menu.NONE, R.string.option_update);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        mi.setIcon(R.drawable.edit_np330037);
        optionIcon(mi);

        mi = menu.add(Menu.NONE, R.id.option_reload, Menu.NONE, R.string.reload)
            .setIcon(R.drawable.reload_np45438);
        optionIcon(mi);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        addToShelfMenuItem = menu.add(Menu.NONE, R.id.option_add, Menu.NONE, R.string.option_add);
        addToShelfMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        setHasOptionsMenu(true);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.option_reload:
                bookProgress.setVisibility(View.INVISIBLE);
                reload();
                break;
            case R.id.option_update:
                showUpdateDialog();
                break;
            case R.id.option_add:
                showAddDialog();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book, container, false);

        ImageView cover = (ImageView) view.findViewById(R.id.image_view);
        MainActivity.imageLoader.displayImage(primary.getS("cover"), cover);

        setOrHide(view, R.id.title, primary.getS("title"));
        setOrHide(view, R.id.author, primary.getS("author").replaceAll(",", "<br>"));
        // And this is where I'd put a Description
        //
        // IF I HAD ONE

        String format = primary.getS("format");
        setOrHide(view, R.id.format,
                SettingsManager.bookFormats.containsKey(format)
                ? SettingsManager.bookFormats.get(format) : format
        );
        setOrHide(view, R.id.isbn_13, primary.getS("isbn_13"));
        setOrHide(view, R.id.isbn_10, primary.getS("isbn_10"));
        setOrHide(view, R.id.date, primary.getS("publish_date"));
        setOrHide(view, R.id.publisher, primary.getS("publisher"));
        setOrHide(view, R.id.pages, primary.getS("pages"));
        setOrHide(view, R.id.language, primary.getS("language"));


        adjustOrientation(view, getActivity().getResources().getConfiguration());

        return super.createProgressView(inflater, container, view);
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

        int mar = Misc.convertDpToPixel(20, v.getContext());
        int lHeight = Misc.convertDpToPixel(config.screenHeightDp, v.getContext())
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

}
