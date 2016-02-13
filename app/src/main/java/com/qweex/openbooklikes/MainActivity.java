package com.qweex.openbooklikes;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.qweex.openbooklikes.model.Me;
import com.qweex.openbooklikes.model.Shelf;
import com.qweex.openbooklikes.model.User;
import com.qweex.openbooklikes.model.UserPartial;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static Me me;
    public static ImageLoader imageLoader;
    public static ArrayList<Shelf> shelves = new ArrayList<>();

    static final String MAIN_FRAGMENT_TAG = "MAIN_FRAGMENT", SIDE_FRAGMENT_TAG = "SIDE_FRAGMENT";

    private ArrayList<MenuItem> shelfMenuItems = new ArrayList<>();
    private DrawerLayout drawer;
    private MenuItem selectedNav, notMeNav, challengeNav, blogNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("OBL:MASTER", "token: " + me.token());

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        drawer.setFocusableInTouchMode(false);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ///////////////////////////////////////////////

        drawer.setDrawerListener(new ActionBarDrawerToggle(this, drawer, toolbar, 0, 0) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (drawerView != null && drawerView.getId() == R.id.sidebar_content) //or: ((DrawerLayout.LayoutParams)drawerView.getLayoutParams()).gravity==GravityCompat.END)
                    super.onDrawerSlide(drawerView, 0);
                else
                    super.onDrawerSlide(drawerView, slideOffset);
            }
        });
        closeRightDrawer();

        // Init imageLoader
        imageLoader = ImageLoader.getInstance();
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(android.R.drawable.ic_menu_compass)
                .showImageForEmptyUri(android.R.drawable.ic_menu_gallery)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(options)
                .build();
        imageLoader.init(config);

        // Load primary info
        ((TextView) findViewById(R.id.username)).setText(me.getS("username"));
        ((TextView)findViewById(R.id.email)).setText(me.getS("email"));
        imageLoader.displayImage(me.getS("photo"), (ImageView) findViewById(R.id.image_view));

        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);

        // Locate the important nav items
        notMeNav = navView.getMenu().findItem(R.id.nav_not_me);
        blogNav = navView.getMenu().findItem(R.id.nav_blog);
        challengeNav = navView.getMenu().findItem(R.id.nav_challenge);

        // Add shelfMap to menu
        Menu shelfNav = navView.getMenu().findItem(R.id.nav_shelves).getSubMenu();
        shelfNav.clear();
        for(Shelf s : shelves) {
            MenuItem mitem = shelfNav.add(R.id.nav_group,
                        s.isAllBooks() ? R.id.nav_all_shelf : R.id.nav_shelf,
                        0,
                        s.getS("name") + " (" + s.getI("book_count") + ")")
                .setCheckable(true)
                .setIcon(android.R.drawable.ic_menu_compass); //TODO: Icon
            shelfMenuItems.add(mitem);
        }


        toolbar = (Toolbar) findViewById(R.id.side_toolbar);
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeRightDrawer();
            }
        });


        if(savedInstanceState==null) {
            // Select default fragment
            MenuItem start = navView.getMenu().findItem(R.id.nav_blog); //TODO: settings
            onNavigationItemSelected(start);
            navView.setCheckedItem(start.getItemId());
        } else {
            //FragmentBase mContent = (FragmentBase)getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
            Fragment myFragment = (Fragment) getSupportFragmentManager()
                    .findFragmentByTag(MAIN_FRAGMENT_TAG);
            //FIXME: what do
        }



        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        //TODO: THis doesn't work for some reason
                        // but if I can get it working, it will take away the need to do it in loadMainFragment
                        String currentName = getSupportFragmentManager().getBackStackEntryAt(0).getName();
                        notMeNav.setVisible(!currentName.equals(me.id()));
                    }
                });
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
        Log.d("onBackPressed", drawer.isDrawerOpen(GravityCompat.START) + " | " + drawer.isDrawerOpen(GravityCompat.END));
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            closeRightDrawer();
        } else if (drawer.isDrawerOpen(GravityCompat.START)) {
            closeLeftDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(item==selectedNav) {
            closeLeftDrawer();
            return true;
        }

        switch(id) {
            case R.id.nav_all_shelf:
            case R.id.nav_shelf:
                Shelf shelf = shelves.get(shelfMenuItems.indexOf(item));
                Log.d("OBL:nav_shelf", shelf.getS("name"));
                loadShelf(shelf, me);
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
            case R.id.nav_logout:
                logout();
                return false;
        }

        if(item.getGroupId()==R.id.nav_group)
            selectedNav = item;

        closeLeftDrawer();
        return true;
    }

    public void logout() {
        //Ask the primary if they want to quit
        new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.menu_logout)
            .setMessage(R.string.confirm_logout)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getSharedPreferences(Me.USER_DATA_PREFS, MODE_PRIVATE)
                            .edit().clear().apply();
                    imageLoader.clearDiskCache();
                    imageLoader.clearMemoryCache();
                    startActivity(new Intent(MainActivity.this, LaunchActivity.class));
                    MainActivity.this.finish();
                }
            })
            .setNegativeButton(android.R.string.no, null)
            .show();
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

    public void loadShelf(Shelf shelf, User user) {
        Log.d("OBL", "loadShelf " + shelf.getS("name") + " | " + user.getS("username"));

        Bundle b = new Bundle();
        shelf.wrapInBundle(b);
        user.wrapInBundle(b);

        BookListFragment bookListFragment = new BookListFragment();
        bookListFragment.setArguments(b);
        loadMainFragment(bookListFragment, user);
    }

    public void loadUser(UserPartial user) {
        Log.d("OBL", "loadUser " + user.id());

        Bundle b = user.wrapInBundle(new Bundle());

        UserFragment userFragment = new UserFragment();
        userFragment.setArguments(b);
        if(user instanceof Me) {
            blogNav.setChecked(true);
            notMeNav.setVisible(false);
        }
        loadMainFragment(userFragment, user);
    }

    public void loadChallengeFragment(UserPartial user) {
        ReadingChallengeFragment challengeFragment = new ReadingChallengeFragment();
        Bundle b = new Bundle();
        user.wrapInBundle(b);
        challengeFragment.setArguments(b);
        if(user instanceof Me) {
            loadMainFragment(challengeFragment, user);
            challengeNav.setChecked(true);
            notMeNav.setVisible(false);
        } else
            loadSideFragment(challengeFragment);
    }

    private void loadMainFragment(FragmentBase fragment, UserPartial owner) {
        closeRightDrawer();

        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();

        if(owner!=null && me.equals(owner)) {
            notMeNav.setVisible(false);
        } else {
            notMeNav.setVisible(true);
            selectedNav = notMeNav;
            notMeNav.setChecked(true);
            notMeNav.setTitle(fragment.getTitle(getResources()));
            if(fragment.getClass().equals(UserFragment.class))
                notMeNav.setIcon(android.R.drawable.ic_menu_edit); //TODO: icon for blog
            else
                notMeNav.setIcon(android.R.drawable.ic_menu_compass); //TODO: icon for shelf?
            //transaction.add(R.id.fragment, fragment, MAIN_FRAGMENT_TAG);
        }

        transaction.replace(R.id.fragment, fragment, MAIN_FRAGMENT_TAG); //TODO: do "add" one day
        //transaction.addToBackStack(owner.id());
        transaction.commit();
        ((Toolbar) findViewById(R.id.toolbar)).setTitle(fragment.getTitle(getResources()));
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

    public void setMainTitle() {
        String title = ((FragmentBase)getSupportFragmentManager().findFragmentById(R.id.fragment)).getTitle(getResources());
        //((Toolbar) findViewById(R.id.toolbar)).setTitle(title);
        getSupportActionBar().setTitle(title);
    }

    public void setSideTitle() {
        String title = ((FragmentBase)getSupportFragmentManager().findFragmentById(R.id.side_fragment)).getTitle(getResources());
        ((Toolbar) findViewById(R.id.side_toolbar)).setTitle(title);
    }


    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
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
