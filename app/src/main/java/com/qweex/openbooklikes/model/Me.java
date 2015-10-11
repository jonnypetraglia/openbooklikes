package com.qweex.openbooklikes.model;

import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;


public class Me extends User {
    public String token = null, email; // Only applicable for Me

    public Me(JSONObject json) throws JSONException, Exception {
        super(json);
        if(!json.has("usr_token"))
            throw new Exception("tried to initialize Me without a token");
        token = json.getString("usr_token");
        email = json.getString("usr_email");
    }

    public Me(SharedPreferences prefs) {
        id = prefs.getString("id_user", null);
        username = prefs.getString("usr_username", null);
        domain = prefs.getString("usr_domain", null);
        photo = prefs.getString("usr_photo", null);
        blog_title = prefs.getString("usr_blog_title", null);
        blog_desc = prefs.getString("usr_blog_desc", null);
        following_count = prefs.getString("usr_following_count", null);
        followed_count = prefs.getString("usr_followed_count", null);
        book_count = prefs.getInt("usr_book_count", -1);

        email = prefs.getString("usr_email", null);
        token = prefs.getString("usr_token", null);
    }
}