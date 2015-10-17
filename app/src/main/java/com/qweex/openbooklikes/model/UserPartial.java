package com.qweex.openbooklikes.model;


import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

public class UserPartial extends ModelBase {
    public String id, username, domain, photo; //Returned by GetUserFollowers

    protected UserPartial(Void v) {}

    public UserPartial(JSONObject json) throws JSONException {
        id = json.getString("id_user");
        username = json.getString("usr_username");
        domain = json.getString("usr_domain");
        photo = json.getString("usr_photo"); //url
    }

    @Override
    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putString("id", id);
        b.putString("username", username);
        b.putString("domain", domain);
        b.putString("photo", photo);
        return b;
    }

    protected static class Void {}
}
