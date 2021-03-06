package com.qweex.openbooklikes;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.qweex.openbooklikes.model.Book;

import java.util.ArrayList;

abstract public class AdapterBase<T> extends ArrayAdapter<T> {
    ArrayList<T> data; //TODO: Find a different way to do this

    public AdapterBase(Context context, int i, ArrayList<T> objects) {
        super(context, i, objects);
        data = objects;
    }

    abstract public int perScreen();

    public ArrayList<T> getData() {
        return this.data;
    }

    abstract public boolean noMore();

    final protected int perScreen(int i) {
        return Math.max(i, getContext().getResources().getInteger(R.integer.min_per_page));
    }
}
