package com.qweex.openbooklikes.handler;

import android.app.Activity;
import android.util.Log;

import com.qweex.openbooklikes.LoadingViewInterface;
import com.qweex.openbooklikes.LoadingViewManager;
import com.qweex.openbooklikes.activity.MainActivity;
import com.qweex.openbooklikes.fragment.FragmentBase;
import com.qweex.openbooklikes.model.Me;
import com.qweex.openbooklikes.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public abstract class UserHandler extends LoadingResponseHandler {
    protected User user;
    Activity activity;

    public UserHandler(LoadingViewInterface v, Activity context) {
        super(v);
        this.activity = context;
    }

    public UserHandler(FragmentBase f) {
        super(f);
        activity = f.getActivity();
    }

    @Override
    protected String urlPath() {
        return "user/GetUserInfo";
    }

    @Override
    protected String countFieldName() {
        return null; //No count
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
        super.onSuccess(statusCode, headers, response);
        Log.d("OBL:user.", "Success " + response.length());

        if(noMoreAfterLastTime())
            return;
        try {
            user = new User(response);
            if(user.id().equals(MainActivity.me.id()))
                MainActivity.me = new Me(response, activity, MainActivity.me);
            Log.d("OBL:user", "Filling UI from userHandler " + user.id());
        } catch (JSONException e) {
            Log.e("OBL:user!", "Failed cause " + e.getMessage());
            e.printStackTrace();
            loadingManager.error(e);
        }
    }
//
//    @Override
//    public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
//        super.onFailure(statusCode, headers, error, responseBody);
//        Log.e("OBL:user", "Failed cause " + error.getMessage());
//    }
}
