package com.qweex.openbooklikes.model;


import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

public class Book extends ModelBase {

    @Override
    public String modelName() { return "book"; }

    // Prefix is: book_
    public String title, author, cover, isbn_10, isbn_13, format, publisher, language;
    // These should be better data types
    public String pages, publish_date;

    public Book(JSONObject json) throws JSONException {
        super(json);
        title = unHTML(json.getString("book_title"));
        author = unHTML(json.getString("book_author"));
        cover = json.getString("book_cover");
        isbn_10 = json.getString("book_isbn_10");
        isbn_13 = json.getString("book_isbn_13");
        format = json.getString("book_format"); //!!! weird number enum
        publisher = unHTML(json.getString("book_publisher"));
        language = unHTML(json.getString("book_language"));
        pages = json.getString("book_pages");
        publish_date = json.getString("book_publish_date");
    }

    public Book(Bundle b) {
        super(b);
        b = b.getBundle(modelName());
        title = b.getString("title");
        author = b.getString("author");
        cover = b.getString("cover");
        isbn_10 = b.getString("isbn_10");
        isbn_13 = b.getString("isbn_13");
        format = b.getString("format");
        publisher = b.getString("publisher");
        language = b.getString("language");
        pages = b.getString("pages");
        publish_date = b.getString("publish_date");
    }


    @Override
    public Bundle toBundle() {
        Bundle b = super.asBundle();
        b.putString("title", title);
        b.putString("author", author);
        b.putString("cover", cover);
        b.putString("isbn_10", isbn_10);
        b.putString("isbn_13", isbn_13);
        b.putString("format", format);
        b.putString("publisher", publisher);
        b.putString("language", language);
        b.putString("pages", pages);
        b.putString("publish_date", publish_date);
        return b;
    }
}

