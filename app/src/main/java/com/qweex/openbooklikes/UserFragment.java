package com.qweex.openbooklikes;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.qweex.openbooklikes.model.Post;
import com.qweex.openbooklikes.model.User;
import com.qweex.openbooklikes.notmine.EndlessScrollListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class UserFragment extends FragmentBase {
    static int MIN_PER_PAGE = 10, IMG_SIZE_PX = 600, MAX_POST_HEIGHT = 200;
    User user;
    BlogAdapter adapter;
    ListView listView;
    // TODO: domain -> open in browser

    @Override
    String getTitle() {
        if(user==null)
            return null;
        return user==MainActivity.me ? "Blog" : user.properName();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {

        View v = inflater.inflate(R.layout.fragment_user, null);
        listView = (ListView) v.findViewById(R.id.list_view);
        listView.setOnItemClickListener(selectPost);
        listView.setOnScrollListener(scrollMuch);
        listView.setDivider(null);

        View header = inflater.inflate(R.layout.fragment_user_header, null);
        listView.addHeaderView(header);
        Log.d("OBL:userOnCreateView", "?" + header);

        adapter = new BlogAdapter(getActivity(), new ArrayList<Post>());
        listView.setAdapter(adapter);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
         */
        return createProgressView(inflater, container, v);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("OBL:userFragment", "!" + getArguments().getString("id"));
        if(MainActivity.me.id.equals(getArguments().getString("id"))) {
            // no need to fetch, MainActivity.me has all the info already
            user = MainActivity.me;
            // UI will be filled in onViewCreated
        } else {
            RequestParams params = new RequestParams();
            params.put("username", getArguments().getString("username"));
            ApiClient.get("user/GetUserInfo", params, userHandler);
            // UI will be filled in userHandler
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(user!=null) {
            Log.d("OBL:user", "Filling UI from onViewCreated");
            fillUi();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        adapter.clear(); //TODO: Is this in the right place? Or needed?
        fetchMore(0);
    }

    EndlessScrollListener scrollMuch = new EndlessScrollListener() {
        @Override
        public boolean onLoadMore(int page, int totalItemsCount) {
            // Triggered only when new data needs to be appended to the list
            // Add whatever code is needed to append new items to your AdapterView
            if (adapter.noMore)
                return false;
            Log.d("OBL:user:scrollMuch", "Fetching page " + (page-1));
            fetchMore(page - 1);
            return true; // ONLY if more data is actually being loaded; false otherwise.
        }
    };

    public void fetchMore(int page) {
        super.fetchMore(page);
        Log.d("OBL:fetchMore", "Fetching more posts, page " + page);
        RequestParams params = new RequestParams();
        params.put("PerPage", Math.min(adapter.perScreen(), MIN_PER_PAGE));
        params.put("Page", page);
        params.put("uid", getArguments().getString("id"));

        ApiClient.get("post/GetUserPosts", params, blogHandler);
    }

    void fillUi() {
        View v = getView();
        Log.d("OBL:fillUi", user.photoSize(IMG_SIZE_PX));
        ImageView pic = (ImageView) v.findViewById(R.id.profilePic);
        MainActivity.imageLoader.displayImage(user.photoSize(IMG_SIZE_PX), pic);
        ((TextView)v.findViewById(R.id.title)).setText(user.properName());
        ((TextView)v.findViewById(R.id.description)).setText(user.blog_desc);
        ((Button)v.findViewById(R.id.bookCount)).setText(user.book_count + " books");

        Button followers = ((Button)v.findViewById(R.id.followersCount));
        followers.setText(user.followed_count + " followers");
        followers.setOnClickListener(loadFriends);
        Button followings = ((Button)v.findViewById(R.id.followingCount));
        followings.setText(user.following_count + " following");
        followings.setOnClickListener(loadFriends);
    }

    View.OnClickListener loadFriends = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            FriendsFragment friendsFragment = new FriendsFragment();
            Bundle args = new Bundle();
            args.putString("uid", user.id);
            args.putString("name", user.properName());
            switch(view.getId()) {
                case R.id.followersCount:
                    args.putInt("count", Integer.parseInt(user.followed_count));
                    args.putString("type", "Followers");
                    break;
                case R.id.followingCount:
                    args.putInt("count", Integer.parseInt(user.following_count));
                    args.putString("type", "Followings");
                    break;
                default:
                    Log.e("OBL", "Unidentified type of friends to fetch");
                    return;
            }
            friendsFragment.setArguments(args);

            getMainActivity().loadSideFragment(friendsFragment);
        }
    };

    ResponseHandler userHandler = new ResponseHandler() {

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);
            Log.d("OBL:user.", "Success " + response.length());
            try {
                user = new User(response);
                Log.d("OBL:user", "Filling UI from userHandler");
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

    ResponseHandler blogHandler = new ResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);
            try {
                if (response.getInt("status") != 0 || statusCode >= 400)
                    throw new JSONException(response.getString("message"));
                if(response.getInt("count")==0) {
                    adapter.noMore = true;
                    return;
                }
                JSONArray posts = response.getJSONArray("posts");

                for(int i=0; i<posts.length(); i++) {
                    Post p = new Post(posts.getJSONObject(i));
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
        public boolean noMore = false;

        public BlogAdapter(Context context, ArrayList<Post> posts) {
            super(context, 0, posts);
        }

        @Override
        public boolean isEmpty() { return false; }

        @Override
        public View getView(int position, View row, ViewGroup parent) {
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.list_blog_post, parent, false);
            }
            Post post = getItem(position);

            ImageView photo = (ImageView) row.findViewById(R.id.image);
            if(post.photo_url!=null)
                MainActivity.imageLoader.displayImage(post.photo_url, photo);
            else
                photo.setVisibility(View.GONE);



            Log.d("OBL:post", post.title + " ");
            setOrHide(row, R.id.type, post.type);
            setOrHide(row, R.id.date, post.date);
            setOrHide(row, R.id.title, post.title);

            TextView special = setOrHide(row, R.id.special, post.special);
            special.setVerticalFadingEdgeEnabled(true);
            special.setMaxHeight(MAX_POST_HEIGHT);
            row.findViewById(R.id.special_fadeout).setVisibility(special.getVisibility());

            TextView desc = setOrHide(row, R.id.description, post.desc);
            desc.setVerticalFadingEdgeEnabled(true);
            desc.setMaxHeight(MAX_POST_HEIGHT);
            row.findViewById(R.id.description_fadeout).setVisibility(desc.getVisibility());

            Log.d("OBL:bg", "Listview: " + listView.getBackground());
            Log.d("OBL:bg", "Parent 1: " + ((View)listView.getParent()).getBackground());
            Log.d("OBL:bg", "Parent 1: " + ((View)listView.getParent().getParent()).getBackground());

            return row;
        }

        @Override
        public int perScreen() {
            return 10; //TODO?
        }
    }

    AdapterView.OnItemClickListener selectPost = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Log.d("Clicked!", adapter.getItem(position).date);
            Post p = adapter.getItem(position-1); //???? Why is this? because of header?
            PostFragment postFragment = new PostFragment();
            postFragment.setPost(p, user.properName());

            getMainActivity().loadSideFragment(postFragment);
        }
    };
}
