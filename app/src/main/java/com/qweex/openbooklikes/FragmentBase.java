package com.qweex.openbooklikes;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


abstract public class FragmentBase extends Fragment {
    RelativeLayout contentView;
    View childView;
    ProgressBar progressView;

    abstract String getTitle();

    protected TextView setOrHide(View container, int tvId, String text) {
        TextView tv = ((TextView)container.findViewById(tvId));
        tv.setText(text);
        tv.setVisibility(text == null ? View.GONE : View.VISIBLE);
        return tv;
    }

    protected MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }


    protected View createProgressView(LayoutInflater inflater, ViewGroup container, View childView) {
        contentView = (RelativeLayout) inflater.inflate(R.layout.loading, null);
        progressView = (ProgressBar) contentView.findViewById(R.id.progress);

        progressView.setVisibility(View.GONE);

        this.childView = childView;
        contentView.addView(childView);
        return contentView;
    }

    protected void showError() {
        progressView.setVisibility(View.VISIBLE);
        contentView.setBackgroundColor(0xff99cc00);
        //TODO
    }

    protected void showEmpty() {
        progressView.setVisibility(View.VISIBLE);
        contentView.setBackgroundColor(0xffaaaaaa);
        //TODO
    }

    protected void showLoading() {
        childView.setVisibility(View.GONE);
        progressView.setVisibility(View.VISIBLE);
    }

    protected void showContent() {
        childView.setVisibility(View.VISIBLE);
        progressView.setVisibility(View.GONE);
    }


    protected void fetchMore(int page) {
        if(page==0)
            showLoading();
    }

    public class ResponseHandler extends JsonHttpResponseHandler {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            //TODO: if error show error, else hide loading
            showContent();
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {

        }
    }

}
