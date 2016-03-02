package com.qweex.openbooklikes.fragment;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.qweex.openbooklikes.AdapterBase;
import com.qweex.openbooklikes.ApiClient;
import com.qweex.openbooklikes.LoadingViewInterface;
import com.qweex.openbooklikes.LoadingViewManager;
import com.qweex.openbooklikes.R;
import com.qweex.openbooklikes.SettingsManager;
import com.qweex.openbooklikes.activity.MainActivity;
import com.qweex.openbooklikes.handler.LoadingResponseHandler;
import com.qweex.openbooklikes.model.Book;
import com.qweex.openbooklikes.model.BookListPartial;
import com.qweex.openbooklikes.model.Shelf;
import com.qweex.openbooklikes.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import jp.co.recruit_mp.android.widget.HeaderFooterGridView;


public class BookListFragment<BookList extends BookListPartial> extends FetchFragmentBase<BookList, Book> implements AdapterView.OnItemClickListener {
    User owner;
    HeaderFooterGridView gridView;

    static CheckTracker statusTracker, specialTracker;
    LoadingViewManager gridLoadingManager = new LoadingViewManager(), listLoadingManager = new LoadingViewManager();

    static {
        statusTracker = new BookListFragment.CheckTracker();
        statusTracker.add(R.id.filter_all, R.id.filter_read, R.id.filter_planning, R.id.filter_reading);

        specialTracker = new BookListFragment.CheckTracker();
        specialTracker.add(R.id.filter_favourite, R.id.filter_wishlist, R.id.filter_reviewed, R.id.filter_private);
        //TODO: Settings
    }

    @Override
    public String getTitle(Resources r) {
        return primary.getTitle(r);
    }

    @Override
    public void setArguments(Bundle a) {
        owner = new User(a);
        //noinspection unchecked
        primary = (BookList) new Shelf(a, owner);
        super.setArguments(a);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(primary!=null && savedInstanceState==null)
            reload();
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        adapter = new CoverAdapter(getActivity(), new ArrayList<Book>());
        int filters = SettingsManager.FILTER_ALL;
        if(getArguments()!=null)
            filters = getArguments().getInt("filters", SettingsManager.FILTER_ALL);
        SettingsManager.setFilters(getActivity(), statusTracker, specialTracker, filters);
    }

    FrameLayout wrapInFrame(View v) {
        FrameLayout fl = new FrameLayout(getContext());
        fl.addView(v, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        return fl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_list, container, false);

        View loadingGrid = inflater.inflate(R.layout.loading_horz, null),
             loadingList = inflater.inflate(R.layout.loading_horz, null),
             emptyGrid = inflater.inflate(R.layout.empty, null),
             emptyList = inflater.inflate(R.layout.empty, null),
             errorGrid = inflater.inflate(R.layout.error, null),
                errorList = inflater.inflate(R.layout.error, null);


        gridView = (HeaderFooterGridView) view.findViewById(R.id.grid_view);
        gridView.setColumnWidth((int) getResources().getDimension(R.dimen.list_book_size));
        gridView.setOnScrollListener(scrollMuch);
        gridView.setOnItemClickListener(this);
        gridView.setHorizontalSpacing(0);
        gridView.setVerticalSpacing(0);
        gridView.addFooterView(loadingGrid);
        gridView.addFooterView(emptyGrid);
        gridView.addFooterView(errorGrid);


        listView = (ListView) view.findViewById(R.id.list_view);
        listView.setOnScrollListener(scrollMuch);
        listView.setOnItemClickListener(this);
        listView.addFooterView(wrapInFrame(loadingList));
        listView.addFooterView(wrapInFrame(emptyList));
        listView.addFooterView(wrapInFrame(errorList));

        errorGrid.findViewById(R.id.retry).setOnClickListener(retryLoad);
        errorList.findViewById(R.id.retry).setOnClickListener(retryLoad);

        View loadingView = inflater.inflate(R.layout.loading, null),
                emptyView = inflater.inflate(R.layout.empty, null),
                errorView = inflater.inflate(R.layout.error, null);

        gridLoadingManager.setInitial(loadingView, view, emptyView, errorView);
        gridLoadingManager.setMore(loadingGrid, gridView, emptyGrid, errorGrid);
        gridLoadingManager.changeState(LoadingViewManager.State.INITIAL);
        gridLoadingManager.content();
        listLoadingManager.setInitial(loadingView, view, emptyView, errorView);
        listLoadingManager.setMore(loadingList, listView, emptyList, errorList);
        listLoadingManager.changeState(LoadingViewManager.State.INITIAL);
        listLoadingManager.content();


        int id = SettingsManager.getId(getActivity(), "shelf_view", R.string.default_shelf_view);
        changeWidget((AbsListView) view.findViewById(id));

        return gridLoadingManager.wrapInitialInLayout(getContext());
    }

