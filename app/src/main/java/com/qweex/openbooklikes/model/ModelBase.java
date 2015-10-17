package com.qweex.openbooklikes.model;


import android.os.Bundle;

import com.loopj.android.http.RequestParams;

abstract public class ModelBase {

    abstract public Bundle toBundle();

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
