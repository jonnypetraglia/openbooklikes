package com.qweex.openbooklikes;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public abstract class LoadingResponseHandler extends ApiClient.ApiResponseHandler {
    protected LoadingViewManager loadingManager;

    public LoadingResponseHandler(FragmentBase f) {
        this(f.loadingManager);
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
