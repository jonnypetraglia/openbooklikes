package com.qweex.openbooklikes.model;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


// Only applicable for Me
public class Me extends User {
    private final static String[]
            STRING_FIELDS = new String[] {"token", "email"};

    public static final String USER_DATA_PREFS = "UserData";
    public static final int USER_DATA_MODE = Activity.MODE_PRIVATE;

    public Me(JSONObject json, Activity a) throws JSONException {
        this(json);
        save(a);
    }

    public Me(JSONObject json) throws JSONException {
        super(json);
    }

    public Me(JSONObject json, Activity a, Me old) throws JSONException {
        this(json);
        for(String s : STRING_FIELDS)
            bundle.putString(s, old.getS(s));
        save(a);
    }

    public String token() {
        return getS("token");
    }

    @Override
    protected String[] stringFields() {
        return mergeArrays(STRING_FIELDS, super.stringFields());
    }

    public static Me fromPrefs(Activity a) throws JSONException {
        SharedPreferences prefs = a.getSharedPreferences(Me.USER_DATA_PREFS, Me.USER_DATA_MODE);
        if(prefs.getString("usr_token", null)==null || prefs.getString("id_user", null)==null)
            return null;

        Bundle b = new Bundle(); //FIXME: Ugly
        b.putBundle("user", new Bundle());
        b.getBundle("user").putString("id", "NA");
        User fakeUser = new User(b);

        JSONObject fakeson = new JSONObject();
        fakeson.put("id_" + fakeUser.apiName(), prefs.getString("id_" + fakeUser.apiName(), null));
        for(String f : fakeUser.idFields())
            fakeson.put("id_" + f, prefs.getString("id_" + f, null));
        for(String f : mergeArrays(STRING_FIELDS, fakeUser.stringFields()))
            fakeson.put("usr_" + f, prefs.getString("usr_" + f, null));
        for(String f : fakeUser.intFields())
            fakeson.put("usr_" + f, prefs.getInt("usr_" + f, -1));

        return new Me(fakeson);
    }

    private void save(Activity a) {
        SharedPreferences.Editor prefs = a.getSharedPreferences(USER_DATA_PREFS, USER_DATA_MODE).edit();

        prefs.putString("id_" + apiName(), id());
        for(String f : idFields())
            prefs.putString(apiPrefix() + "_" + f, getS(f));
        for(String f : stringFields())
            prefs.putString(apiPrefix() + "_" + f, getS(f));
        for(String f : intFields())
            prefs.putInt(apiPrefix() + "_" + f, getI(f));

        Log.d("OBL:saveAsMe", getS("token"));
        prefs.apply();
    }
}