    public void changeWidget(AbsListView choice) {
        LoadingViewManager otherManager;
        AbsListView otherView;
        if(choice==listView) {
            loadingManager = listLoadingManager;
            otherManager = gridLoadingManager;
            adapter = new DetailsAdapter(getActivity(), adapter!=null ? adapter.getData() : new ArrayList<Book>()); //FIXME: This is ugly
            otherView = gridView;
        } else if(choice==gridView) {
            loadingManager = gridLoadingManager;
            otherManager = listLoadingManager;
            adapter = new CoverAdapter(getActivity(), adapter!=null ? adapter.getData() : new ArrayList<Book>()); //FIXME: This is also ugly
            otherView = listView;
        } else
            throw new RuntimeException("Tried to change to an unknown widget");

        choice.setAdapter(adapter);
        otherView.setVisibility(View.GONE);
        choice.setVisibility(View.VISIBLE);
        responseHandler = new BookHandler(loadingManager);
        loadingManager.changeState(otherManager.getState());
        loadingManager.content();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_shelf, menu);

        Log.d("OBL:createOptions", "?");
        Menu submenu = menu.findItem(R.id.filter_status).getSubMenu();
        for(int id : statusTracker.getChecked()) {
            Log.d("OBL:checkedT", id + "!" + submenu.findItem(id).getTitle());
            submenu.findItem(id).setChecked(true);
        }
        submenu = menu.findItem(R.id.filter_special).getSubMenu();
        for(int id : specialTracker.getChecked()) {
            Log.d("OBL:checkedP", id + "!" + submenu.findItem(id).getTitle());
            submenu.findItem(id).setChecked(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.d("OBL", "OptionSelected");

        if(id==R.id.change_view) {
            if(gridView.getVisibility()!=View.GONE) {
                item.setTitle(R.string.option_view_grid);
                changeWidget(listView);
            } else {
                item.setTitle(R.string.option_view_list);
                changeWidget(gridView);
            }
            return true;
        }

        if(statusTracker.has(id)) {
            item.setChecked(true);
            statusTracker.checkEx(id);
            reload();
            Log.d("OBL", "optionselected? " + item.getTitle() + "=" + item.isChecked());
            return true;
        }
        if(specialTracker.has(id)) {
            item.setChecked(!item.isChecked());
            specialTracker.check(id, item.isChecked());
            reload();
            Log.d("OBL", "optionselected? " + item.getTitle() + "=" + item.isChecked());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean fetchMore(int page) {
        if(!super.fetchMore(page))
            return false;
        if(!this.getClass().equals(BookListFragment.class))
            return true;

        RequestParams params = new ApiClient.PagedParams(page, (BookHandler)responseHandler);
        params.put("uid", owner.id());
        if(!primary.isAllBooks())
            params.put("Cat", primary.id());
        if(specialTracker.isChecked(R.id.filter_wishlist))
            params.put("BookIsWish", specialTracker.isChecked(R.id.filter_wishlist) ? 1 : 0);
        if(specialTracker.isChecked(R.id.filter_favourite))
            params.put("Favourite", "1");
        if(statusTracker.isChecked(R.id.filter_read))
            params.put("BookStatus", "read");
        else if(statusTracker.isChecked(R.id.filter_planning))
            params.put("BookStatus", "planning");
        else if(statusTracker.isChecked(R.id.filter_reading))
            params.put("BookStatus", "currently");
        //for(String s : getArguments().wrapBundle("params").keySet())
            //params.put(s, getArguments().wrapBundle("params").get(s));

        //TODO other params?
        ApiClient.get(params, responseHandler);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(position >= adapter.getCount())
            return;

        Book book = adapter.getItem(position);
        Bundle b = new Bundle();
        int imgHeight = ((ImageView)view.findViewById(R.id.image_view)).getDrawable().getIntrinsicHeight();
        b.putInt("imgHeight", imgHeight);

        book.wrapInBundle(b);

        BookFragment bookFragment = new BookFragment();
        bookFragment.setArguments(b);
        getMainActivity().loadSideFragment(bookFragment);
    }

    protected class BookHandler extends LoadingResponseHandler {

        public BookHandler(FragmentBase f) {
            super(f);
        }

        public BookHandler(LoadingViewInterface lvm) {
            super(lvm);
        }

        @Override
        protected String urlPath() {
            return "book/GetUserBooks";
        }

        @Override
        protected String countFieldName() {
            return "book_count";
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);
            Log.d("OBL:book.", "Success " + response.length());
            if(getActivity() == null)
                return;

            if(noMoreAfterLastTime()) {
                if(adapter.getCount()==0)
                    loadingManager.empty();
                return;
            }
            try {
                if (response.getInt("status") != 0 || statusCode >= 400)
                    throw new JSONException(response.getString("message"));
                JSONArray books = response.getJSONArray("books");
                for (int i = 0; i < books.length(); i++) {
                    Book b = new Book(books.getJSONObject(i));
                    Log.d("OBL:book", "Book: " + b.getS("title"));
                    adapter.add(b);
                }
            } catch (JSONException e) {
                Log.e("OBL:Book!", "Failed cause " + e.getMessage());
                e.printStackTrace();
                this.loadingManager.error(e);
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
            super.onFailure(statusCode, headers, error, responseBody);
            Log.e("OBL:Cat", "Failed cause " + error.getMessage());
        }

        @Override
        public int perScreen() {
            if(gridView.getVisibility()==View.VISIBLE) {
                return super.perScreen(listView.getLastVisiblePosition() - listView.getFirstVisiblePosition() + 1);
            }
            float IMG_SIZE = getResources().getDimension(R.dimen.list_book_size);
            int numberOfRows = (int) Math.ceil(gridView.getHeight() / IMG_SIZE);
            int numberPerRow = (int) Math.floor(gridView.getWidth() / IMG_SIZE);
            Log.d("OBL:fetchMore", numberOfRows + " * " + numberPerRow);
            return super.perScreen(numberOfRows * numberPerRow);
        }
    }


    abstract class BookListAdapter extends AdapterBase<Book> {
        int layoutId;
        SimpleImageLoadingListener imageLoadListener = null;

        public BookListAdapter(Context context, int i, ArrayList<Book> objects, int resId) {
            super(context, i, objects);
            layoutId = resId;
        }

        @Override
        public boolean isEmpty() { return false; }

        @Override
        public View getView(final int position, View row, final ViewGroup parent) {
            if (row==null || row.findViewById(R.id.title)==null) { //FIXME: cause bug in HeaderFooterGridView, apparently
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(layoutId, parent, false);
            }
            TextView title = ((TextView) row.findViewById(R.id.title));
            ImageView cover = ((ImageView) row.findViewById(R.id.image_view));

            if(position >= super.getCount()) {
                title.setText("");
                cover.setImageDrawable(null);
                return row;
            }

            title.setText(getItem(position).getS("title"));
            title.setVisibility(View.VISIBLE);

            Drawable loading = getResources().getDrawable(R.drawable.book2_np3698),
                    empty = getResources().getDrawable(R.drawable.book2_np3698),
                    fail = getResources().getDrawable(R.drawable.cover_fail_np347201);
//                loading.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
//            empty.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
//            fail.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

            MainActivity.displayImage(
                    getItem(position).getS("cover"),
                    cover,
                    loading,
                    empty,
                    fail,
                    imageLoadListener
            );
            final View finalRow = row;
            row.findViewById(R.id.cardView).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gridView.getOnItemClickListener().onItemClick((AdapterView<?>) parent, finalRow, position, finalRow.getId());
                }
            });
            return row;
        }
    }

