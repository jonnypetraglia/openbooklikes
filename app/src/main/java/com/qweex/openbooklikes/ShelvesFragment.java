package com.qweex.openbooklikes;

import android.content.Context;
import android.os.Bundle;
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
    String getTitle() {
        return "Shelves";
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        responseHandler = new ShelvesHandler(new ArrayList<Shelf>(), primary) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                adapter.notifyDataSetChanged();
                showContent();
                if(this.wasLastFetchNull())
                    hideLoading();
            }
        };
        adapter = new ShelvesAdapter(getActivity(), ((ShelvesHandler)responseHandler).shelves);

        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState==null) {
            RequestParams params = new RequestParams();
            params.put("uid", primary.id());
            ApiClient.get(params, responseHandler);
            showLoading();
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
