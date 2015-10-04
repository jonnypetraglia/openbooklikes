package com.qweex.openbooklikes;

import android.content.Context;
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

import com.qweex.openbooklikes.model.Book;
import com.qweex.openbooklikes.model.Shelf;

import java.util.List;


public class ShelfFragment extends Fragment {
    Shelf shelf;
    List<Book> books;
    GridView gridView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shelf, container, false);
        gridView = (GridView) view.findViewById(R.id.gridView);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(shelf!=null)
            ((Toolbar)getActivity().findViewById(R.id.toolbar)).setTitle(shelf.name);
        Log.d("OBL:Adapter", "() " + books);
        if(books!=null)
            gridView.setAdapter(new CoverAdapter(getActivity()));
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    class CoverAdapter extends ArrayAdapter<Book> {

        public CoverAdapter(Context context) {
            super(context, 0, books);
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {
            if(row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.shelf_cover, parent, false);
            }
            TextView title = ((TextView) row.findViewById(R.id.text));
            title.setText(books.get(position).title);
            title.setVisibility(View.GONE);

            ImageView cover = ((ImageView) row.findViewById(R.id.image));
            cover.setImageBitmap(books.get(position).bitmap);

            return row;
        }
    }
    /* TODO
        IMMEDIATE
            - fetch multiple images asyncronously & cache them
              - do it inside a custom ImageView class
              - after it is fetched, cache it to SQL/SharedPreferences/disk
                - map from 'imageUrl' -> 'imageData'
              - check cache when loading
              - https://github.com/nostra13/Android-Universal-Image-Loader
            - All books as default shelf
            - Infinite scroll for shelves
            - Filter by status & special
              - Move out of drawer and to Options?
             - Book screen

        LATER
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
