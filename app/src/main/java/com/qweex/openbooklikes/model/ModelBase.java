package com.qweex.openbooklikes.model;


import android.os.Bundle;
import android.util.Log;

import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

abstract public class ModelBase {
//    private final static String[]
//            ID_FIELDS = new String[] {},
//            STRING_FIELDS = new String[] {},
//            INT_FIELDS = new String[] {};
    protected Bundle bundle = new Bundle();

    abstract protected String[] idFields();

    abstract protected String[] stringFields();

    abstract protected String[] intFields();

    public String getS(String f) {
        if(!Arrays.asList(mergeArrays(stringFields(), idFields())).contains(f))
            throw new RuntimeException("Field is not valid for string: " + f);
        return bundle.getString(f);
    }
    public int getI(String f) {
        if(!Arrays.asList(intFields()).contains(f))
            throw new RuntimeException("Field is not valid for int: " + f);
        return bundle.getInt(f);
    }

    public String id() { return bundle.getString("id"); }

    abstract public String apiPrefix();
    abstract public String apiName();
    abstract public String apiNamePlural();

    public Bundle getBundle() { return bundle; }

    public Bundle wrapInBundle(Bundle b) {
        b.putBundle(apiName(), getBundle());
        return b;
    }

    public boolean equals(ModelBase other) {
        return other!=null &&
                apiPrefix().equals(other.apiPrefix()) &&
                id().equals(other.id());
    }

    public ModelBase(Bundle b) {
        b = b.getBundle(apiName());
        for(String s : b.keySet())
            Log.d("Creating Model", s);
        if(b.getString("id")==null)
            throw new RuntimeException("Error Creating " + apiName() + ": id not supplied");
        bundle.putString("id", b.getString("id"));

        for(String s : idFields())
            bundle.putString(s, b.getString(s));

        for(String s : stringFields())
            bundle.putString(s, b.getString(s));

        for(String i : intFields())
            bundle.putInt(i, b.getInt(i));
    }

    public ModelBase(JSONObject json) throws JSONException {
        if(json.getString("id_" + apiName())==null)
            throw new RuntimeException("Error Creating " + apiName() + ": id not supplied");

        // e.g. id = id_category
        bundle.putString("id", json.getString("id_" + apiName()));

        // e.g. user_id = id_user
        for(String s : idFields())
            bundle.putString(s + "_id", json.getString("id_" + s));

        // e.g. username = usr_username
        for(String s : stringFields())
            bundle.putString(s, unHTML(json.getString(apiPrefix() + "_" + s)));

        for(String i : intFields())
            bundle.putInt(i, json.getInt(apiPrefix() + "_" + i));
    }

    public RequestParams toRequestParams() {
        RequestParams params = new RequestParams();
        for(String s : stringFields())
            params.put(s, bundle.getString(s));
        for(String s : intFields())
            params.put(s, bundle.getInt(s));
        return params;
    }

    // http://www.java2s.com/Code/Java/Servlets/Escapeandunescapestring.htm
    public static String unHTML(String str) {
        if (str == null || str.length() == 0)
            return null;

        return android.text.Html.fromHtml(str)
                .toString()
                .replaceAll("\\r\\n", "\n")
                //.replaceAll("\\n\\n", "\n")
                .trim();
    }


    static protected String[] mergeArrays(String[] ... arrays) {
        ArrayList<String> fields = new ArrayList<String>();
        for(String[] a : arrays)
            fields.addAll(new ArrayList<String>(Arrays.asList(a)));
        String[] x = new String[fields.size()];
        fields.toArray(x);
        return x;
    }
}
