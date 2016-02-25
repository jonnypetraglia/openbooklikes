package com.qweex.openbooklikes.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.SupportMenuInflater;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.qweex.openbooklikes.ApiClient;
import com.qweex.openbooklikes.DownloadableImageView;
import com.qweex.openbooklikes.NavDrawerAdapter;
import com.qweex.openbooklikes.R;
import com.qweex.openbooklikes.SettingsManager;
import com.qweex.openbooklikes.Titleable;
import com.qweex.openbooklikes.fragment.BookListFragment;
import com.qweex.openbooklikes.fragment.FragmentBase;
import com.qweex.openbooklikes.fragment.LoginFragment;
import com.qweex.openbooklikes.fragment.PreferenceFragment;
import com.qweex.openbooklikes.fragment.ReadingChallengeFragment;
import com.qweex.openbooklikes.fragment.SearchFragment;
import com.qweex.openbooklikes.fragment.UserFragment;
import com.qweex.openbooklikes.model.Me;
import com.qweex.openbooklikes.model.Shelf;
import com.qweex.openbooklikes.model.User;
import com.qweex.openbooklikes.model.UserPartial;
import com.qweex.openbooklikes.model.Username;

import org.json.JSONException;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnLoginListener {

    public static Me me;
    public static ImageLoader imageLoader;
    public static ArrayList<Shelf> shelves = new ArrayList<>();

    static final String MAIN_FRAGMENT_TAG = "MAIN_FRAGMENT", SIDE_FRAGMENT_TAG = "SIDE_FRAGMENT";

    private DrawerLayout drawer;
    private MenuItem notMeNav, challengeNav;

    ListView drawerList;
    NavDrawerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApiClient.setApiKey(getResources());

        setContentView(R.layout.activity_main);

        PopupMenu p = new PopupMenu(this, null);
        Menu navMenu = p.getMenu();
        new SupportMenuInflater(this).inflate(R.menu.drawer_menu_main, navMenu);
        adapter = new NavDrawerAdapter(navMenu, this);
        notMeNav = navMenu.findItem(R.id.nav_not_me);
        challengeNav = navMenu.findItem(R.id.nav_challenge);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.drawer_list);
        // Init imageLoader
        imageLoader = ImageLoader.getInstance();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator() {
                    @Override
                    public String generate(String s) {
                        return super.generate(s) + s.substring(s.lastIndexOf("."));
                    }
                })
                .build();
        imageLoader.init(config);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View header = getLayoutInflater().inflate(R.layout.app_bar_main_header, null);
        header.setClickable(true);
        drawerList.addHeaderView(header);
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MenuItem item = (MenuItem) adapter.getItem(i - drawerList.getHeaderViewsCount());
                selectNavDrawer(item);
            }
        });

        drawer.setDrawerListener(new ActionBarDrawerToggle(this, drawer, toolbar, 0, 0) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (drawerView != null && drawerView.getId() == R.id.sidebar_content) //or: ((DrawerLayout.LayoutParams)drawerView.getLayoutParams()).gravity==GravityCompat.END)
                    super.onDrawerSlide(drawerView, 0);
                else
                    super.onDrawerSlide(drawerView, slideOffset);
            }
        });
        drawerList.setAdapter(adapter);

        toolbar = (Toolbar) findViewById(R.id.side_toolbar);
        toolbar.setNavigationIcon(R.drawable.x_np337402);
        toolbar.getNavigationIcon().setColorFilter(0xffffffff, PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeRightDrawer();
            }
        });

