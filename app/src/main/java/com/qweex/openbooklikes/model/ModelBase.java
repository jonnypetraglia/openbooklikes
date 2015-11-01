package com.qweex.openbooklikes.model;


import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

abstract public class ModelBase implements Parcelable {
//    private final static String[]
//            ID_FIELDS = new String[] {},
//            STRING_FIELDS = new String[] {},
//            INT_FIELDS = new String[] {};

    protected final String DEFAULT_DOMAIN_SCHEME = "http";

    protected Bundle bundle = new Bundle();

    abstract protected String[] idFields();

    abstract protected String[] stringFields();

    abstract protected String[] intFields();

    public String getS(String f) {
        if(!arrayContains(stringFields(), f) &&
                !(f.endsWith("_id") && arrayContains(idFields(), f.substring(0, f.length() - "_id".length())))
                )
            throw new RuntimeException("Field is not valid for string: " + f);
        return bundle.getString(f);
    }
    public int getI(String f) {
        if(!arrayContains(intFields(), f))
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
        if(b.getString("id")==null)
            throw new RuntimeException("Error Creating " + apiName() + ": id not supplied");
        bundle.putString("id", b.getString("id"));

        for(String s : idFields())
            bundle.putString(s + "_id", b.getString(s + "_id"));

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
            bundle.putString(s + "_id", getJSONString(json, "id_" + s));

        // e.g. username = usr_username
        for(String s : stringFields())
            bundle.putString(s, unHTML(getJSONString(json, apiPrefix() + "_" + s)));

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
    static public String unHTML(String str) {
        if (str == null || str.trim().length() == 0)
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

    static protected boolean arrayContains(String[] array, String s) {
        return Arrays.asList(array).contains(s);
    }

    static private String getJSONString(JSONObject j, String s) throws JSONException {
        return j.isNull(s) ? null : j.getString(s);
    }



    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeBundle(bundle);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public ModelBase(Parcel p) {
        this(p.readBundle());
    }
}
