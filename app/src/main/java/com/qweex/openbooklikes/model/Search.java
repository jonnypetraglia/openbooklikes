package com.qweex.openbooklikes.model;


import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

public class Search extends BookListPartial {
    private final static String[]
            STRING_FIELDS = new String[] {"q", "lng"};

    @Override
    protected String[] idFields() {
        return new String[0];
    }

    @Override
    protected String[] stringFields() {
        return STRING_FIELDS;
    }

    public Search(Bundle b) {
        super(b);
    }

    // Search model is not retrieved by API so this is not needed
    private Search(JSONObject json) throws JSONException {
        super(json);
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

    public static Search create(String q) { return create(q, null); }

    public static Search create(String q, String lng) {
        Bundle b = new Bundle();
        b.putString("id", q);
        b.putInt("book_count", -1);
        b.putString("q", q);
        if(lng!=null)
            b.putString("lng", lng);

        Bundle w = new Bundle();
        w.putBundle("book", b);

        return new Search(w);
    }

    @Override
    public String title() {
        return "Search: " + getS("q");
    }
}
