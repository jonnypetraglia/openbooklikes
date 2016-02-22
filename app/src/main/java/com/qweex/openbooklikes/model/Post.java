package com.qweex.openbooklikes.model;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Post extends ModelBase implements Shareable {
    final static String[]
            ID_FIELDS = new String[] {"user"},
            STRING_FIELDS = new String[] {"type", "title", "url", "desc", "spcial", "source",
            "like_count", "reblog_count", "is_review", "tag", "rating", "date"}, // These should be better data types
            ARRAY_FIELDS = new String[] {"photos"}, // {photo_url, photo_caption}
            INT_FIELDS = new String[] {};

    UserPartial owner;

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

    public Post(JSONObject json, UserPartial o) throws JSONException {
        super(json);
        String key;
        for(String s : ARRAY_FIELDS) {
            if(json.has(key = apiPrefix() + "_" + s))
                bundle.putString(s, json.isNull(key) ? null : json.getJSONArray(key).toString());
        }
        owner = o;
        Log.d("New Post", bundle.toString().replace("\n","\\n"));
        if(!o.id().equals(getS("user_id")))
            throw new RuntimeException("User id does not match from owner's id:" + o.id() + " vs " + getS("user_id"));
    }

    public Post(Bundle b, UserPartial o) {
        super(b);
        b = b.getBundle(apiName());
        for(String s : ARRAY_FIELDS) {
            bundle.putString(s, b.getString(s));
        }
        owner = o;
        if(!o.id().equals(getS("user_id")))
            throw new RuntimeException("User id does not match from owner's id:" + o.id() + " vs " + getS("user_id"));
    }

    /*
    public boolean is_review; // input is String, "0" or "1"
    public String[] tags; // input is 'tag', space separated
    public float rating; // input is "X.Y"
    public Date date; // input is ??? String?
    */

    @Override
    public String getS(String f) {
        if(f.equals("special"))
            f = "spcial";
        return super.getS(f);
    }

    public JSONArray getA(String f) {
        if(!Arrays.asList(ARRAY_FIELDS).contains(f))
            throw new RuntimeException("Field is not valid for array: " + f);
        if(bundle.containsKey(f))
            try {
                return new JSONArray(bundle.getString(f));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        return null;
    }

    @Override
    public Uri link() {
        if(getS("url")!=null)
            return Uri.parse(getS("url")); //FIXME: should this get appended or what? what is its form?
        return owner.link().buildUpon()
                .appendPath("post")
                .appendPath(id())
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