    class CoverAdapter extends BookListAdapter {

        public CoverAdapter(Context context, ArrayList<Book> books) {
            super(context, 0, books, R.layout.list_book_cover);
            imageLoadListener = new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    super.onLoadingComplete(imageUri, view, loadedImage);
                    TextView title = (TextView) ((ViewGroup)view.getParent().getParent()).findViewById(R.id.title);
                    title.setVisibility(
                            imageUri.endsWith("upload/books/book.jpg") ? View.VISIBLE : View.GONE
                    );
                }
            };
        }

        @Override
        public int getCount() {
            // fill in any extras with a blank
            int n = super.getCount(), x = gridView.getNumColumns();
            return (n+x-1) / x * x;
        }

        @Override
        public View getView(int position, View row, final ViewGroup parent) {
            row = super.getView(position, row, parent);
            row.findViewById(android.R.id.widget_frame).setLayoutParams(new RelativeLayout.LayoutParams(gridView.getColumnWidth(), gridView.getColumnWidth()));

            boolean showBg = SettingsManager.getBool(getActivity(), "shelf_background", R.bool.default_shelf_background);
            row.findViewById(R.id.background).setVisibility(showBg ? View.VISIBLE : View.GONE);
            return row;
        }
    }

    class DetailsAdapter extends BookListAdapter {

        public DetailsAdapter(Context context, ArrayList<Book> objects) {
            super(context, 0, objects, R.layout.list_book_details);
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {
            row = super.getView(position, row, parent);
            TextView author = ((TextView) row.findViewById(R.id.author));
            author.setText(getItem(position).getS("author"));

            ImageView cover = ((ImageView) row.findViewById(R.id.image_view));
            int IMG_SIZE = getResources().getDimensionPixelSize(R.dimen.list_book_size);
            cover.setLayoutParams(new RelativeLayout.LayoutParams(IMG_SIZE / 2, IMG_SIZE / 2));

            return row;
        }
    }

    public static class CheckTracker { //FIXME: find an alternative to this class
        HashMap<Integer, Boolean> group = new HashMap<>();

        public boolean has(int id) {
            return group.containsKey(id);
        }

        public void add(Integer...ids) {
            for(int id : ids)
                group.put(id, false);
        }

        public void check(int id, boolean stat) {
            group.put(id, stat);
        }

        public boolean isChecked(int id) {
            return group.get(id);
        }

        public void checkEx(int id) {
            for(Integer i : group.keySet())
                group.put(i, false);
            Log.d("OBL:checkEx", id + " True");
            group.put(id, true);
        }

        public ArrayList<Integer> getChecked() {
            ArrayList<Integer> what = new ArrayList<>();
            for(Integer i : group.keySet())
                if(group.get(i))
                    what.add(i);
            return what;
        }
    }
}
