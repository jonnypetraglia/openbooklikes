package com.qweex.openbooklikes.model;


import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


public class Shelf extends BookListPartial {

    @Override
    public String modelName() { return "category"; }

    public String user_id;
    public String name;

    public Shelf(JSONObject json) throws JSONException {
        super(json);
        user_id = json.getString("id_user");
        name = unHTML(json.getString("category_name"));
    }

    @Override
    public String title() {
        return name;
    }

    public Shelf(Bundle b) {
        super(b);
        Log.d("OBL:shelfName", "?" + modelName());
        b = b.getBundle(modelName());
        user_id = b.getString("user_id");
        name = b.getString("name");
    }

    @Override
    public Bundle toBundle() {
        Bundle b = super.toBundle();
        b.putString("user_id", user_id);
        b.putString("name", name);
        return b;
    }
}
