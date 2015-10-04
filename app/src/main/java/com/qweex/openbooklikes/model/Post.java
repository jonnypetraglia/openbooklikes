package com.qweex.openbooklikes.model;


public class Post {
    public String id, user_id;

    // Prefix is: post_
    public String type, url, desc, special /*spcial*/, source, like_count, reblog_count;
    // These should be other formats
    public String is_review, tag, rating, date;
    /*
    public boolean is_review; // input is String, "0" or "1"
    public String[] tags; // input is 'tag', space separated
    public float rating; // input is "X.Y"
    public Date date; // input is ??? String?
    */


}
