package com.qweex.openbooklikes;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.qweex.openbooklikes.model.UserPartial;
import com.qweex.openbooklikes.notmine.EndlessScrollListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class FriendsFragment extends FragmentBase {
    static int MIN_PER_PAGE = 10;

    AdapterBase<UserPartial> adapter;
    ListView listView;


    @Override
    String getTitle() {
        String title = getArguments().getString("type");
        if(!MainActivity.me.id.equals(getArguments().getString("uid")))
            title += " - " + getArguments().getString("name");
        return title;
    }

    public void fetchMore(int page) {
        super.fetchMore(page);
        RequestParams params = new RequestParams();
        params.put("PerPage", Math.min(adapter.perScreen(), MIN_PER_PAGE));
        params.put("Page", page);

        params.put("uid", getArguments().getString("uid"));

        String endpoint = getArguments().getString("type");
        endpoint = endpoint.substring(0,1).toUpperCase() + endpoint.substring(1).toLowerCase();
        // endpoint must now be either Followers or Followings

        ApiClient.get("user/GetUser" + endpoint, params, friendsHandler);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {

        listView = new ListView(getActivity());
        listView.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.MATCH_PARENT));
        listView.setOnItemClickListener(selectFriend);
        listView.setOnScrollListener(scrollMuch);
        listView.setDivider(null);

        adapter = new FriendAdapter(getActivity(), R.layout.list_user, new ArrayList<UserPartial>());
        listView.setAdapter(adapter);
        return super.createProgressView(inflater, container, listView);
    }

    @Override
    public void onStart() {
        super.onStart();

        adapter.clear(); //TODO: Is this in the right place? Or needed?
        fetchMore(0);
    }

    AdapterView.OnItemClickListener selectFriend = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Log.d("OBL:Friends", "Clicked " + adapter.getItem(i).id);
            ((MainActivity)getActivity()).loadUser(adapter.getItem(i));
        }
    };

    EndlessScrollListener scrollMuch = new EndlessScrollListener() {
        @Override
        public boolean onLoadMore(int page, int totalItemsCount) {
            // Triggered only when new data needs to be appended to the list
            // Add whatever code is needed to append new items to your AdapterView
            if (adapter.getCount()==getArguments().getInt("count"))
                return false;
            Log.d("OBL:friends:scrollMuch", "Fetching page " + (page-1));
            fetchMore(page - 1);
            return true; // ONLY if more data is actually being loaded; false otherwise.
        }
    };

    ResponseHandler friendsHandler = new ResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);
            Log.d("OBL:user.", "Success " + response.length());

            try {
                if (response.getInt("status") != 0 || statusCode >= 400)
                    throw new JSONException(response.getString("message"));
                JSONArray friends = response.getJSONArray("followers");
                for(int i=0; i<friends.length(); i++) {
                    UserPartial f = new UserPartial(friends.getJSONObject(i));
                    adapter.add(f);
                    Log.d("OBL:user", "User: " + f.id + " | " + adapter.getCount());
                }
            } catch (JSONException e) {
                Log.e("OBL:User!", "Failed cause " + e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
            Log.e("OBL:user", "Failed cause " + error.getMessage());
        }
    };

    class FriendAdapter extends AdapterBase<UserPartial> {

        public FriendAdapter(Context context, int i, ArrayList objects) {
            super(context, i, objects);
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.list_user, parent, false);
            }
            UserPartial user = getItem(position);

            MainActivity.imageLoader.displayImage(user.photo, (ImageView) row.findViewById(R.id.profilePic));

            ((TextView)row.findViewById(R.id.title)).setText(user.username);
            //((TextView)row.findViewById(R.id.description)).setText(user.);

            return row;
        }

        @Override
        public int perScreen() {
            return 16;
        }
    }
}
