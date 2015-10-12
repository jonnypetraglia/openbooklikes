package com.qweex.openbooklikes.model;


import org.json.JSONException;
import org.json.JSONObject;

public class Post extends BaseModel {
    public String id, user_id;

    // Prefix is: post_
    public String type, title, url, desc, special /*spcial*/, source, photo_url, photo_caption;
    // These should be better data types
    public String like_count, reblog_count, is_review, tag, rating, date;

    public Post(JSONObject data) throws JSONException {
        id = data.getString("id_post");
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

    @Override
    public void persist() {

    }
    /*
    public boolean is_review; // input is String, "0" or "1"
    public String[] tags; // input is 'tag', space separated
    public float rating; // input is "X.Y"
    public Date date; // input is ??? String?
    */


}
