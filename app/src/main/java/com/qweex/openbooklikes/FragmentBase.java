package com.qweex.openbooklikes;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qweex.openbooklikes.model.ModelBase;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


abstract public class FragmentBase<Primary extends ModelBase> extends Fragment {
    Primary primary;

    RelativeLayout contentView;
    View childView;
    ProgressBar progressView;
    TextView progressText;

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
        contentView = (RelativeLayout) inflater.inflate(R.layout.loading, null);
        progressView = (ProgressBar) contentView.findViewById(R.id.progress);
        progressText = (TextView) contentView.findViewById(R.id.title);

        progressView.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);


        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


//        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//        lp.addRule(RelativeLayout.ABOVE, progressView.getId());
//
        contentView.addView(childView, lp);
//
//        lp = ((RelativeLayout.LayoutParams)progressView.getLayoutParams());
//        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
//        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//        progressView.setLayoutParams(lp);
        //*/

        this.childView = childView;
        return contentView;
    }

    protected void showError(String text) {
        progressView.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);
        contentView.setBackgroundColor(0xff99cc00);

        TextView errorView = new TextView(getActivity());
        errorView.setText(text);
        contentView.addView(errorView);
        //TODO
    }

    protected void showEmpty() {
        progressView.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);
        contentView.setBackgroundColor(0xffaaaaaa);

        TextView errorView = new TextView(getActivity());
        errorView.setText("Nothing to show");
        contentView.addView(errorView);
        //TODO
    }

    protected void showLoading() {
        showLoading(null);
    }

    protected void showLoading(String text) {
        childView.setVisibility(View.GONE);
        progressView.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
        progressText.setText(text);
    }

    protected void showContent() {
        childView.setVisibility(View.VISIBLE);
        progressView.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);
    }

    public abstract class LoadingResponseHandler extends ApiClient.ApiResponseHandler {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);
            //TODO: if error show error, else hide loading
            showContent();
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
            //TODO: ???
            showError(error.getMessage());
            error.printStackTrace();
        }
    }

    final public boolean isFor(Primary other) {
        return primary.equals(other);
    }


    final protected float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
