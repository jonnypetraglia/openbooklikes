package com.qweex.openbooklikes.model;

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;


abstract public class BookListPartial extends ModelBase {
    private final static String[]
            INT_FIELDS = new String[] {"book_count"};

    public abstract String title();

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
}
