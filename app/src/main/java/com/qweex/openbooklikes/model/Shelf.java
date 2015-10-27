package com.qweex.openbooklikes.model;


import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


public class Shelf extends BookListPartial {
    private final static String[]
            ID_FIELDS = new String[] {"user"},
            STRING_FIELDS = new String[] {"name"};

    @Override
    public String title() {
        return getS("name");
    }

    @Override
    protected String[] idFields() {
        return ID_FIELDS;
    }

    @Override
    protected String[] stringFields() {
        return STRING_FIELDS;
    }

    @Override
    public String apiPrefix() {
        return "category";
    }

    @Override
    public String apiName() {
        return "category";
    }

    @Override
    public String apiNamePlural() {
        return "categories";
    }

    public Shelf(JSONObject json) throws JSONException {
        super(json);
    }

    public Shelf(Bundle b) {
        super(b);
    }


    static public Shelf allBooksOfUser(User owner) {
        Bundle b = new Bundle();
        b.putString("id", "-1");
        b.putString("id_user", owner.id());
        b.putString("name", "All books");
        b.putInt("book_count", owner.getI("book_count"));
        Bundle w = new Bundle();
        w.putBundle("category", b);
        return new Shelf(w);
    }
}
