package com.qweex.openbooklikes;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.qweex.openbooklikes.model.Book;
import com.qweex.openbooklikes.model.Shelf;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


public class ShelfFragment extends Fragment {
    Shelf shelf;
    GridView gridView;
    ArrayAdapter<Book> adapter;

    public void setShelf(MainActivity a, Shelf s) {
        shelf = s;
        ArrayList<Book> bookList = new ArrayList<>();
        Log.d("OBL:Adapter", shelf.name + "() " + bookList);
        adapter = new CoverAdapter(a, bookList);
        a.getSupportActionBar().setTitle(shelf.name);
        Log.d("OBL:setShelf", a.toolbar + " - " + a.toolbar.getTitle());
    }

    @Override
    public void setArguments(Bundle b) {
        super.setArguments(b);
        //shelf = MainActivity.shelves.get(b.getInt("shelfIndex"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shelf, container, false);

        gridView = (GridView) view.findViewById(R.id.gridView);
        gridView.setAdapter(adapter);
        gridView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                if (adapter.getCount() == shelf.book_count)
                    return false;
                fetchMore(page - 1);
                return true; // ONLY if more data is actually being loaded; false otherwise.
            }
        });

        return view;
    }

    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public void fetchMore(int page) {
        int heightDp = 140, widthDp = 140;

        int numberOfRows = (int) Math.ceil(gridView.getHeight() / dpToPx(heightDp));
        int numberPerRow = (int) Math.floor(gridView.getWidth() / dpToPx(widthDp));
        Log.d("OBL:fetchMore", numberOfRows + " * " + numberPerRow);

        RequestParams params = new RequestParams();
        params.put("PerPage", numberOfRows * numberPerRow);
        params.put("Page", page);
        if(getArguments().getString("Cat")!=null)
            params.put("Cat", getArguments().getString("Cat"));
        //TODO other params
        ApiClient.get("book/GetUserBooks", params, shelfHandler);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (shelf == null)
            return;

        gridView.post(new Runnable() {
            @Override
            public void run() {
                fetchMore(0);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    class CoverAdapter extends ArrayAdapter<Book> {

        public CoverAdapter(Context context, ArrayList<Book> books) {
            super(context, 0, books);
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {
            if(row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.shelf_cover, parent, false);
            }
            TextView title = ((TextView) row.findViewById(R.id.text));
            title.setText(getItem(position).title);
            title.setVisibility(View.GONE);

            ImageView cover = ((ImageView) row.findViewById(R.id.image));
            MainActivity.imageLoader.displayImage(getItem(position).cover, cover);

            return row;
        }
    }

    JsonHttpResponseHandler shelfHandler = new JsonHttpResponseHandler() {

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
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

    /* TODO
        IMMEDIATE
            - Infinite scroll for shelves
            - Filter by status & special
              - Move out of drawer and to Options?
             - Book screen

        LATER
            - Default shelf is not visually checked
            - create Profile screen & move Logout to it
              - add "Are you sure?"
            - Option to switch between grid & list view (create ListAdapter for latter)
            - add "Friends" to drawer, or "Following/Followers" to Profile?
            - Add "Reading Challenge" to drawer
                - only display if user has an active one
            - cache shelves?

        MUCH LATER
            - Loading screens
            - Make drawer same width as Gmail...somehow
            - setting: custom default shelf/fragment
            - add background image to shelf view
            - better Login screen
            - About screen
            - Ability to search for books on Amazon; IndieBound; share on social media
            - Search books / AddBookToShelf
            - Add shelf

        NOTHER LIFETIME
            - Blog
            - Register
     */
}
