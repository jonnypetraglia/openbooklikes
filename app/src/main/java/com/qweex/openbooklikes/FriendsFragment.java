package com.qweex.openbooklikes;

import android.content.Context;
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
import com.qweex.openbooklikes.model.User;
import com.qweex.openbooklikes.model.UserPartial;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class FriendsFragment extends FetchFragmentBase<User, UserPartial> implements AdapterView.OnItemClickListener {
    String relation;
    int relationCount = 0;

    @Override
    String getTitle() {
        return relation;
    }

    public boolean is(User user, String relation) {
        return this.primary.equals(user) && this.relation.equals(relation);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("relation", relation);
        outState.putInt("relationCount", relationCount);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new FriendAdapter(getActivity(), R.layout.list_user, new ArrayList<UserPartial>());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState==null) {
            adapter.clear();
            fetchMore(0);
        }
    }

    @Override
    public void setArguments(Bundle b) {
        primary = new User(b);
        if(b.keySet().contains("relationCount")) {
            relation = b.getString("relation");
            relationCount = b.getInt("relationCount");
        }
        else if(b.getInt("relationId")==R.id.followingCount) {
            relation = getResources().getString(R.string.followings);
            relationCount = Integer.parseInt(primary.getS("following_count"));
        } else {
            relation = getResources().getString(R.string.followers);
            relationCount = Integer.parseInt(primary.getS("followed_count"));
        }
        super.setArguments(b);
    }

    @Override
    public boolean fetchMore(int page) {
        if (!super.fetchMore(page))
            return false;
        RequestParams params = new ApiClient.PagedParams(page, adapter);
        params.put("uid", primary.id());


        ApiClient.get(params, friendsHandler);
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {

        listView = new ListView(getActivity());
        listView.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.MATCH_PARENT));
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(scrollMuch);
        ((ListView)listView).setDivider(null);

        listView.setAdapter(adapter);
        return super.createProgressView(inflater, container, listView);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d("OBL:Friends", "Clicked " + adapter.getItem(i).id());
        getMainActivity().loadUser(adapter.getItem(i));
    }

    LoadingResponseHandler friendsHandler = new LoadingResponseHandler() {
        @Override
        protected String urlPath() {
            return "user/GetUser" + relation;
        }

        @Override
        protected String countFieldName() {
            return "count";
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);
            Log.d("OBL:friends.", "Success " + response.length());

            try {
                if (response.getInt("status") != 0 || statusCode >= 400)
                    throw new JSONException(response.getString("message"));
                JSONArray friends = response.getJSONArray("followers");
                for(int i=0; i<friends.length(); i++) {
                    UserPartial f = new UserPartial(friends.getJSONObject(i));
                    adapter.add(f);
                    Log.d("OBL:friends", "User: " + f.id() + " | " + adapter.getCount());
                }
            } catch (JSONException e) {
                Log.e("OBL:friends!", "Failed cause " + e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
            Log.e("OBL:friends", "Failed cause " + error.getMessage());
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

            MainActivity.imageLoader.displayImage(user.getS("photo"), (ImageView) row.findViewById(R.id.profilePic));

            ((TextView)row.findViewById(R.id.title)).setText(user.getS("username"));

            return row;
        }

        @Override
        public int perScreen() {
            return super.perScreen(0);
        }

        @Override
        public boolean noMore() {
            return getCount()==relationCount || friendsHandler.wasLastFetchNull();
        }
    }
}
