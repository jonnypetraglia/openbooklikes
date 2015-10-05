package com.qweex.openbooklikes.model;


import org.json.JSONException;
import org.json.JSONObject;

public class Book extends Base {

    // Prefix is: book_
    public String id, title, author, cover, isbn_10, isbn_13, format, publisher, language;
    // These should be better data types
    public String pages, publish_date;

    public Book(JSONObject json) throws JSONException {
        id = json.getString("id_book");
        title = unescapeXML(json.getString("book_title"));
        author = unescapeXML(json.getString("book_author"));
        cover = json.getString("book_cover");
        isbn_10 = json.getString("book_isbn_10");
        isbn_13 = json.getString("book_isbn_13");
        format = json.getString("book_format"); //!!! weird number enum
        publisher = unescapeXML(json.getString("book_publisher"));
        language = unescapeXML(json.getString("book_language"));
        pages = json.getString("book_pages");
        publish_date = json.getString("book_publish_date");
    }


    @Override
    public void persist() {

    }
}

