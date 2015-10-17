package com.qweex.openbooklikes.model;


import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;


public class Shelf extends ModelBase {
    public String id, user_id;
    public String name;
    public int book_count;

    public Shelf(JSONObject data) throws JSONException {
        id = data.getString("id_category");
        user_id = data.getString("id_user");
        name = unHTML(data.getString("category_name"));
        book_count = data.getInt("category_book_count");
    }

    @Override
    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putString("id", id);
        b.putString("user_id", user_id);
        b.putString("name", name);
        b.putInt("book_count", book_count);
        return b;
    }
}
