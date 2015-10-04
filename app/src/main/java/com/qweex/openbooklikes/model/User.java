package com.qweex.openbooklikes.model;

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    public String token = null; // Only applicable for Me
    public String id, username, domain, photo; //Returned by GetUserFollowers

    public String email, blog_title, blog_desc, following_count, followed_count;
    public int book_count;

    public User(JSONObject json, SharedPreferences.Editor prefs) throws JSONException {
        id = json.getString("id_user");
        username = json.getString("usr_username");
        domain = json.getString("usr_domain");
        photo = json.getString("usr_photo"); //url

        if(json.has("usr_book_count")) {
            email = json.getString("usr_email");
            blog_title = json.getString("usr_blog_title");
            blog_desc = json.getString("usr_blog_desc");
            following_count = json.getString("usr_following_count");
            followed_count = json.getString("usr_followed_count");
            book_count = json.getInt("usr_book_count");
            if (json.has("usr_token")) {
                token = json.getString("usr_token");
                prefs.putString("id_user", id);
                prefs.putString("usr_username", username);
                prefs.putString("usr_domain", domain);
                prefs.putString("usr_photo", photo);

                prefs.putString("usr_email", email);
                prefs.putString("usr_blog_title", blog_title);
                prefs.putString("usr_blog_desc", blog_desc);
                prefs.putString("usr_following_count", following_count);
                prefs.putString("usr_followed_count", followed_count);
                prefs.putInt("usr_book_count", book_count);

                prefs.putString("usr_token", token);
                Log.d("OBL:User(me)", token);
                prefs.apply();
            }
        }
    }

    public User(SharedPreferences prefs) {
        id = prefs.getString("id_user", null);
        username = prefs.getString("usr_username", null);
        domain = prefs.getString("usr_domain", null);
        photo = prefs.getString("usr_photo", null);
        email = prefs.getString("usr_email", null);
        blog_title = prefs.getString("usr_blog_title", null);
        blog_desc = prefs.getString("usr_blog_desc", null);
        following_count = prefs.getString("usr_following_count", null);
        followed_count = prefs.getString("usr_followed_count", null);
        book_count = prefs.getInt("usr_book_count", -1);
    }
}
