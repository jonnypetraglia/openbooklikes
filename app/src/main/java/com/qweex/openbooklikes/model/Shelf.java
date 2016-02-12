package com.qweex.openbooklikes.model;


import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


public class Shelf extends BookListPartial implements Linkable {
    private final static String[]
            ID_FIELDS = new String[] {"user"},
            STRING_FIELDS = new String[] {"name"};

    User owner;

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

    public Shelf(JSONObject json, User o) throws JSONException {
        super(json);
        owner = o;
        if(!o.id().equals(getS("user_id")))
            throw new RuntimeException("User id does not match from owner's id:" + o.id() + " vs " + getS("user_id"));
    }

    public Shelf(Bundle b, User o) {
        super(b);
        owner = o;
        Log.d("?", b.getString("user_id")+"!");
        if(!o.id().equals(getS("user_id")))
            throw new RuntimeException("User id does not match from owner's id:" + o.id() + " vs " + getS("user_id"));
    }


    static public Shelf allBooksOfUser(User owner) {
        Bundle b = new Bundle();
        b.putString("id", NO_SHELF_ID);
        b.putString("user_id", owner.id());
        b.putString("name", "All books");
        b.putInt("book_count", owner.getI("book_count"));
        Bundle w = new Bundle();
        w.putBundle("category", b);
        return new Shelf(w, owner);
    }

    @Override
    public Uri link() {
        Uri.Builder builder = owner.link().buildUpon()
                .appendPath("shelf");
        if(!isAllBooks())
            builder.appendPath(id());
        return builder.build();
    }
}
