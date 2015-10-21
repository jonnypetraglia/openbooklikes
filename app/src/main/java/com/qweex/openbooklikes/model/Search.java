package com.qweex.openbooklikes.model;


import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Search extends BookListPartial {
    final static String MODEL_NAME = "search";
    public String q, lng;

    @Override
    public String modelName() { return MODEL_NAME; }

    public Search(Bundle b) {
        super(b);
        b = b.getBundle(modelName());
        q = b.getString("q");
        lng = b.getString("lng");
    }

    // Search model is not retrieved by API so this is not needed
    private Search(JSONObject json) throws JSONException {
        super(json);
    }

    public static Search create(String q) { return create(q, null); }

    public static Search create(String q, String lng) {
        Bundle b = new Bundle();
        b.putInt("book_count", -1);
        b.putString("q", q);
        if(lng!=null)
            b.putString("lng", lng);
        Bundle w = new Bundle();
        w.putBundle(MODEL_NAME, b);
        return new Search(w);
    }

    @Override
    public String title() {
        return "Search: " + q;
    }

    @Override
    public Bundle toBundle() {
        Bundle b = super.asBundle();
        b.putInt("book_count", book_count);
        return b;
    }
}
