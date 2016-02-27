package com.qweex.openbooklikes.handler;

import android.util.Log;

import com.qweex.openbooklikes.ApiClient;
import com.qweex.openbooklikes.LoadingViewInterface;
import com.qweex.openbooklikes.LoadingViewManager;
import com.qweex.openbooklikes.fragment.FragmentBase;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public abstract class LoadingResponseHandler extends ApiClient.ApiResponseHandler {
    protected LoadingViewInterface loadingManager;

    public static int MIN_PER_SCREEN = 10;

    public LoadingResponseHandler(FragmentBase f) {
        this(f.getLoadingManager());
    }
    public LoadingResponseHandler(LoadingViewInterface lvm) {
        super();
        loadingManager = lvm;
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
        //TODO: if error show error, else hide loading
        try {
            super.onSuccess(statusCode, headers, response);
            if(currentCount<0)
                return; //Exception was handled by super
            loadingManager.content();
            loadingManager.changeState(LoadingViewManager.State.MORE);
            loadingManager.content();
        } catch (Exception e) {
            Log.d("LVM", "!");
            e.printStackTrace();
            onFailure(statusCode, headers, e, response);
        }
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
        error.printStackTrace();
        loadingManager.error(error);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
        throwable.printStackTrace();
        loadingManager.error(throwable);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
        throwable.printStackTrace();
        loadingManager.error(throwable);
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
