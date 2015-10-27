package com.qweex.openbooklikes;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ListView;

import com.qweex.openbooklikes.model.ModelBase;
import com.qweex.openbooklikes.notmine.EndlessScrollListener;

import java.util.ArrayList;

abstract public class FetchFragmentBase<Primary extends ModelBase, T extends ModelBase> extends FragmentBase<Primary>
{

    protected AbsListView listView;
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
        listView = new ListView(getActivity());
        return super.createProgressView(inflater, container, childView);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("adapter", adapter.getData());
    }

    protected boolean fetchMore(int page) {
        if(adapter.noMore())
            return false;
        if(page==0)
            showLoading();
        else
            showLoadingMore();
        return true;
    }

    protected EndlessScrollListener scrollMuch = new EndlessScrollListener() {
        @Override
        public boolean onLoadMore(int page, int totalItemsCount) {
            // Triggered only when new data needs to be appended to the list
            // Add whatever code is needed to append new items to your AdapterView
            return fetchMore(page - 1);
        }
    };
}
