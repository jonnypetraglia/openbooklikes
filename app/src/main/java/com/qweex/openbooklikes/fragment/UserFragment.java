package com.qweex.openbooklikes.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.qweex.linkspan.LinkSpan;
import com.qweex.openbooklikes.AdapterBase;
import com.qweex.openbooklikes.ApiClient;
import com.qweex.openbooklikes.DownloadableImageView;
import com.qweex.openbooklikes.LoadingViewManager;
import com.qweex.openbooklikes.R;
import com.qweex.openbooklikes.SettingsManager;
import com.qweex.openbooklikes.activity.MainActivity;
import com.qweex.openbooklikes.handler.LoadingResponseHandler;
import com.qweex.openbooklikes.model.ModelBase;
import com.qweex.openbooklikes.model.Post;
import com.qweex.openbooklikes.model.User;
import com.qweex.openbooklikes.model.UserPartial;
import com.qweex.openbooklikes.model.Username;
import com.qweex.openbooklikes.notmine.Misc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class UserFragment extends FetchFragmentBase<Username, Post> implements AdapterView.OnItemClickListener
{

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
            TextView count = (TextView) v.findViewById(R.id.count);
            try {
                try {
                    count.setText(primary.getS(primaryDataName));
                } catch (RuntimeException e) {
                    count.setText(Integer.toString(primary.getI(primaryDataName)));
                }
            } catch (RuntimeException e) {
                count.setText(primaryDataName);
            }
        }
    }

    @Override
    public String getTitle(Resources res) {
        if(primary.equals(MainActivity.me))
            return res.getString(R.string.blog);
        else
            return primary.getS("username");
    }

    @Override
    public void setArguments(Bundle a) {
        primary = User.fromData(a);
        super.setArguments(a);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.option_browser) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, ((UserPartial)primary).link());
            startActivity(browserIntent);
            return true;
        }
        if(item.getItemId()==R.id.option_add)
            PostCreateFragment.showTypePicker(this, getMainActivity());
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.add(Menu.NONE, R.id.option_browser, Menu.NONE, R.string.option_browser);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        if(MainActivity.me.equals(primary)) {
            item = menu.add(Menu.NONE, R.id.option_add, Menu.NONE, "Create") //TODO: String
                    .setIcon(R.drawable.add_np45467);
            optionIcon(item);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

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


        for(HeaderData h : headerDatas) {
            View g = inflater.inflate(R.layout.list_user_count, null);
            g.setId(h.layoutId);
            ((TextView)g.findViewById(R.id.title)).setText(getResources().getString(h.stringId, ""));
            listView.addHeaderView(g);
        }

        ViewGroup listViewFooter = (ViewGroup) inflater.inflate(R.layout.loading, listView, false);
        View footerError = inflater.inflate(R.layout.error, listView, false);
        listView.addFooterView(listViewFooter);
        listView.addFooterView(footerError);
        View dummy = new View(getContext()),
                moreEmpty = inflater.inflate(R.layout.empty, null);
        ((TextView)moreEmpty.findViewById(R.id.title)).setText("No posts to show"); //TODO: String


        loadingManager.setInitial(
                inflater.inflate(R.layout.empty, null),
                v,
                dummy,
                inflater.inflate(R.layout.error, null));
        loadingManager.addMore(listViewFooter, moreEmpty, dummy, footerError); //FIXME: emptyView
        footerError.findViewById(R.id.retry).setOnClickListener(retryLoad);
        footerError.findViewById(R.id.retry).setOnClickListener(retryLoad);

        listView.setAdapter(adapter);
        this.listView = listView;
        return createProgressView(inflater, container, loadingManager.wrapInitialInLayout(getActivity()));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        adapter = new BlogAdapter(getActivity(), new ArrayList<Post>());

        boolean forceFetch = SettingsManager.userInfoExpired(getActivity());
        Log.d("OBL:userFragment", "!" + primary.getS("username"));
        if(primary.equals(MainActivity.me) && !forceFetch) {
            // no need to fetch, MainActivity.me has all the info already
            Log.d("OBL:user is me", "woah");
            primary = MainActivity.me;
            loadingManager.changeState(LoadingViewManager.State.MORE);

            // UI will be filled in onViewCreated
        }

        headerDatas.add(new HeaderData(R.id.books, R.string.books, "book_count", loadShelves));
        headerDatas.add(new HeaderData(R.id.followers, R.string.followers, "followed_count", loadFriends));
        headerDatas.add(new HeaderData(R.id.followings, R.string.followings, "following_count", loadFriends));
        headerDatas.add(new HeaderData(R.id.challenge, R.string.challenge, null, loadChallenge));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(primary instanceof User)
            fillUi();
        else
            reload();
    }

    @Override
    public boolean fetchMore(int page) {
        if(!super.fetchMore(page) || primary.getClass().equals(Username.class))
            return false;
        Log.d("OBL:fetchMore", "Fetching more posts, page " + page);
        RequestParams params = new ApiClient.PagedParams(page, blogHandler);
        params.put("uid", primary.id());

        ApiClient.get(params, responseHandler = blogHandler);
        return true;
    }

    void fillUi() {
        int IMG_SIZE = getResources().getDimensionPixelSize(R.dimen.profile_size);

        ImageView pic = (ImageView) listView.findViewById(R.id.image_view);

        Drawable placeholder = getResources().getDrawable(R.drawable.profile_np76855);
        VectorDrawable vectorDrawable = (VectorDrawable) placeholder;
        placeholder = Misc.resizeDrawable(vectorDrawable,
                pic.getLayoutParams().width,
                pic.getLayoutParams().height);
        placeholder.setColorFilter(0xff333333, PorterDuff.Mode.SRC_ATOP);

        String imgUrl = ((User) primary).photoSize(IMG_SIZE);
        ((DownloadableImageView)pic).setSource(((User) primary).properName(), imgUrl);
        MainActivity.imageLoader.displayImage(
                imgUrl,
                pic,
                new DisplayImageOptions.Builder()
                        .showImageOnLoading(placeholder)
                        .showImageForEmptyUri(placeholder)
                        .showImageOnFail(placeholder)
                        .cacheInMemory(true)
                        .cacheOnDisk(true)
                        .build()

        );

        ((TextView) listView.findViewById(R.id.title)).setText(((User) primary).properName());
        TextView desc = (TextView) listView.findViewById(R.id.desc);
        ModelBase.unHTML(desc, primary.getS("blog_desc"));
        LinkSpan.replaceURLSpans(desc, this, this);



        for (HeaderData h : headerDatas)
            h.doView(listView.findViewById(h.layoutId));

        loadingManager.content();
        loadingManager.changeState(LoadingViewManager.State.MORE);
        getMainActivity().recreateShelvesNav();
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

    View.OnClickListener loadChallenge = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            getMainActivity().loadChallengeFragment(((UserPartial)primary));
        }
    };


    LoadingResponseHandler blogHandler = new LoadingResponseHandler(this) {

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
            loadingManager.content();

            if(noMoreAfterLastTime())
                return;

            try {
                if (response.getInt("status") != 0 || statusCode >= 400)
                    throw new JSONException(response.getString("message"));
                JSONArray posts = response.getJSONArray("posts");

                for(int i=0; i<posts.length(); i++) {
                    Post p = new Post(posts.getJSONObject(i), (UserPartial)primary);
                    adapter.add(p);
                }
            } catch (JSONException e) {
                Log.e("OBL:Book!", "Failed cause " + e.getMessage());
                e.printStackTrace();
                loadingManager.error(e);
            }
            Log.d("OBL:book", "Count==" + adapter.getCount() + " ? " + adapter.isEmpty());
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


            int MAX_HEIGHT = getResources().getDimensionPixelSize(R.dimen.max_post_height);


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

    @Override
    protected void reload() {
        adapter.clear();
        loadingManager.changeState(LoadingViewManager.State.INITIAL);
        loadingManager.show();
        RequestParams params = new RequestParams();
        params.put("username", primary.getS("username"));
        Log.d("Fetching User", primary.getS("username") + "!");
        ApiClient.get(params, new UserHandler(this));
    }

    class UserHandler extends com.qweex.openbooklikes.handler.UserHandler {

        public UserHandler(FragmentBase f) {
            super(f);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);
            primary = this.user;
            if(noMoreAfterLastTime() || getActivity()==null)
                return;
            fillUi();
        }
    }
}
