package com.qweex.openbooklikes.model;

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;


abstract public class BookListPartial extends ModelBase {
    public int book_count;

    public abstract String title();

    public BookListPartial(Bundle b) {
        super(b);
        b = b.getBundle(modelName());
        book_count = b.getInt("book_count");
    }

    public BookListPartial(JSONObject json) throws JSONException {
        super(json);
        book_count = json.getInt("category_book_count");
    }

    @Override
    public String modelName() {
        return null;
    }

    @Override
    public Bundle toBundle() {
        Bundle b = super.asBundle();
        b.putInt("book_count", book_count);
        return b;
    }
}