//        getSupportFragmentManager().addOnBackStackChangedListener(
//                new FragmentManager.OnBackStackChangedListener() {
//                    public void onBackStackChanged() {
//                        //TODO: THis doesn't work for some reason
//                        // but if I can get it working, it will take away the need to do it in loadMainFragment
//
//                        String currentName = getSupportFragmentManager().getBackStackEntryAt(0).getName();
//                        notMeNav.setVisible(!currentName.equals(me.id()));
//                    }
//                });


        if (savedInstanceState != null) {
            //FragmentBase mContent = (FragmentBase)getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
            Fragment myFragment = (Fragment) getSupportFragmentManager()
                    .findFragmentByTag(MAIN_FRAGMENT_TAG);
            //FIXME: what do
            return;
        }


        try {
            MainActivity.me = Me.fromPrefs(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (MainActivity.me == null) {
            showLogin();
        } else
            onLogin();
    }

    public void showLogin() {
        LoginFragment loginForm = new LoginFragment();
        loginForm.setOnLoginListener(this);
        loadMainFragment(loginForm, null);
    }

    @Override
    public void onLogin() {
        Log.d("Logged in", "id=" + me.id());
        SettingsManager.init(this);

        // Select default fragment
        String idStr = SettingsManager.getString(this, "initial_fragment", R.string.default_initial_fragment);
        int id;
        try {
            id = Integer.parseInt(idStr);
            // idStr is numerical, meaning it must be a Shelf's ID instead of the name of a resource
        } catch (Exception e) {
            id = getResources().getIdentifier(idStr, "id", getPackageName());
            // idStr was not numerical, therefore it must be the name of an ID
        }




        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        drawer.setFocusableInTouchMode(false);
        toggle.syncState();
        adapter.setSelected(id);
        closeRightDrawer();
        recreateShelvesNav();

        String arg = SettingsManager.getString(this, "initial_arg", "");
        FragmentBase fragment;
        Username user = me;
        Bundle b;

        switch(id) {
            case R.id.nav_challenge:
                loadChallengeFragment(me); //TODO: replace 'me' with 'user
                return;
            default:
                for(String want : new String[] {idStr, Shelf.NO_SHELF_ID})
                    for(Shelf s : shelves) {
                        if (want.equals(s.id())) {
                            loadShelf(s, me, 0); //TODO: replace 'me' with 'user
                            return;
                        }
                    }
                // Falls through to Blog if (for some ungodly reason) even "All Books" isn't found
            case R.id.nav_blog:
                fragment = new UserFragment();
                if(arg.length() == 0) {
                    b = me.wrapInBundle(new Bundle());
                } else {
                    user = Username.create(arg);
                    b = user.wrapInBundle(new Bundle());
                }
                fragment.setArguments(b);
                break;
            case R.id.nav_search:
                fragment = new SearchFragment();
                if(arg.length()>0) {
                    b = new Bundle();
                    b.putString("q", arg);
                    fragment.setArguments(b);
                }
                break;
        }
        loadMainFragment(fragment, user);
    }

    public void recreateShelvesNav() {
        ((TextView) drawerList.findViewById(R.id.nav_title)).setText(me.getS("blog_title"));
        ((TextView) drawerList.findViewById(R.id.nav_subtitle)).setText(me.getS("domain"));
        Drawable placeholder = getResources().getDrawable(R.drawable.profile_np76855);
        placeholder.setColorFilter(0xff333333, PorterDuff.Mode.SRC_ATOP);
        imageLoader.displayImage(
                me.getS("photo"),
                (ImageView) drawerList.findViewById(R.id.image_view),
                new DisplayImageOptions.Builder()
                        .showImageOnLoading(placeholder)
                        .showImageForEmptyUri(placeholder)
                        .showImageOnFail(placeholder)
                        .cacheInMemory(true)
                        .cacheOnDisk(true)
                        .build()
        );

        // Add shelfMap to menu
        Menu shelfNav = adapter.getMenu().findItem(R.id.nav_shelves).getSubMenu();
        shelfNav.clear();
        for(Shelf s : shelves) {
            if(SettingsManager.hiddenShelvesIds.contains(s.id()))
                continue;
            Log.d("recreateShelvesNav", s.getTitle(getResources()));
            Intent i = new Intent();
            i.putExtra("count", s.getI("book_count"));
            shelfNav.add(R.id.nav_group,
                    s.isAllBooks() ? R.id.nav_all_shelf : Integer.parseInt(s.id()),
                    0,
                    s.getS("name"))
                    .setCheckable(true)
                    .setIcon(R.drawable.shelf_np147205)
                    .setIntent(i);
        }
        adapter.notifyDataSetInvalidated();
    }

    /*
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the fragment's instance
        getSupportFragmentManager().putFragment(outState, "mContent", mContent);
    }
    */

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount()>0) {
            getSupportFragmentManager().popBackStack();
            return;
        }
        Log.d("onBackPressed", drawer.isDrawerOpen(GravityCompat.START) + " | " + drawer.isDrawerOpen(GravityCompat.END));
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            closeRightDrawer();
        } else if (drawer.isDrawerOpen(GravityCompat.START)) {
            closeLeftDrawer();
        } else {
            super.onBackPressed();
        }
    }

    void selectNavDrawer(MenuItem item) {

        if(adapter.isSelected(item)) {
            closeLeftDrawer();
            return;
        }

        switch(item.getItemId()) {
            case R.id.nav_all_shelf:
            default:
                Shelf shelf = shelves.get(adapter.indexOf(item) - 4); //FIXME: Super ugly
                loadShelf(shelf, me, SettingsManager.FILTER_ALL);
                break;
            case R.id.nav_blog:
                loadUser(me);
                break;
            case R.id.nav_search:
                loadMainFragment(new SearchFragment(), MainActivity.me);
                break;
            case R.id.nav_challenge:
                loadChallengeFragment(MainActivity.me);
                break;
            case R.id.nav_manage_shelves:
                startActivityForResult(new Intent(MainActivity.this, ManageShelvesActivity.class), 0);
                return;
            case R.id.nav_settings:
                loadMainFragment(new PreferenceFragment(), MainActivity.me);
                break;
        }

        adapter.setSelected(item);

        closeLeftDrawer();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // After ManageShelvesActivity
        super.onActivityResult(requestCode, resultCode, data);
        try {
            shelves = SettingsManager.loadShelves(this);
            recreateShelvesNav();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void openRightDrawer() {
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN, GravityCompat.END);
    }

    public void openLeftDrawer() {
        closeRightDrawer();
        drawer.openDrawer(GravityCompat.START);
    }

    public void closeRightDrawer() {
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
    }

    public void closeLeftDrawer() {
        drawer.closeDrawer(GravityCompat.START);
    }

    public void loadShelf(Shelf shelf, User user, int filters) {
        Log.d("OBL", "loadShelf " + shelf.getS("name") + " | " + user.getS("username"));

        Bundle b = new Bundle();
        shelf.wrapInBundle(b);
        user.wrapInBundle(b);
        b.putInt("filters", filters);

        BookListFragment bookListFragment = new BookListFragment();
        bookListFragment.setArguments(b);
        loadMainFragment(bookListFragment, user);
    }

    public void loadUser(UserPartial user) {
        Log.d("OBL", "loadUser " + user.id());

        Bundle b = user.wrapInBundle(new Bundle());

        UserFragment userFragment = new UserFragment();
        userFragment.setArguments(b);
        loadMainFragment(userFragment, user);
    }

    public void loadChallengeFragment(UserPartial user) {
        ReadingChallengeFragment challengeFragment = new ReadingChallengeFragment();
        Bundle b = new Bundle();
        user.wrapInBundle(b);
        challengeFragment.setArguments(b);
        if(user instanceof Me) {
            loadMainFragment(challengeFragment, user);
        } else
            loadSideFragment(challengeFragment);
    }

    private void loadMainFragment(Fragment fragment, Username owner) {
        closeRightDrawer();
        if(fragment instanceof LoginFragment)
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START);

        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();

        boolean wasVisible = notMeNav.isVisible();
        if (owner != null) {
            if (me.equals(owner)) {
                notMeNav.setVisible(false);
                if (fragment instanceof ReadingChallengeFragment) {
                    adapter.setSelected(challengeNav);
                }
            } else {
                notMeNav.setVisible(true);
                adapter.setSelected(notMeNav);
                if (fragment instanceof Titleable)
                    notMeNav.setTitle(((Titleable) fragment).getTitle(getResources()));
                if (fragment.getClass().equals(UserFragment.class))
                    notMeNav.setIcon(R.drawable.profile_np76855);
                else
                    notMeNav.setIcon(R.drawable.shelf_np147205);
                //transaction.add(R.id.fragment, fragment, MAIN_FRAGMENT_TAG);
            }
            if (wasVisible != notMeNav.isVisible())
                adapter.notifyDataSetInvalidated();
        }

        transaction.replace(R.id.fragment, fragment, MAIN_FRAGMENT_TAG); //TODO: do "add" one day
        //transaction.addToBackStack(owner.id());
        transaction.commit();

        if(fragment instanceof Titleable)
            getSupportActionBar().setTitle(((Titleable) fragment).getTitle(getResources()));
    }

    public void loadSideFragment(FragmentBase fragment) {
        openRightDrawer();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.side_fragment, fragment, SIDE_FRAGMENT_TAG)
                .commit();
        Toolbar sideToolbar = ((Toolbar) findViewById(R.id.side_toolbar));
        sideToolbar.setTitle(fragment.getTitle(getResources()));
        fragment.onCreateOptionsMenu(sideToolbar.getMenu(), getMenuInflater());
        sideToolbar.setOnMenuItemClickListener(fragment);
    }


    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    public int getActionBarHeight() {
        int result = 0;
        TypedValue tv = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
        if (tv.resourceId > 0) {
            result = getResources().getDimensionPixelSize(tv.resourceId);
        }
        return result;
    }
}
