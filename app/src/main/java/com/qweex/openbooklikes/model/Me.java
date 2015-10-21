package com.qweex.openbooklikes.model;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.qweex.openbooklikes.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;


public class Me extends User {
    public static final String USER_DATA_PREFS = "UserData";
    public static final int USER_DATA_MODE = Activity.MODE_PRIVATE;

    public String token = null, email; // Only applicable for Me

    public Me(JSONObject json, Activity a) throws JSONException {
        this(json);
        save(a);
    }

    public Me(JSONObject json) throws JSONException {
        super(json);
        token = json.getString("usr_token");
        email = json.getString("usr_email");
    }

    public static Me fromPrefs(Activity a) throws JSONException {
        SharedPreferences prefs = a.getSharedPreferences(Me.USER_DATA_PREFS, Me.USER_DATA_MODE);
        if(prefs.getString("usr_token", null)==null || prefs.getString("id_user", null)==null)
            return null;

        JSONObject fakeson = new JSONObject();
        fakeson.put("id_user", prefs.getString("id_user", null));
        String[] fields = new String[] {"token", "email",
                "username", "domain", "photo", "blog_title", "blog_desc", "following_count", "followed_count"};
        for(String f : fields)
            fakeson.put("usr_" + f, prefs.getString("usr_" + f, null));
        fakeson.put("usr_book_count", prefs.getInt("usr_book_count", -1));
        return new Me(fakeson);
    }

    @Override
    public Bundle toBundle() {
        Bundle b = super.toBundle();
        b.putString("token", token);
        b.putString("email", email);
        return b;
    }

    private void save(Activity a) {
        SharedPreferences.Editor prefs = a.getSharedPreferences(USER_DATA_PREFS, USER_DATA_MODE).edit();
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
        Log.d("OBL:saveAsMe", token);
        prefs.apply();
    }
}
