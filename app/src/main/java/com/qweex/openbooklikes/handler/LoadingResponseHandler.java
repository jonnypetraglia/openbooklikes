package com.qweex.openbooklikes.handler;

import com.qweex.openbooklikes.ApiClient;
import com.qweex.openbooklikes.LoadingViewManager;
import com.qweex.openbooklikes.fragment.FragmentBase;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public abstract class LoadingResponseHandler extends ApiClient.ApiResponseHandler {
    protected LoadingViewManager loadingManager;

    public static int MIN_PER_SCREEN = 10;

    public LoadingResponseHandler(FragmentBase f) {
        this(f.getLoadingManager());
    }
    public LoadingResponseHandler(LoadingViewManager lvm) {
        super();
        loadingManager = lvm;
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
        super.onSuccess(statusCode, headers, response);
        //TODO: if error show error, else hide loading
        try {
            if(response.getInt("status")>0)
                throw new Exception(response.getString("message"));
            loadingManager.content();
            loadingManager.changeState(LoadingViewManager.State.MORE);
            loadingManager.content();
        } catch (Exception e) {
            e.printStackTrace();
            onFailure(statusCode, headers, e, response);
        }
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
        error.printStackTrace();
        loadingManager.error(error);
    }

    final public boolean noMore() { return currentCount<perScreen(); }

    final public boolean noMoreAfterLastTime() {
        return lastCount<perScreen();
    };

    public int perScreen() {
        return MIN_PER_SCREEN;
    }

    final protected int perScreen(int i) {
        return Math.max(i, MIN_PER_SCREEN);
    }
}
