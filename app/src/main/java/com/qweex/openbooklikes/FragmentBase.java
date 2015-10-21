package com.qweex.openbooklikes;

import android.support.v4.app.Fragment;
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
    RelativeLayout contentView;
    View childView;
    ProgressBar progressView;
    TextView progressText;
    Primary primary;

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
        progressText = (TextView) contentView.findViewById(R.id.textView);

        progressView.setVisibility(View.GONE);

        this.childView = childView;
        contentView.addView(childView);
        return contentView;
    }

    protected void showError() {
        progressView.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);
        contentView.setBackgroundColor(0xff99cc00);
        //TODO
    }

    protected void showEmpty() {
        progressView.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);
        contentView.setBackgroundColor(0xffaaaaaa);
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
            error.printStackTrace();
        }
    }

    final public boolean isFor(Primary other) {
        return primary.equals(other);
    }

}
