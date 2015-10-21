package com.qweex.openbooklikes.model;


import android.os.Bundle;
import android.util.Log;

import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

abstract public class ModelBase {

    public String id;


    abstract public String modelName();

    abstract public Bundle toBundle();

    public boolean equals(ModelBase other) {
        return other!=null && id.equals(other.id);
    }

    final protected Bundle asBundle() {
        Bundle b = new Bundle();
        b.putString("id", id);
        return b;
    }

    public Bundle intoBundle(Bundle b) {
        Log.d("OBL", "!!?" + modelName());
        Bundle c = toBundle();
        c.putString("id", id);
        b.putBundle(modelName(), c);
        return b;
    }

    public ModelBase(Bundle b) {
        id = b.getBundle(modelName()).getString("id");
    }

    public ModelBase(JSONObject json) throws JSONException {
        id = json.getString("id_" + modelName());
    }

    public RequestParams toRequestParams() {
        Bundle b = toBundle();
        RequestParams params = new RequestParams();
        for(String s : b.keySet())
            params.put(s, b.get(s));
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
}
