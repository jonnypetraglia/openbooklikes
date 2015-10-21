package com.qweex.openbooklikes.model;


import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

public class Post extends ModelBase {

    @Override
    public String modelName() {
        return "post";
    }

    public String user_id;

    // Prefix is: post_
    public String type, title, url, desc, special /*spcial*/, source, photo_url, photo_caption;
    // These should be better data types
    public String like_count, reblog_count, is_review, tag, rating, date;

    public Post(JSONObject data) throws JSONException {
        super(data);
        user_id = data.getString("id_user");
        type = data.getString("post_type");
        title = unHTML(data.getString("post_title"));
        url = data.getString("post_url");
        desc = unHTML(data.getString("post_desc"));
        special = unHTML(data.getString("post_spcial"));
        source = data.getString("post_source");
        like_count = data.getString("post_like_count");
        reblog_count = data.getString("post_reblog_count");
        is_review = data.getString("post_is_review");
        tag = data.getString("post_tag");
        rating = data.getString("post_rating");
        date = data.getString("post_date");

        if(data.has("photo_url"))
            photo_url = data.getString("photo_url");
        if(data.has("photo_caption"))
            photo_caption = data.getString("photo_caption");
    }

    public Post(Bundle b) {
        super(b);
        b = b.getBundle(modelName());
        user_id = b.getString("user_id");
        type = b.getString("type");
        title = b.getString("title");
        url = b.getString("url");
        desc = b.getString("desc");
        special = b.getString("special");
        source = b.getString("source");
        like_count = b.getString("like_count");
        reblog_count = b.getString("reblog_count");
        is_review = b.getString("is_review");
        tag = b.getString("tag");
        rating = b.getString("rating");
        date = b.getString("date");
        photo_url = b.getString("photo_url");
        photo_caption = b.getString("photo_caption");
    }

    @Override
    public Bundle toBundle() {
        Bundle b = super.asBundle();
        b.putString("user_id", user_id);
        b.putString("type", type);
        b.putString("title", title);
        b.putString("url", url);
        b.putString("desc", desc);
        b.putString("special", special);
        b.putString("source", source);
        b.putString("photo_url", photo_url);
        b.putString("photo_caption", photo_caption);
        b.putString("like_count", like_count);
        b.putString("reblog_count", reblog_count);
        b.putString("is_review", is_review);
        b.putString("tag", tag);
        b.putString("rating", rating);
        b.putString("date", date);
        return b;
    }


    /*
    public boolean is_review; // input is String, "0" or "1"
    public String[] tags; // input is 'tag', space separated
    public float rating; // input is "X.Y"
    public Date date; // input is ??? String?
    */


}
