package com.qweex.openbooklikes;

import com.qweex.openbooklikes.model.ModelBase;
import com.qweex.openbooklikes.notmine.EndlessScrollListener;

abstract public class FetchFragmentBase<Primary extends ModelBase, T extends ModelBase> extends FragmentBase<Primary>
{


    protected AdapterBase<T> adapter;

    protected boolean fetchMore(int page) {
        if(adapter.noMore())
            return false;
        if(page==0)
            showLoading();
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
