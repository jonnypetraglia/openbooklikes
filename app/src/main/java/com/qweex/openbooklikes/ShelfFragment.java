package com.qweex.openbooklikes;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.qweex.openbooklikes.model.Book;
import com.qweex.openbooklikes.model.Shelf;
import com.qweex.openbooklikes.notmine.EndlessScrollListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;


public class ShelfFragment extends FragmentBase {
    Shelf shelf;
    GridView gridView;
    ListView listView;
    AdapterBase<Book> adapter;
    static int IMG_SIZE = MainActivity.dpToPx(140), MIN_PER_PAGE = 25;

    static CheckTracker statusTracker, specialTracker;

    static {
        statusTracker = new ShelfFragment.CheckTracker();
        statusTracker.add(R.id.filter_all, R.id.filter_read, R.id.filter_planning, R.id.filter_currently);
        statusTracker.checkEx(R.id.filter_all); //TODO: Settings

        specialTracker = new ShelfFragment.CheckTracker();
        specialTracker.add(R.id.filter_favourite, R.id.filter_wishlist, R.id.filter_reviewed, R.id.filter_private);
        //TODO: Settings
    }

    @Override
    String getTitle() {
        return shelf.name;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shelf, container, false);

        gridView = (GridView) view.findViewById(R.id.gridView);
        gridView.setColumnWidth(IMG_SIZE);
        gridView.setOnScrollListener(scrollMuch);
        gridView.setOnItemClickListener(selectBook);

        listView = (ListView) view.findViewById(R.id.listView);
        gridView.setOnScrollListener(scrollMuch);
        listView.setOnItemClickListener(selectBook);

        changeWidget();
        return super.createProgressView(inflater, container, view);
    }

    EndlessScrollListener scrollMuch = new EndlessScrollListener() {
        @Override
        public boolean onLoadMore(int page, int totalItemsCount) {
            // Triggered only when new data needs to be appended to the list
            // Add whatever code is needed to append new items to your AdapterView
            if (adapter.getCount() == shelf.book_count)
                return false;
            fetchMore(page - 1);
            return true; // ONLY if more data is actually being loaded; false otherwise.
        }
    };

    public void changeWidget() {
        if(adapter instanceof DetailsAdapter) {
            gridView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.GONE);
            gridView.setVisibility(View.VISIBLE);
        }
        listView.setAdapter(adapter);
        gridView.setAdapter(adapter);
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
        menu.findItem(R.id.change_view).setTitle("List view"); //TODO: settings
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.d("OBL", "OptionSelected");

        if(id==R.id.change_view) {
            if(adapter instanceof DetailsAdapter) {
                adapter = new CoverAdapter(getActivity(), adapter.getData());
                item.setTitle("List view");
            } else {
                adapter = new DetailsAdapter(getActivity(), adapter.getData());
                item.setTitle("Grid view");
            }
            changeWidget();
            return true;
        }

        if(statusTracker.has(id)) {
            item.setChecked(true);
            statusTracker.checkEx(id);
            onStart();
            Log.d("OBL", "optionselected? " + item.getTitle() + "=" + item.isChecked());
            return true;
        }
        if(specialTracker.has(id)) {
            item.setChecked(!item.isChecked());
            specialTracker.check(id, item.isChecked());
            onStart();
            Log.d("OBL", "optionselected? " + item.getTitle() + "=" + item.isChecked());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setShelf(MainActivity a, Shelf s) {
        shelf = s;
        ArrayList<Book> bookList = new ArrayList<>();
        Log.d("OBL:Adapter", shelf.name + "() " + bookList);
        adapter = new CoverAdapter(a, bookList);
    }

    public void fetchMore(int page) {
        super.fetchMore(page);
        RequestParams params = new RequestParams();
        params.put("PerPage", Math.min(adapter.perScreen(), MIN_PER_PAGE));
        params.put("Page", page);
        if(getArguments().containsKey("Cat"))
            params.put("Cat", getArguments().get("Cat"));
        if(specialTracker.isChecked(R.id.filter_wishlist))
            params.put("BookIsWish", "1");
        if(specialTracker.isChecked(R.id.filter_favourite))
            params.put("Favourite", "1");
        if(statusTracker.isChecked(R.id.filter_read))
            params.put("BookStatus", "read");
        else if(statusTracker.isChecked(R.id.filter_planning))
            params.put("BookStatus", "planning");
        else if(statusTracker.isChecked(R.id.filter_currently))
            params.put("BookStatus", "currently");
        //for(String s : getArguments().getBundle("params").keySet())
            //params.put(s, getArguments().getBundle("params").get(s));

        //TODO other params
        ApiClient.get("book/GetUserBooks", params, booksHandler);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (shelf == null)
            return;

        adapter.clear();
        gridView.post(new Runnable() {
            @Override
            public void run() {
                fetchMore(0);
            }
        });
    }

    class CoverAdapter extends AdapterBase<Book> {

        public CoverAdapter(Context context, ArrayList<Book> books) {
            super(context, 0, books);
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {
            if(row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.list_shelf_cover, parent, false);
            }
            TextView title = ((TextView) row.findViewById(R.id.title));
            title.setText(getItem(position).title);
            title.setVisibility(View.GONE);

            ImageView cover = ((ImageView) row.findViewById(R.id.image));
            cover.setLayoutParams(new LinearLayout.LayoutParams(gridView.getColumnWidth(), gridView.getColumnWidth()));
            MainActivity.imageLoader.displayImage(getItem(position).cover, cover);

            return row;
        }

        @Override
        public int perScreen() {
            int numberOfRows = (int) Math.ceil(gridView.getHeight() / IMG_SIZE); //140
            int numberPerRow = (int) Math.floor(gridView.getWidth() / IMG_SIZE);  //140
            Log.d("OBL:fetchMore", numberOfRows + " * " + numberPerRow);
            return numberOfRows * numberPerRow;
        }
    }

    class DetailsAdapter extends AdapterBase<Book> {

        public DetailsAdapter(Context context, ArrayList<Book> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.list_shelf_details, parent, false);
            }
            TextView title = ((TextView) row.findViewById(R.id.title));
            title.setText(getItem(position).title);

            TextView author = ((TextView) row.findViewById(R.id.author));
            author.setText(getItem(position).author);

            ImageView cover = ((ImageView) row.findViewById(R.id.image));
            cover.setLayoutParams(new RelativeLayout.LayoutParams(IMG_SIZE / 2, IMG_SIZE / 2));
            MainActivity.imageLoader.displayImage(getItem(position).cover, cover);

            return row;
        }

        @Override
        public int perScreen() {
            return 10; //TODO
        }
    }

    ResponseHandler booksHandler = new ResponseHandler() {

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);
            Log.d("OBL:book.", "Success " + response.length());

            try {
                if (response.getInt("status") != 0 || statusCode >= 400)
                    throw new JSONException(response.getString("message"));
                JSONArray books = response.getJSONArray("books");
                for(int i=0; i<books.length(); i++) {
                    Book b = new Book(books.getJSONObject(i));
                    Log.d("OBL:book", "Book: " + b.title);
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


    public static class CheckTracker {
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

    AdapterView.OnItemClickListener selectBook = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Book b = adapter.getItem(position);

            BookFragment bookFragment = new BookFragment();
            int imgHeight = ((ImageView)view.findViewById(R.id.image)).getDrawable().getIntrinsicHeight();
            bookFragment.setBook(b, imgHeight);

            getMainActivity().loadSideFragment(bookFragment);
        }
    };


}
