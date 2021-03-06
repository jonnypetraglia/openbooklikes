package com.qweex.openbooklikes.model;


import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

public class Username extends ModelBase {
    private final static String[]
            STRING_FIELDS = new String[] {"username"};

    public Username(Bundle b) {
        super(b);
    }

    public Username(JSONObject json) throws JSONException {
        super(json);
    }

    @Override
    protected String[] idFields() {
        return new String[0];
    }

    @Override
    protected String[] stringFields() {
        return STRING_FIELDS;
    }

    @Override
    protected String[] intFields() {
        return new String[0];
    }


    @Override
    public String apiPrefix() {
        return "usr";
    }

    @Override
    public String apiName() {
        return "user";
    }

    @Override
    public String apiNamePlural() {
        return "users";
    }



    public static Username create(String username) {
        Bundle usr = new Bundle();
        usr.putString("username", username);
        usr.putString("id", "");
        Bundle wrap = new Bundle();
        wrap.putBundle("user", usr);
        return new Username(wrap);
    }
}
