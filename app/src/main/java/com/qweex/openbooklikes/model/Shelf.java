package com.qweex.openbooklikes.model;


import org.json.JSONException;
import org.json.JSONObject;

public class Shelf {
    public String id, user_id;
    public String name;
    public int book_count;

    public Shelf(JSONObject data) throws JSONException {
        id = data.getString("id_category");
        user_id = data.getString("id_user");
        name = data.getString("name");
        book_count = data.getInt("book_count");
    }
}
