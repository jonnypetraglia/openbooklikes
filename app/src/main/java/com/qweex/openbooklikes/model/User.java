package com.qweex.openbooklikes.model;

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

public class User extends UserPartial {
    public String blog_title, blog_desc, following_count, followed_count;
    public int book_count;

    public User(JSONObject json) throws JSONException {
        super(json);
        blog_title = unHTML(json.getString("usr_blog_title"));
        blog_desc = unHTML(json.getString("usr_blog_desc"));
        following_count = json.getString("usr_following_count");
        followed_count = json.getString("usr_followed_count");
        book_count = json.getInt("usr_book_count");
    }

    public User(Bundle b) {
        super(b);
        b = b.getBundle(modelName());
        blog_title = b.getString("blog_title");
        blog_desc = b.getString("blog_desc");
        following_count = b.getString("following_count");
        followed_count = b.getString("followed_count");
        book_count = b.getInt("book_count");
    }

    public String photoSize(int size) {
        return photo.replace("100/100", size + "/" + size);
    }


    public String properName() {
        if(blog_title==null)
            return username;
        return blog_title;
    }

    @Override
    public Bundle toBundle() {
        Bundle b = super.toBundle();
        b.putString("properName()", properName());
        b.putString("blog_title", blog_title);
        b.putString("blog_desc", blog_desc);
        b.putString("following_count", following_count);
        b.putString("followed_count", followed_count);
        b.putInt("book_count", book_count);
        return b;
    }
}
