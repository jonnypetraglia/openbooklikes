package com.qweex.openbooklikes.notmine;

import android.text.Editable;
import android.text.Html;

import org.xml.sax.XMLReader;

//http://stackoverflow.com/a/9546532/1526210

// Allowed HTML tags: br, p, div, em, b, strong, cite, dfn, i, big, small, font, blockquote, tt, monospace, a, u, sup, sub
// MyTagHandler: ul, ol, li

// Possible others: hr

public class MyTagHandler implements Html.TagHandler {
    boolean first= true;
    String parent=null;
    int index=1;
    @Override
    public void handleTag(boolean opening, String tag, Editable output,
                          XMLReader xmlReader) {

        if(tag.equals("ul")) parent="ul";
        else if(tag.equals("ol")) parent="ol";
        if(tag.equals("li")){
            if(parent.equals("ul")){
                if(first){
                    output.append("\n\t•");
                    first= false;
                }else{
                    first = true;
                }
            }
            else{
                if(first){
                    output.append("\n\t"+index+". ");
                    first= false;
                    index++;
                }else{
                    first = true;
                }
            }
        }
    }
}