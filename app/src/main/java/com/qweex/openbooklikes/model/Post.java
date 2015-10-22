package com.qweex.openbooklikes.model;


import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class Post extends ModelBase {
    final static String[]
            ID_FIELDS = new String[] {"user"},
            STRING_FIELDS = new String[] {"type", "title", "url", "desc", "spcial", "source",
            "like_count", "reblog_count", "is_review", "tag", "rating", "date"}, // These should be better data types
            NO_PREFIX_STRING_FIELDS = new String[] {"photo_url", "photo_caption"},   // These should be better data types
            INT_FIELDS = new String[] {};

    @Override
    protected String[] idFields() {
        return ID_FIELDS;
    }

    @Override
    protected String[] stringFields() {
        return STRING_FIELDS;
    }

    @Override
    protected String[] intFields() {
        return INT_FIELDS;
    }

    @Override
    public String apiPrefix() {
        return "post";
    }

    @Override
    public String apiName() {
        return "post";
    }

    @Override
    public String apiNamePlural() {
        return "posts";
    }

    public Post(JSONObject json) throws JSONException {
        super(json);
        for(String s : NO_PREFIX_STRING_FIELDS)
            if(json.has(s))
                bundle.putString(s, json.getString(s));
    }

    public Post(Bundle b) {
        super(b);
    }


    /*
    public boolean is_review; // input is String, "0" or "1"
    public String[] tags; // input is 'tag', space separated
    public float rating; // input is "X.Y"
    public Date date; // input is ??? String?
    */

    @Override
    public String getS(String f) {
        if(Arrays.asList(NO_PREFIX_STRING_FIELDS).contains(f))
            return bundle.getString(f);
        if(f.equals("special"))
            f = "spcial";
        return super.getS(f);
    }
}
