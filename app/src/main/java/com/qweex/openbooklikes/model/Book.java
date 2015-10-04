package com.qweex.openbooklikes.model;


import com.qweex.openbooklikes.AndThen;

import org.json.JSONException;
import org.json.JSONObject;

public class Book extends Base {

    // Prefix is: book_
    public String id, title, author, cover, isbn_10, isbn_13, format, publisher, language;
    // These should be better data types
    public String pages, publish_date;

    public Book(JSONObject json, AndThen then) throws JSONException {
        id = json.getString("id_book");
        title = json.getString("book_title");
        author = json.getString("book_author");
        cover = json.getString("book_cover");
        isbn_10 = json.getString("book_isbn_10");
        isbn_13 = json.getString("book_isbn_13");
        format = json.getString("book_format");
        publisher = json.getString("book_publisher");
        language = json.getString("book_language");
        pages = json.getString("book_pages");
        publish_date = json.getString("book_publish_date");

        downloadImage(cover, then);
    }


    @Override
    public void persist() {

    }
}

