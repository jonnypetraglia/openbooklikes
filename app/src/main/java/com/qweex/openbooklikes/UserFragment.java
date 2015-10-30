package com.qweex.openbooklikes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

    ArrayList<HeaderData> headerDatas = new ArrayList<>();

    private class HeaderData {
        public int layoutId, stringId;
        public String primaryDataName;
        public View.OnClickListener listener;

        public HeaderData(int l, int s, String p, View.OnClickListener o) {
            layoutId = l;
            stringId = s;
            primaryDataName = p;
            listener = o;
        }

        public void doView(View v) {
            v.setId(layoutId);
            try {
                ((TextView) v.findViewById(R.id.count)).setText(primary.getS(primaryDataName));
            } catch(RuntimeException e) {
                ((TextView) v.findViewById(R.id.count)).setText(Integer.toString(primary.getI(primaryDataName)));
            }
        }
    }

    @Override
    String getTitle() {
        if(primary ==null) //TODO: I don't like this;
            return null; // It's null when the fragment is first created because User is fetched asyncronously
        return primary == MainActivity.me ? "Blog" : primary.properName();
    }

    @Override
    public void setArguments(Bundle a) {
        primary = new User(a);
        Log.d("SET ARGUMENTS", a.getBundle("user").getString("followed_count") + "?" + primary.getS("followed_count"));
        super.setArguments(a);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.option_browser) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, primary.link());
            startActivity(browserIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.add(Menu.NONE, R.id.option_browser, Menu.NONE, R.string.option_browser);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {

        View v = inflater.inflate(R.layout.fragment_user, null);
        ListView listView = (ListView) v.findViewById(R.id.list_view);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(scrollMuch);
        listView.setDivider(null);

        View header = inflater.inflate(R.layout.fragment_user_header, null);

        listView.addHeaderView(header, null, false);

        Log.d("OBL:userOnCreateView", "?" + header);

        adapter = new BlogAdapter(getActivity(), new ArrayList<Post>());
        listView.setAdapter(adapter);
        this.listView = listView;

        for(HeaderData h : headerDatas) {
            View g = inflater.inflate(R.layout.list_user_count, null);
            g.setId(h.layoutId);
            ((TextView)g.findViewById(R.id.title)).setText(h.stringId);
            listView.addHeaderView(g);
        }

        listView.addFooterView(listViewFooter = (ViewGroup) inflater.inflate(R.layout.loading, listView, false));

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void listener(View view) {
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
        setHasOptionsMenu(true);

        Log.d("OBL:userFragment", "!" + primary.id());
        if(primary.equals(MainActivity.me)) {
            // no need to fetch, MainActivity.me has all the info already
            Log.d("OBL:user is me", "woah");
            primary = MainActivity.me;
            // UI will be filled in onViewCreated
        } else {
            RequestParams params = new RequestParams();
            params.put("username", primary.getS("username"));
            Log.d("Fetching User", primary.getS("username") + "!");
            ApiClient.get(params, userHandler);
            // UI will be filled in userHandler
        }

        headerDatas.add(new HeaderData(R.id.books, R.string.books, "book_count", loadShelves));
        headerDatas.add(new HeaderData(R.id.followers, R.string.followers, "followed_count", loadFriends));
        headerDatas.add(new HeaderData(R.id.followings, R.string.followings, "following_count", loadFriends));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(primary !=null) {
            Log.d("OBL:user", "Filling UI from onViewCreated");
            fillUi();
        }
    }

    @Override
    public boolean fetchMore(int page) {
        if(!super.fetchMore(page))
            return false;
        Log.d("OBL:fetchMore", "Fetching more posts, page " + page);
        RequestParams params = new ApiClient.PagedParams(page, adapter);
        params.put("uid", primary.id());

        ApiClient.get(params, blogHandler);
        return true;
    }

    void fillUi() {
        View v = getView();
        int IMG_SIZE = getResources().getDimensionPixelSize(R.dimen.profile_size);

        Log.d("OBL:fillUi", primary.photoSize(IMG_SIZE));
        ImageView pic = (ImageView) v.findViewById(R.id.image_view);
        MainActivity.imageLoader.displayImage(primary.photoSize(IMG_SIZE), pic);
        ((TextView)v.findViewById(R.id.title)).setText(primary.properName());
        ((TextView)v.findViewById(R.id.desc)).setText(primary.getS("blog_desc"));


        for(HeaderData h : headerDatas) {
            h.doView(v.findViewById(h.layoutId));
        }

        getMainActivity().setMainTitle(); //FIXME: UGGGGH I HATE THIS
        hideLoading();
        loadingViewGroup = listViewFooter;
        fetchMore(0); // FIXME: Will EndlessScrollView call this once adapter is cleared?
    }

    View.OnClickListener loadShelves = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(MainActivity.me.id().equals(primary.id()))
                getMainActivity().openLeftDrawer();
            else {
                ShelvesFragment shelvesFragment = new ShelvesFragment();
                shelvesFragment.setArguments(primary.wrapInBundle(new Bundle()));
                getMainActivity().loadSideFragment(shelvesFragment);
            }
        }
    };

    View.OnClickListener loadFriends = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Bundle args = new Bundle();
            args.putInt("relationId", view.getId());
            args.putString("relation", getResources().getString(view.getId()==R.id.followings ? R.string.followings : R.string.followers));
            primary.wrapInBundle(args);

            FriendsFragment friendsFragment = new FriendsFragment();
            friendsFragment.setArguments(args);
            getMainActivity().loadSideFragment(friendsFragment);
        }
    };

    LoadingResponseHandler userHandler = new LoadingResponseHandler() {

        @Override
        protected String urlPath() {
            return "user/GetUserInfo";
        }

        @Override
        protected String countFieldName() {
            return null; //No count
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);
            Log.d("OBL:user.", "Success " + response.length());
            if(wasLastFetchNull()) {
                hideLoading();
                return;
            }
            try {
                primary = new User(response);
                Log.d("OBL:user", "Filling UI from userHandler");
                fillUi();
            } catch (JSONException e) {
                Log.e("OBL:user!", "Failed cause " + e.getMessage());
                e.printStackTrace();
                showError(e.getMessage());
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
            super.onFailure(statusCode, headers, error, responseBody);
            Log.e("OBL:user", "Failed cause " + error.getMessage());
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
            if(wasLastFetchNull()) {
                hideLoading();
                return;
            }
            try {
                if (response.getInt("status") != 0 || statusCode >= 400)
                    throw new JSONException(response.getString("message"));
                JSONArray posts = response.getJSONArray("posts");

                for(int i=0; i<posts.length(); i++) {
                    Post p = new Post(posts.getJSONObject(i), primary);
                    Log.d("OBL:blog", "Post: " + (p.getS("tag")==null));
                    adapter.add(p);
                }
            } catch (JSONException e) {
                Log.e("OBL:Book!", "Failed cause " + e.getMessage());
                e.printStackTrace();
                showError(e.getMessage());
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
            super.onFailure(statusCode, headers, error, responseBody);
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

            ImageView photo = (ImageView) row.findViewById(R.id.image_view);
            if(post.getS("photo_url")!=null)
                MainActivity.imageLoader.displayImage(post.getS("photo_url"), photo);
            else
                photo.setVisibility(View.GONE);


            int MAX_HEIGHT = getResources().getDimensionPixelSize(R.dimen.max_post_height);


            Log.d("OBL:blogadapter", post.getS("title") + " ");
            setOrHide(row, R.id.type, post.getS("type"));
            setOrHide(row, R.id.date, post.getS("date"));
            setOrHide(row, R.id.title, post.getS("title"));

            TextView special = setOrHide(row, R.id.special, post.getS("special"));
            special.setVerticalFadingEdgeEnabled(true);
            special.setMaxHeight(MAX_HEIGHT);
            row.findViewById(R.id.fadeout1).setVisibility(special.getVisibility());

            TextView desc = setOrHide(row, R.id.desc, post.getS("desc"));
            desc.setVerticalFadingEdgeEnabled(true);
            desc.setMaxHeight(MAX_HEIGHT);
            row.findViewById(R.id.fadeout2).setVisibility(desc.getVisibility());

            return row;
        }

        @Override
        public int perScreen() {
            return super.perScreen(0); //TODO?
        }

        @Override
        public boolean noMore() {
            return blogHandler.wasLastFetchNull();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if(position < ((ListView)adapterView).getHeaderViewsCount()) {
            headerDatas.get(position - 1).listener.onClick(view);
            return;
        }
        position -= ((ListView)adapterView).getHeaderViewsCount();
        Post post = adapter.getItem(position);

        Bundle b = primary.wrapInBundle(post.wrapInBundle(new Bundle()));

        PostFragment postFragment = new PostFragment();
        postFragment.setArguments(b);
        getMainActivity().loadSideFragment(postFragment);
    }
}
