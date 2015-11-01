package com.qweex.openbooklikes;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.qweex.openbooklikes.model.ModelBase;
import com.qweex.openbooklikes.notmine.EndlessScrollListener;

import java.util.ArrayList;

abstract public class FetchFragmentBase<Primary extends ModelBase, T extends ModelBase> extends FragmentBase<Primary>
{

    protected ListView listView;
    protected ViewGroup listViewFooter;
    protected AdapterBase<T> adapter;

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
        else if(listView.getFooterViewsCount()==0)
            listView.addFooterView(listViewFooter = (ViewGroup) inflater.inflate(R.layout.loading, listView, false));
        return super.createProgressView(inflater, container, childView);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("adapter", adapter.getData());
    }

    protected boolean fetchMore(int page) {
        if(primary==null || adapter.noMore())
            return false;
        if(page==0)
            showLoading();
        else
            showLoadingAlso();
        return true;
    }

    @Override
    protected void showLoadingAlso(String text) {
        if(!adapter.isEmpty()) {
            hideLoading();
            loadingViewGroup = getLoadingMoreViewGroup();
        }
        super.showLoadingAlso(text);
    }

    protected ViewGroup getLoadingMoreViewGroup() {
        return listViewFooter;
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
        menu.add(Menu.NONE, R.id.reload, Menu.NONE, R.string.reload)
                .setIcon(android.R.drawable.ic_menu_preferences)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.reload)
            reload();
        return super.onOptionsItemSelected(item);
    }

    protected void reload() {
        Log.d("OBL", "Reloading " + getClass().getSimpleName());
        responseHandler.reset();
        adapter.clear();
        fetchMore(0);
    }
}
