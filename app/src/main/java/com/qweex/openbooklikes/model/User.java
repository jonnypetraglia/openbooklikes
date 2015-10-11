package com.qweex.openbooklikes.model;

import org.json.JSONException;
import org.json.JSONObject;

public class User extends Base {
    public String id, username, domain, photo; //Returned by GetUserFollowers

    public String blog_title, blog_desc, following_count, followed_count;
    public int book_count;

    protected User() {
        // Only used for subclass constructors
    }

    public User(String id, String username) {
        this.id = id;
        this.username = username;
    }

    public User(JSONObject json) throws JSONException {
        id = json.getString("id_user");
        username = json.getString("usr_username");
        domain = json.getString("usr_domain");
        photo = json.getString("usr_photo"); //url

        if(json.has("usr_book_count")) {
            blog_title = unHTML(json.getString("usr_blog_title"));
            blog_desc = unHTML(json.getString("usr_blog_desc"));
            following_count = json.getString("usr_following_count");
            followed_count = json.getString("usr_followed_count");
            book_count = json.getInt("usr_book_count");
        }
    }


    public String niceBlogTitle() {
        return blog_title != null ? blog_title : username;
    }

    @Override
    public void persist() {
        //TODO: SQLite insert
    }
}
