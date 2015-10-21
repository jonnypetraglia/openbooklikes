package com.qweex.openbooklikes.model;


import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

public class UserPartial extends ModelBase {

    @Override
    public String modelName() { return "user"; }

    public String username, domain, photo; //Returned by GetUserFollowers

    public UserPartial(JSONObject json) throws JSONException {
        super(json);
        username = json.getString("usr_username");
        domain = json.getString("usr_domain");
        photo = json.getString("usr_photo"); //url
    }

    public UserPartial(Bundle b) {
        super(b);
        b = b.getBundle(modelName());
        username = b.getString("username");
        domain = b.getString("domain");
        photo = b.getString("photo");
    }

    @Override
    public Bundle toBundle() {
        Bundle b = super.asBundle();
        b.putString("username", username);
        b.putString("domain", domain);
        b.putString("photo", photo);
        return b;
    }

    protected static class Void {}
}
