package com.qweex.openbooklikes.model;


import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

public class Book extends ModelBase {
    private final static String[]
            STRING_FIELDS = new String[] {"title", "author", "cover", "isbn_10", "isbn_13", "format", "publisher", "language",
            "pages", "publish_date"}; // These should be better data types

    @Override
    protected String[] idFields() {
        return new String[0];
    }

    @Override
    protected String[] stringFields() {
        return STRING_FIELDS;
    }

    @Override
    protected String[] intFields() {
        return new String[0];
    }

    @Override
    public String apiPrefix() {
        return "book";
    }

    @Override
    public String apiName() {
        return "book";
    }

    @Override
    public String apiNamePlural() {
        return "books";
    }

    public Book(JSONObject json) throws JSONException {
        super(json);
    }

    public Book(Bundle b) {
        super(b);
    }
}

