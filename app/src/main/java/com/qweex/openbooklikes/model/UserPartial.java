package com.qweex.openbooklikes.model;


import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

// Returned by GetUserFollowers
public class UserPartial extends ModelBase {
    private final static String[]
            STRING_FIELDS = new String[] {"username", "domain", "photo"};

    public UserPartial(Bundle b) {
        super(b);
    }

    public UserPartial(JSONObject json) throws JSONException {
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
}
