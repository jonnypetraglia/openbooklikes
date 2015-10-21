package com.qweex.openbooklikes;

import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.qweex.openbooklikes.model.Shelf;
import com.qweex.openbooklikes.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class ShelvesHandler extends ApiClient.ApiResponseHandler {
    protected ArrayList<Shelf> shelves;
    User owner;

    @Override
    protected String urlPath() {
        return "user/GetUserCategories";
    }

    @Override
    protected String countFieldName() {
        return null; // No count
    }


    public ShelvesHandler(ArrayList<Shelf> s, User o) {
        shelves = s;
        owner = o;
    }

    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
        super.onSuccess(statusCode, headers, response);
        if(wasLastFetchNull())
            return;
        Log.d("OBL:cat.", "Success " + response.length());

        try {
            if (response.getInt("status") != 0 || statusCode >= 400)
                throw new JSONException(response.getString("message"));
            JSONArray categories = response.getJSONArray("categories");

            JSONObject allBooks = new JSONObject();
            allBooks.put("id_category", "-1");
            allBooks.put("id_user", owner.id);
            allBooks.put("category_name", "All books");
            allBooks.put("category_book_count", owner.book_count);
            Shelf s = new Shelf(allBooks);
            shelves.add(s);

            for (int i = 0; i < categories.length(); i++) {
                s = new Shelf(categories.getJSONObject(i));
                shelves.add(s);
                Log.d("OBL:Cat", s.name);
            }
        } catch (JSONException e) {
            Log.e("OBL:Cat!", "Failed cause " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
        super.onFailure(statusCode, headers, error, responseBody);
        Log.e("OBL:Cat", "Failed cause " + error.getMessage());
    }
}
