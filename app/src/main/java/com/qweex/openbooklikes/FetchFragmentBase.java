package com.qweex.openbooklikes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.qweex.openbooklikes.model.ModelBase;
import com.qweex.openbooklikes.notmine.EndlessScrollListener;

import java.util.ArrayList;

abstract public class FetchFragmentBase<Primary extends ModelBase, T extends ModelBase> extends FragmentBase<Primary>
{

    protected ListView listView;
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
        if(loadingViewGroup == null && adapter!=null && adapter.isEmpty())
            listView.addFooterView(loadingViewGroup = (ViewGroup) inflater.inflate(R.layout.loading, listView, false));
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
        if(adapter.isEmpty())
            showLoading();
        else
            showLoadingAlso();
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
}
