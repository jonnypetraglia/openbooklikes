package com.qweex.openbooklikes.model;

import android.os.Bundle;

import com.qweex.openbooklikes.Titleable;

import org.json.JSONException;
import org.json.JSONObject;


abstract public class BookListPartial extends ModelBase implements Titleable {
    private final static String[]
            INT_FIELDS = new String[] {"book_count"};

    public final static String NO_SHELF_ID = "-1";

    @Override
    protected String[] intFields() {
        return INT_FIELDS;
    }

    public BookListPartial(Bundle b) {
        super(b);
    }

    public BookListPartial(JSONObject json) throws JSONException {
        super(json);
    }

    public boolean isAllBooks() {
        return id().equals(NO_SHELF_ID);
    }
}
