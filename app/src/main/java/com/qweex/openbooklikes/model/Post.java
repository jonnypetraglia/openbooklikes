package com.qweex.openbooklikes.model;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class Post extends ModelBase implements Shareable {
    final static String[]
            ID_FIELDS = new String[] {"user"},
            STRING_FIELDS = new String[] {"type", "title", "url", "desc", "spcial", "source",
            "like_count", "reblog_count", "is_review", "tag", "rating", "date"}, // These should be better data types
            NO_PREFIX_STRING_FIELDS = new String[] {"photo_url", "photo_caption"},   // These should be better data types
            INT_FIELDS = new String[] {};

    User owner;

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

    public Post(JSONObject json, User o) throws JSONException {
        super(json);
        for(String s : NO_PREFIX_STRING_FIELDS)
            if(json.has(s))
                bundle.putString(s, json.getString(s));
        owner = o;
        if(!o.id().equals(getS("user_id")))
            throw new RuntimeException("User id does not match from owner's id:" + o.id() + " vs " + getS("user_id"));
    }

    public Post(Bundle b, User o) {
        super(b);
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
        if(Arrays.asList(NO_PREFIX_STRING_FIELDS).contains(f))
            return bundle.getString(f);
        if(f.equals("special"))
            f = "spcial";
        return super.getS(f);
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
