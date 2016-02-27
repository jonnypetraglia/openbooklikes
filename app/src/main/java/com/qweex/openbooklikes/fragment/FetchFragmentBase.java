package com.qweex.openbooklikes.fragment;

import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.qweex.openbooklikes.AdapterBase;
import com.qweex.openbooklikes.R;
import com.qweex.openbooklikes.handler.LoadingResponseHandler;
import com.qweex.openbooklikes.model.ModelBase;
import com.qweex.openbooklikes.notmine.EndlessScrollListener;
import com.qweex.openbooklikes.notmine.Misc;

import java.util.ArrayList;

abstract public class FetchFragmentBase<Primary extends ModelBase, T extends ModelBase> extends FragmentBase<Primary>
{

    protected ListView listView;
    protected AdapterBase<T> adapter;
    protected int lastPageFetched;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter.clear();
        if(savedInstanceState!=null) {
            ArrayList<T> data = savedInstanceState.getParcelableArrayList("adapter");
            adapter.addAll(data);
        }
    }

    @Override
    protected View createProgressView(LayoutInflater inflater, ViewGroup container, View childView) {
        if(listView == null)
            listView = new ListView(getActivity());

        View v = super.createProgressView(inflater, container, childView);
        v.findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingManager.show();
                reload();
            }
        });
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("adapter", adapter.getData());
    }

    protected boolean fetchMore(int page) {
        lastPageFetched = page;
        if(primary==null ||
                (responseHandler instanceof LoadingResponseHandler &&
                        ((LoadingResponseHandler)responseHandler).noMore())
                )
            return false;
        loadingManager.show();
        return true;
    }

    protected EndlessScrollListener scrollMuch = new EndlessScrollListener() {
        @Override
        public boolean onLoadMore(int page, int totalItemsCount) {
            // Triggered only when new data needs to be appended to the list
            // Add whatever code is needed to append new items to your AdapterView
            boolean b = fetchMore(page - 1);
//            if(!b)
//                hideLoading();
            return b;
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem mi;
        mi = menu.add(Menu.NONE, R.id.option_reload, Menu.NONE, R.string.reload)
                .setIcon(R.drawable.reload_np83413);
        optionIcon(mi);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.option_reload)
            reload();
        return super.onOptionsItemSelected(item);
    }

    protected void reload() {
        Log.d("OBL", "Reloading " + getClass().getSimpleName());
        responseHandler.reset();
        adapter.clear();
        fetchMore(lastPageFetched);
    }

    protected View.OnClickListener retryLoad = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            responseHandler.reset();
            fetchMore(lastPageFetched);
        }
    };
}
