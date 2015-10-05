package com.qweex.openbooklikes.model;


abstract public class Base {

    abstract public void persist();

    // http://www.java2s.com/Code/Java/Servlets/Escapeandunescapestring.htm
    public static String unescapeXML(String str) {
        if (str == null || str.length() == 0)
            return "";

        StringBuffer buf = new StringBuffer();
        int len = str.length();
        for (int i = 0; i < len; ++i) {
            char c = str.charAt(i);
            if (c == '&') {
                int pos = str.indexOf(";", i);
                if (pos == -1) { // Really evil
                    buf.append('&');
                } else if (str.charAt(i + 1) == '#') {
                    int val = Integer.parseInt(str.substring(i + 2, pos), 16);
                    buf.append((char) val);
                    i = pos;
                } else {
                    String substr = str.substring(i, pos + 1);
                    if (substr.equals("&amp;"))
                        buf.append('&');
                    else if (substr.equals("&lt;"))
                        buf.append('<');
                    else if (substr.equals("&gt;"))
                        buf.append('>');
                    else if (substr.equals("&quot;"))
                        buf.append('"');
                    else if (substr.equals("&apos;"))
                        buf.append('\'');
                    else
                        // ????
                        buf.append(substr);
                    i = pos;
                }
            } else {
                buf.append(c);
            }
        }
        return buf.toString();
    }
}
