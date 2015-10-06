package com.qweex.openbooklikes;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.qweex.openbooklikes.model.Book;

import java.util.ArrayList;

abstract class AdapterBase<T> extends ArrayAdapter<T> {
    ArrayList<T> data;

    public AdapterBase(Context context, int i, ArrayList<T> objects) {
        super(context, i, objects);
        data = objects;
    }

    abstract public int perScreen();

    public ArrayList<T> getData() {
        return this.data;
    }
}
