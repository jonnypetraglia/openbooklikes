package com.qweex.openbooklikes;

import android.content.res.Resources;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.qweex.openbooklikes.activity.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class ApiClient {
    private static final String BASE_URL = "http://booklikes.com/api/v1_05/";
    private static String API_KEY;

    public static void setApiKey(Resources r) { API_KEY = r.getString(R.string.api_key); }


    protected static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(RequestParams params, ApiResponseHandler responseHandler) {
        params.put("key", API_KEY);
        if(MainActivity.me !=null) {
            params.put("usr_token", MainActivity.me.token());
        }
        Log.d("OBL:Getting", getAbsoluteUrl(responseHandler.urlPath()) + "?" + params.toString());
        client.get(getAbsoluteUrl(responseHandler.urlPath()), params, responseHandler);
    }
    public static void get(ApiResponseHandler responseHandler) {
        get(new RequestParams(), responseHandler);
    }

    public static void post(RequestParams params, ApiResponseHandler responseHandler) {
        params.put("key", API_KEY);
        if(MainActivity.me !=null) {
            params.put("usr_token", MainActivity.me.token());
        }
        Log.d("OBL:Posting", getAbsoluteUrl(responseHandler.urlPath()) + "?" + params.toString());
        client.post(getAbsoluteUrl(responseHandler.urlPath()), params, responseHandler);
    }
    public static void post(ApiResponseHandler responseHandler) {
        post(new RequestParams(), responseHandler);
    }

    protected static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }


    public static abstract class ApiResponseHandler extends JsonHttpResponseHandler {
        abstract protected String urlPath();
        abstract protected String countFieldName();

        private boolean lastFetchWasNull = false;
        public boolean wasLastFetchNull() { return lastFetchWasNull; }
        public void reset() { lastFetchWasNull = false; }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            if(!lastFetchWasNull)
                try {
                    lastFetchWasNull = countFieldName()!=null && response.getInt(countFieldName())==0;
                } catch (JSONException e) {
                    e.printStackTrace();
                    lastFetchWasNull = true;
                }
        }
    }

    public static class PagedParams extends RequestParams {
        public PagedParams(int page, AdapterBase adapter) {
            put("PerPage", adapter.perScreen());
            put("Page", page);
        }
    }
}
