package com.qweex.openbooklikes.model;

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class User extends UserPartial {
    private final static String[]
            STRING_FIELDS = new String[] {"blog_title", "blog_desc", "following_count", "followed_count"},
            INT_FIELDS = new String[] {"book_count"};

    public User(JSONObject json) throws JSONException {
        super(json);
    }

    public User(Bundle b) {
        super(b);
    }

    public String photoSize(int size) {
        return getS("photo").replace("100/100", size + "/" + size);
    }

    @Override
    protected String[] intFields() {
        return mergeArrays(INT_FIELDS, super.intFields());
    }

    @Override
    protected String[] stringFields() {
        return mergeArrays(STRING_FIELDS, super.stringFields());
    }

    public String properName() {
        if(getS("blog_title")==null)
            return getS("username");
        return getS("blog_title");
    }

    public static Username fromData(Bundle b) {
        Username u;
        u = new User(b);
        if(u.getS("following_count")==null) {
            u = new UserPartial(b);
            if(u.id()==null || u.id().length()==0)
                u = new Username(b);
        }
        return u;
    }
}
