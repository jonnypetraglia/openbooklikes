package com.qweex.openbooklikes.handler;

import com.qweex.openbooklikes.ApiClient;
import com.qweex.openbooklikes.LoadingViewManager;
import com.qweex.openbooklikes.fragment.FragmentBase;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public abstract class LoadingResponseHandler extends ApiClient.ApiResponseHandler {
    protected LoadingViewManager loadingManager;

    public LoadingResponseHandler(FragmentBase f) {
        this(f.getLoadingManager());
    }
    public LoadingResponseHandler(LoadingViewManager lvm) {
        super();
        loadingManager = lvm;
    }
    private LoadingResponseHandler() {}

    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
        super.onSuccess(statusCode, headers, response);
        //TODO: if error show error, else hide loading

        loadingManager.content();
        loadingManager.changeState(LoadingViewManager.State.MORE);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
        //TODO: ???
        error.printStackTrace();
        loadingManager.error(error);
    }
}
