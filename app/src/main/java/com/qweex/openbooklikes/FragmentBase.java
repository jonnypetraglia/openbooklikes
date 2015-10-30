package com.qweex.openbooklikes;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qweex.openbooklikes.model.ModelBase;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


abstract public class FragmentBase<Primary extends ModelBase> extends Fragment {
    Primary primary;

    ViewGroup contentView;
    View childView;
    ViewGroup loadingViewGroup;

    abstract String getTitle();

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(primary!=null)
            primary.wrapInBundle(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState!=null) {
            setArguments(savedInstanceState);
            Log.d("oac:" + getClass().getSimpleName(), "Saved data: " + primary.apiName());
         } else {
            Log.d("oac:" + getClass().getSimpleName(), "No saved, fetching data");
        }
    }

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
        contentView = (ViewGroup) inflater.inflate(R.layout.loading, null);
        if(loadingViewGroup==null)
            loadingViewGroup = contentView;

//        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//        lp.addRule(RelativeLayout.ABOVE, R.id.progress);

        contentView.addView(childView); //, lp);
//
//        lp = ((RelativeLayout.LayoutParams)progressView.getLayoutParams());
//        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
//        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//        progressView.setLayoutParams(lp);
        //*/

        this.childView = childView;
        showContentOnly();
        return contentView;
    }

    View getLoadingView(int id) {
        return loadingViewGroup.findViewById(id);
    }

    void setLoadingVisibility(int status, int id) {
        getLoadingView(id).setVisibility(status);
    }

    void setVisibleAndText(String text, int id) {
        TextView v = (TextView) getLoadingView(id);
        v.setVisibility(View.VISIBLE);
        v.setText(text);
    }

    void setVisibleAndText(int textId, int id) {
        TextView v = (TextView) getLoadingView(id);
        v.setVisibility(View.VISIBLE);
        v.setText(textId);
    }


    protected void showError(String text) {
        setLoadingVisibility(View.GONE, R.id.progress);
        setLoadingVisibility(View.GONE, R.id.progress_text);
        setVisibleAndText(text, R.id.error);
        //TODO

        childView.setVisibility(View.GONE);
    }

    protected void showEmpty() {
        childView.setVisibility(View.GONE);
        setLoadingVisibility(View.GONE, R.id.progress);
        setLoadingVisibility(View.GONE, R.id.progress_text);
        setVisibleAndText(R.string.empty, R.id.empty);
        //TODO
    }

    protected void showLoading() {
        showLoading(null);
    }
    protected void showLoadingAlso() {
        showLoadingAlso(null);
    }

    protected void showLoading(String text) {
        childView.setVisibility(View.GONE);
        setLoadingVisibility(View.GONE, R.id.empty);
        setLoadingVisibility(View.GONE, R.id.error);
        showLoadingAlso(text);
    }

    protected void showLoadingAlso(String text) {
        Log.d("Showing", "loading");
        setLoadingVisibility(View.VISIBLE, R.id.progress);
        setVisibleAndText(text, R.id.progress_text);
    }

    protected void showContentOnly() {
        showContent();
        hideLoading();
    }

    protected void showContent() {
        Log.d("Showing", "content");
        childView.setVisibility(View.VISIBLE);
        setLoadingVisibility(View.GONE, R.id.empty);
        setLoadingVisibility(View.GONE, R.id.error);
    }

    protected void hideLoading() {
        setLoadingVisibility(View.GONE, R.id.progress);
        setLoadingVisibility(View.GONE, R.id.progress_text);
    }

    public abstract class LoadingResponseHandler extends ApiClient.ApiResponseHandler {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);
            //TODO: if error show error, else hide loading

            showContent();
            if(this.wasLastFetchNull())
                hideLoading();
            Log.d("Showing? gone", this.wasLastFetchNull() + "? " + getLoadingView(R.id.progress).getVisibility() + " vs " + View.VISIBLE);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
            //TODO: ???
            error.printStackTrace();
            showError(error.getMessage());
        }
    }

    final public boolean isFor(Primary other) {
        return primary.equals(other);
    }


    final protected float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
