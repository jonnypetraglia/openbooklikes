package com.qweex.openbooklikes.model;


import android.net.Uri;
import android.os.Bundle;

import com.qweex.openbooklikes.R;

import org.json.JSONException;
import org.json.JSONObject;

// Returned by GetUserFollowers
public class UserPartial extends ModelBase implements Linkable {
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

    @Override
    public Uri link() {
        Uri uri = Uri.parse(getS("domain"));
        if (uri.getScheme() == null)
            uri = uri.buildUpon().scheme(DEFAULT_DOMAIN_SCHEME).build();
        return uri;
    }

    public Uri linkForFilter(int specialId) {
        Uri.Builder builder = link().buildUpon()
                .appendPath("shelf");
        String s;
        switch(specialId) {
            case R.id.filter_reading:
                s = "currentlyreading";
                break;
            case R.id.filter_planning:
                s = "planningtoread";
                break;
            case R.id.filter_read:
                s = "read";
                break;
            case R.id.filter_reviewed:
                s = "reviewed";
                break;
            case R.id.filter_favourite:
                s = "favorite-books";
                break;
            case R.id.filter_wishlist:
                s = "wishlist";
                break;
            case R.id.filter_private: // Private has no URL, I think
            default:
                throw new RuntimeException("Invalid specialId specified");
        }
        return builder.appendPath(s).build();
    }
}
