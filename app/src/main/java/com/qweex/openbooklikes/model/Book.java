package com.qweex.openbooklikes.model;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

public class Book extends ModelBase implements Shareable {
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

    //FIXME: not sure if this is 100% reliable
    @Override
    public Uri link() {
        return new Uri.Builder()
                .scheme(DEFAULT_DOMAIN_SCHEME)
                .authority("booklikes.com")
                .appendPath("-")
                .appendPath("book," + id())
                .build();
    }

    @Override
    public Intent share() {
        return new Intent(android.content.Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_SUBJECT, getS("title"))
                .putExtra(Intent.EXTRA_TEXT, link().toString());
    }
}

