package com.qweex.openbooklikes;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class UserFragment extends FetchFragmentBase<User, Post> implements AdapterView.OnItemClickListener {
    static int MIN_PER_PAGE = 10, IMG_SIZE_PX = 600, MAX_POST_HEIGHT = 200;
    ListView listView;
    // TODO: domain -> open in browser


    @Override
    String getTitle() {
        if(primary ==null) //TODO: I don't like this;
            return null; // It's null when the fragment is first created because User is fetched asyncronously
        return primary ==MainActivity.me ? "Blog" : primary.properName();
    }

    @Override
    public void setArguments(Bundle a) {
        primary = new User(a);
        super.setArguments(a);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {

        View v = inflater.inflate(R.layout.fragment_user, null);
        listView = (ListView) v.findViewById(R.id.list_view);
        listView.setOnItemClickListener(this);
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
        Bundle args = getArguments().getBundle(primary.modelName());
        super.onCreate(savedInstanceState);
        Log.d("OBL:userFragment", "!" + args.getString("id"));
        if(MainActivity.me.id.equals(args.getString("id"))) {
            // no need to fetch, MainActivity.me has all the info already
            primary = MainActivity.me;
            // UI will be filled in onViewCreated
        } else {
            RequestParams params = new RequestParams();
            params.put("username", args.getString("username"));
            ApiClient.get(params, userHandler);
            // UI will be filled in userHandler
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(primary !=null) {
            Log.d("OBL:primary", "Filling UI from onViewCreated");
            fillUi();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.clear(); //TODO: Is this in the right place? Or needed?
        fetchMore(0);
    }

    @Override
    public boolean fetchMore(int page) {
        if(!super.fetchMore(page))
            return false;
        Log.d("OBL:fetchMore", "Fetching more posts, page " + page);
        RequestParams params = new ApiClient.PagedParams(page, adapter);
        params.put("uid", primary.id);

        ApiClient.get(params, blogHandler);
        return true;
    }

    void fillUi() {
        View v = getView();
        Log.d("OBL:fillUi", primary.photoSize(IMG_SIZE_PX));
        ImageView pic = (ImageView) v.findViewById(R.id.profilePic);
        MainActivity.imageLoader.displayImage(primary.photoSize(IMG_SIZE_PX), pic);
        ((TextView)v.findViewById(R.id.title)).setText(primary.properName());
        ((TextView)v.findViewById(R.id.description)).setText(primary.blog_desc);

        Button books = ((Button)v.findViewById(R.id.bookCount));
        books.setText(primary.book_count + " books");
        books.setOnClickListener(loadShelves);

        Button followers = ((Button)v.findViewById(R.id.followersCount));
        followers.setText(primary.followed_count + " followers");
        followers.setOnClickListener(loadFriends);
        Button followings = ((Button)v.findViewById(R.id.followingCount));
        followings.setText(primary.following_count + " following");
        followings.setOnClickListener(loadFriends);

        //TODO: UGGGGH I HATE THIS
        getMainActivity().setMainTitle();
    }

    View.OnClickListener loadShelves = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(MainActivity.me.id.equals(primary.id))
                getMainActivity().openLeftDrawer();
            else {
                ShelvesFragment shelvesFragment = new ShelvesFragment();
                shelvesFragment.setArguments(primary.intoBundle(new Bundle()));
                getMainActivity().loadSideFragment(shelvesFragment);
            }
        }
    };

    View.OnClickListener loadFriends = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FriendsFragment friendsFragment = new FriendsFragment();
            Bundle args = new Bundle();
            args.putString("uid", primary.id);
            args.putString("primary", primary.properName());
            switch(view.getId()) {
                case R.id.followersCount:
                    args.putInt("count", Integer.parseInt(primary.followed_count));
                    args.putString("relation", "Followers");
                    break;
                case R.id.followingCount:
                    args.putInt("count", Integer.parseInt(primary.following_count));
                    args.putString("relation", "Followings");
                    break;
                default:
                    Log.e("OBL", "Unidentified type of friends to fetch");
                    return;
            }
            friendsFragment.setArguments(args);

            getMainActivity().loadSideFragment(friendsFragment);
        }
    };

    LoadingResponseHandler userHandler = new LoadingResponseHandler() {

        @Override
        protected String urlPath() {
            return "primary/GetUserInfo";
        }

        @Override
        protected String countFieldName() {
            return null; //No count
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);
            Log.d("OBL:primary.", "Success " + response.length());
            try {
                primary = new User(response);
                Log.d("OBL:primary", "Filling UI from userHandler");
                fillUi();
            } catch (JSONException e) {
                Log.e("OBL:User!", "Failed cause " + e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
            Log.e("OBL:primary", "Failed cause " + error.getMessage());
        }
    };

    LoadingResponseHandler blogHandler = new LoadingResponseHandler() {

        @Override
        protected String urlPath() {
            return "post/GetUserPosts";
        }

        @Override
        protected String countFieldName() {
            return "count";
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);
            if(wasLastFetchNull())
                return;
            try {
                if (response.getInt("status") != 0 || statusCode >= 400)
                    throw new JSONException(response.getString("message"));
                JSONArray posts = response.getJSONArray("posts");

                for(int i=0; i<posts.length(); i++) {
                    Post p = new Post(posts.getJSONObject(i));
                    Log.d("OBL:primary", "Post: " + p.title);
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
                row = inflater.inflate(R.layout.list_blog_post, parent, false);
            }
            Post post = getItem(position);

            ImageView photo = (ImageView) row.findViewById(R.id.image);
            if(post.photo_url!=null)
                MainActivity.imageLoader.displayImage(post.photo_url, photo);
            else
                photo.setVisibility(View.GONE);



            Log.d("OBL:primary", post.title + " ");
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
            return MIN_PER_PAGE; //TODO?
        }

        @Override
        public boolean noMore() {
            return blogHandler.wasLastFetchNull();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Log.d("Clicked!", adapter.getItem(position).date);
        Post post = adapter.getItem(position - listView.getHeaderViewsCount()); //???? Why is this? because of header?

        Bundle b = post.intoBundle(new Bundle());

        for(String s : getArguments().keySet())
            Log.d("OBL", "!!" + s);

        PostFragment postFragment = new PostFragment();
        postFragment.setArguments(b);
        getMainActivity().loadSideFragment(postFragment);
    }
}
