package com.qweex.openbooklikes;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.qweex.openbooklikes.model.Book;
import com.qweex.openbooklikes.model.Post;
import com.qweex.openbooklikes.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class UserFragment extends ListFragment {
    static int MIN_PER_PAGE = 10, IMG_SIZE_PX = 600;
    User user;
    AdapterBase adapter;
// domain = open in browser

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = new User(getArguments().getString("id"), getArguments().getString("username"));
        Log.d("OBL:userOnCreate", "?" + user.username);
        if(user.id.equals(MainActivity.user.id)) {
            // no need to fetch, MainActivity.user has all the info already
            user = MainActivity.user;
        } else {
            RequestParams params = new RequestParams();
            params.put("username", user.username);
            ApiClient.get("user/GetUserInfo", params, userHandler);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View header = getActivity().getLayoutInflater().inflate(R.layout.fragment_user, null);
        getListView().addHeaderView(header);
        Log.d("OBL:userOnCreateView", "?" + header);
        if(user==MainActivity.user)
            fillUi();
        adapter = new BlogAdapter(getActivity(), new ArrayList<Post>());
        setListAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        adapter.clear(); //TODO
        getListView().post(new Runnable() {
            @Override
            public void run() {
                fetchMore(0);
            }
        });
    }

    public void fetchMore(int page) {
        RequestParams params = new RequestParams();
        params.put("PerPage", Math.min(adapter.perScreen(), MIN_PER_PAGE));
        params.put("Page", page);
        params.put("uid", user.id);

        ApiClient.get("post/GetUserPosts", params, blogHandler);
    }

    void fillUi() {
        View v = getView();
        //TODO: image circular?
        Log.d("OBL:fillUi", user.photo);
        String title = user.blog_title != null ? user.blog_title : user.username;
        ImageView pic = (ImageView) v.findViewById(R.id.profilePic);
        pic.getLayoutParams().height = IMG_SIZE_PX;
        pic.getLayoutParams().width = IMG_SIZE_PX;
        MainActivity.imageLoader.displayImage(user.photo.replace("100/100", IMG_SIZE_PX + "/" + IMG_SIZE_PX), pic);
        ((TextView)v.findViewById(R.id.title)).setText(title);
        ((TextView)v.findViewById(R.id.description)).setText(user.blog_desc);
        ((Button)v.findViewById(R.id.bookCount)).setText(user.book_count + " books");
        ((Button)v.findViewById(R.id.followersCount)).setText(user.followed_count + " followers");
        ((Button)v.findViewById(R.id.followingCount)).setText(user.following_count + " following");
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(title);
    }

    JsonHttpResponseHandler userHandler = new JsonHttpResponseHandler() {

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            Log.d("OBL:user.", "Success " + response.length());
            try {
                user = new User(response);
                fillUi();
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

    JsonHttpResponseHandler blogHandler = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            try {
                if (response.getInt("status") != 0 || statusCode >= 400)
                    throw new JSONException(response.getString("message"));
                JSONArray posts = response.getJSONArray("posts");
                for(int i=0; i<posts.length(); i++) {
                    Post p = new Post(posts.getJSONObject(i));
                    // LAST OFF
                    // fragment_post.xml:
                    //   date, title, photo, first part of desc, reblog_count?
                    // how the hell am I going to show other user's shelves?
                    // maybe a popup menu? or an accordian?
                    // don't forget to open blog post in right drawer
                    // Change title to "Blog" for my blog
                    Log.d("OBL:post", "Post: " + p.title);
                    adapter.add(p);
                }
            } catch (JSONException e) {
                Log.e("OBL:Book!", "Failed cause " + e.getMessage());
                e.printStackTrace();
            }
        }
    };

    class BlogAdapter extends AdapterBase<Post> {

        public BlogAdapter(Context context, ArrayList<Post> posts) {
            super(context, 0, posts);
        }

        @Override
        public boolean isEmpty() { return false; }

        @Override
        public View getView(int position, View row, ViewGroup parent) {
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.blog_post, parent, false);
            }
            Post post = getItem(position);

            ((TextView)row.findViewById(R.id.title)).setText(post.title);

            return row;
        }

        @Override
        public int perScreen() {
            return 0;
        }
    }
}
