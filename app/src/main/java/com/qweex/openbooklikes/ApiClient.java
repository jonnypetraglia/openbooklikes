package com.qweex.openbooklikes;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class ApiClient {
    private static final String BASE_URL = "http://booklikes.com/api/v1_05/",
                                API_KEY = "dfb2e5ef0dab25e69041d7e3fc9111e7";

    protected static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        params.put("key", API_KEY);
        if(MainActivity.user!=null) {
            params.put("usr_token", MainActivity.user.token);
            params.put("uid", MainActivity.user.id);
        }
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }
    public static void get(String url, AsyncHttpResponseHandler responseHandler) {
        get(url, new RequestParams(), responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        params.put("key", API_KEY);
        if(MainActivity.user!=null) {
            params.put("usr_token", MainActivity.user.token);
            params.put("uid", MainActivity.user.id);
        }
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }
    public static void post(String url, AsyncHttpResponseHandler responseHandler) {
        post(url, new RequestParams(), responseHandler);
    }

    protected static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
