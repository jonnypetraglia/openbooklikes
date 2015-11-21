package com.qweex.openbooklikes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qweex.openbooklikes.model.ModelBase;
import com.qweex.openbooklikes.model.Shareable;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;



abstract public class FragmentBase<Primary extends ModelBase> extends Fragment implements Toolbar.OnMenuItemClickListener {
    Primary primary;
    ApiClient.ApiResponseHandler responseHandler;
    LoadingViewManager loadingManager = new LoadingViewManager();

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false); // Children should set this if they have one
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        //getActivity().onCreateOptionsMenu(menu);
        if(primary instanceof  Shareable)
            menu.add(Menu.NONE, R.id.option_share, Menu.NONE, R.string.option_share)
                    .setIcon(android.R.drawable.ic_menu_share)//TODO: Icon
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.option_share) {
            startActivity(Intent.createChooser(((Shareable) primary).share(), getResources().getString(R.string.option_share)));
            return true;
        }
        return super.onOptionsItemSelected(item);
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

        ViewGroup loadingView = (ViewGroup) inflater.inflate(R.layout.loading, null);
        View emptyView = inflater.inflate(R.layout.empty, null);


        childView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        linearLayout.addView(loadingView);
        linearLayout.addView(emptyView);
        linearLayout.addView(childView);

        loadingManager.setInitial(loadingView, childView, emptyView);
        loadingManager.changeState(LoadingViewManager.State.INITIAL);
        loadingManager.content();

        return linearLayout;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return onOptionsItemSelected(item);
    }

    public abstract class LoadingResponseHandler extends ApiClient.ApiResponseHandler {
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

    final protected float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
