package com.qweex.openbooklikes;

import android.content.Context;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
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

    static {
        statusTracker = new BookListFragment.CheckTracker();
        statusTracker.add(R.id.filter_all, R.id.filter_read, R.id.filter_planning, R.id.filter_currently);
        statusTracker.checkEx(R.id.filter_all); //TODO: Settings

        specialTracker = new BookListFragment.CheckTracker();
        specialTracker.add(R.id.filter_favourite, R.id.filter_wishlist, R.id.filter_reviewed, R.id.filter_private);
        //TODO: Settings
    }

    @Override
    String getTitle() {
        return primary.title();
    }

    @Override
    public void setArguments(Bundle a) {
        primary = (BookList) new Shelf(a);
        Log.d("OBL:setArgs", "shelf.id=" + primary.id());
        owner = new User(a);
        super.setArguments(a);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(primary!=null && savedInstanceState==null)
            listView.post(refreshData);
    }

    Runnable refreshData = new Runnable() {
        @Override
        public void run() {
            adapter.clear();
            showLoading();
            fetchMore(0);
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        adapter = new CoverAdapter(getActivity(), new ArrayList<Book>());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_list, container, false);

        gridView = (HeaderFooterGridView) view.findViewById(R.id.grid_view);
        gridView.setColumnWidth((int) getResources().getDimension(R.dimen.list_book_size));
        gridView.setOnScrollListener(scrollMuch);
        gridView.setOnItemClickListener(this);

        listView = (ListView) view.findViewById(R.id.list_view);
        listView.setOnScrollListener(scrollMuch);
        listView.setOnItemClickListener(this);

        return super.createProgressView(inflater, container, view);
    }

    public void changeWidget(AbsListView choice) {
        if(choice==listView) {
            adapter = new DetailsAdapter(getActivity(), adapter!=null ? adapter.getData() : new ArrayList<Book>()); //FIXME: This is ugly
            gridView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            listView.setAdapter(adapter);
        } else if(choice==gridView) {
            adapter = new CoverAdapter(getActivity(), adapter!=null ? adapter.getData() : new ArrayList<Book>()); //FIXME: This is also ugly
            listView.setVisibility(View.GONE);
            gridView.setVisibility(View.VISIBLE);
            gridView.setAdapter(adapter);
        } else
            throw new RuntimeException("Tried to change to an unknown widget");
        moveLoadingViews();
    }

    @Override
    protected void moveLoadingViews() {
        ViewGroup p = (ViewGroup)progressView.getParent();
        if(p!=listView && p!=gridView) {
            moveLoadingViews();
            return;
        }
        if(gridView.getVisibility()!=View.GONE && p==listView) {
            listView.removeFooterView(progressView);
            listView.removeFooterView(progressText);
            gridView.addFooterView(progressView);
            gridView.addFooterView(progressText);
        } else if(listView.getVisibility()!=View.GONE && p==gridView) {
            gridView.removeFooterView(progressView);
            gridView.removeFooterView(progressText);
            listView.addFooterView(progressView);
            listView.addFooterView(progressText);
        } else
            throw new RuntimeException("Tried to move views but neither adapterview is visible");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_shelf, menu);
        getActivity().onCreateOptionsMenu(menu);
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
        changeWidget(gridView); //TODO: Settings
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.d("OBL", "OptionSelected");

        if(id==R.id.change_view) {
            if(gridView.getVisibility()!=View.GONE) {
                item.setTitle(R.string.option_view_list);
                changeWidget(listView);
            } else {
                item.setTitle(R.string.option_view_grid);
                changeWidget(gridView);
            }
            return true;
        }

        if(statusTracker.has(id)) {
            item.setChecked(true);
            statusTracker.checkEx(id);
            gridView.post(refreshData);
            Log.d("OBL", "optionselected? " + item.getTitle() + "=" + item.isChecked());
            return true;
        }
        if(specialTracker.has(id)) {
            item.setChecked(!item.isChecked());
            specialTracker.check(id, item.isChecked());
            gridView.post(refreshData);
            Log.d("OBL", "optionselected? " + item.getTitle() + "=" + item.isChecked());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean fetchMore(int page) {
        if(!super.fetchMore(page))
            return false;
        RequestParams params = new ApiClient.PagedParams(page, adapter);
        params.put("uid", owner.id());
        if(!primary.id().equals("-1"))
            params.put("Cat", primary.id());
        if(specialTracker.isChecked(R.id.filter_wishlist))
            params.put("BookIsWish", specialTracker.isChecked(R.id.filter_wishlist) ? 1 : 0);
        if(specialTracker.isChecked(R.id.filter_favourite))
            params.put("Favourite", "1");
        if(statusTracker.isChecked(R.id.filter_read))
            params.put("BookStatus", "read");
        else if(statusTracker.isChecked(R.id.filter_planning))
            params.put("BookStatus", "planning");
        else if(statusTracker.isChecked(R.id.filter_currently))
            params.put("BookStatus", "currently");
        //for(String s : getArguments().wrapBundle("params").keySet())
            //params.put(s, getArguments().wrapBundle("params").get(s));

        //TODO other params?
        ApiClient.get(params, booksHandler);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Book book = adapter.getItem(position);

        Log.d("OBL", "book is " + book.getS("cover"));

        Bundle b = new Bundle();
        int imgHeight = ((ImageView)view.findViewById(R.id.image_view)).getDrawable().getIntrinsicHeight();
        b.putInt("imgHeight", imgHeight);

        book.wrapInBundle(b);

        BookFragment bookFragment = new BookFragment();
        bookFragment.setArguments(b);
        getMainActivity().loadSideFragment(bookFragment);
    }

    class CoverAdapter extends AdapterBase<Book> {

        public CoverAdapter(Context context, ArrayList<Book> books) {
            super(context, 0, books);
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {
            if(row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.list_book_cover, parent, false);
            }
            TextView title = ((TextView) row.findViewById(R.id.title));
            title.setText(getItem(position).getS("title"));
            title.setVisibility(View.GONE);

            ImageView cover = ((ImageView) row.findViewById(R.id.image_view));
            cover.setLayoutParams(new LinearLayout.LayoutParams(gridView.getColumnWidth(), gridView.getColumnWidth()));
            MainActivity.imageLoader.displayImage(getItem(position).getS("cover"), cover);

            return row;
        }

        @Override
        public int perScreen() {
            float IMG_SIZE = getResources().getDimension(R.dimen.list_book_size);
            int numberOfRows = (int) Math.ceil(gridView.getHeight() / IMG_SIZE);
            int numberPerRow = (int) Math.floor(gridView.getWidth() / IMG_SIZE);
            Log.d("OBL:fetchMore", numberOfRows + " * " + numberPerRow);
            return super.perScreen(numberOfRows * numberPerRow);
        }

        @Override
        public boolean noMore() {
            return getCount() == primary.getI("book_count") || booksHandler.wasLastFetchNull();
        }
    }

    BookHandler booksHandler = new BookHandler();
    protected class BookHandler extends LoadingResponseHandler {

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

            try {
                if (response.getInt("status") != 0 || statusCode >= 400)
                    throw new JSONException(response.getString("message"));
                if(response.getInt(countFieldName())==0) {
                    showEmpty();
                    return;
                }
                JSONArray books = response.getJSONArray("books");
                for(int i=0; i<books.length(); i++) {
                    Book b = new Book(books.getJSONObject(i));
                    Log.d("OBL:book", "Book: " + b.getS("title"));
                    adapter.add(b);
                }
            } catch (JSONException e) {
                Log.e("OBL:Book!", "Failed cause " + e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
            Log.e("OBL:Cat", "Failed cause " + error.getMessage());
        }
    };


    class DetailsAdapter extends AdapterBase<Book> {

        public DetailsAdapter(Context context, ArrayList<Book> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.list_book_details, parent, false);
            }
            TextView title = ((TextView) row.findViewById(R.id.title));
            title.setText(getItem(position).getS("title"));

            TextView author = ((TextView) row.findViewById(R.id.author));
            author.setText(getItem(position).getS("author"));

            ImageView cover = ((ImageView) row.findViewById(R.id.image_view));
            int IMG_SIZE = getResources().getDimensionPixelSize(R.dimen.list_book_size);
            cover.setLayoutParams(new RelativeLayout.LayoutParams(IMG_SIZE / 2, IMG_SIZE / 2));
            MainActivity.imageLoader.displayImage(getItem(position).getS("cover"), cover);


            Log.d("Parent is", row.getLayoutParams().width + " vs " + ViewGroup.LayoutParams.MATCH_PARENT);


            return row;
        }

        @Override
        public int perScreen() {
            return super.perScreen(listView.getLastVisiblePosition() - listView.getFirstVisiblePosition() + 1);
        }

        @Override
        public boolean noMore() {
            return adapter.getCount() == primary.getI("book_count");
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
