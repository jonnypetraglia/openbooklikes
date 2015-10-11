package com.qweex.openbooklikes.model;


abstract public class Base {

    abstract public void persist();

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
