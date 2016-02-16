package com.qweex.openbooklikes;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.qweex.openbooklikes.model.Shelf;
import com.qweex.openbooklikes.model.User;

import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class ShelvesFragment extends FetchFragmentBase<User, Shelf> implements AdapterView.OnItemClickListener {

    @Override
    public void setArguments(Bundle a) {
        primary = new User(a);
        super.setArguments(a);
    }

    @Override
    public String getTitle(Resources res) {
        return res.getString(R.string.shelves);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        ArrayList<Shelf> shelves = new ArrayList<Shelf>();
        responseHandler = new ShelvesHandler(loadingManager, shelves, primary) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                adapter = new ShelvesAdapter(getActivity(), shelves);
                listView.setAdapter(adapter); //FIXME: I hate to do this, but the adapter does not update otherwise
                this.loadingManager.content();
            }
        };

        adapter = new ShelvesAdapter(getActivity(), shelves);
        listView.setAdapter(adapter);

        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState==null) {
            RequestParams params = new RequestParams();
            params.put("uid", primary.id());
            ApiClient.get(params, responseHandler);
            this.loadingManager.show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        listView = new ListView(getActivity());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(this);

        return createProgressView(inflater, container, listView);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        getMainActivity().loadShelf(adapter.getItem(i), primary);
    }

    private class ShelvesAdapter extends AdapterBase<Shelf>{
        public ShelvesAdapter(Context context, ArrayList<Shelf> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.list_shelf, parent, false);
            }

            Log.d("gettingview", "?" + position + " " + getItem(position));

            ((TextView)row.findViewById(R.id.title)).setText(getItem(position).getS("name"));
            ((TextView)row.findViewById(R.id.count)).setText(Integer.toString(getItem(position).getI("book_count")));

            return row;
        }

        @Override
        public int perScreen() {
            return super.perScreen(0); // api usually fetches all shelves, so just supply default
        }

        @Override
        public boolean noMore() {
            return responseHandler.wasLastFetchNull();
        }
    }
}
