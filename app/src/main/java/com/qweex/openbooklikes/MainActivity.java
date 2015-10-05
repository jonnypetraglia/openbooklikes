package com.qweex.openbooklikes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static Me user;
    public static ImageLoader imageLoader;
    public static ArrayList<Shelf> shelves = new ArrayList<>();

    private ArrayList<MenuItem> shelfMenuItems = new ArrayList<>();
    public Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(user==null) {
            finish();
            return;
        }
        Log.d("OBL:MASTER", "token: " + user.token);

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        // Load user info
        ((TextView)findViewById(R.id.user_username)).setText(user.username);
        ((TextView)findViewById(R.id.user_email)).setText(user.email);
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
        imageLoader.displayImage(user.photo, (ImageView) findViewById(R.id.user_pic));


        // Add shelfMap to menu
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        Menu shelfNav = navView.getMenu().findItem(R.id.nav_shelves).getSubMenu();
        shelfNav.clear();
        for(Shelf s : shelves) {
            MenuItem mitem = shelfNav.add(R.id.nav_group,
                    s.id.equals("-1") ? R.id.nav_all_shelf : R.id.nav_shelf,
                    0,
                    s.name + " (" + s.book_count + ")")
                .setCheckable(true)
                .setIcon(android.R.drawable.ic_menu_compass); //TODO: Icon
            shelfMenuItems.add(mitem);
        }

        // Select default fragment
        MenuItem startItem = shelfNav.findItem(R.id.nav_all_shelf);
        //startItem.setChecked(true);
        onNavigationItemSelected(startItem);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.action_logout:
                SharedPreferences.Editor prefs = getSharedPreferences(LoginActivity.USER_DATA_PREFS, MODE_PRIVATE).edit();
                prefs.clear();
                prefs.apply();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                MainActivity.this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch(id) {
            case R.id.nav_all_shelf:
            case R.id.nav_shelf:
                Log.d("OBL:nav_shelf", shelves.get(shelfMenuItems.indexOf(item)) .name);
                fetchShelves(shelfMenuItems.indexOf(item));
                break;
            case R.id.nav_blog:
            //TODO: Special & Status shelfMap
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void fetchShelves(int shelfIndex) {
        Log.d("OBL", "fetchShelves");

        Bundle b = new Bundle();
        b.putInt("shelfIndex", shelfIndex);
        if(!shelves.get(shelfIndex).id.equals("-1"))
            b.putString("Cat", shelves.get(shelfIndex).id);
        //b.putBoolean("BookisWish", false);
        //b.putBoolean("Favorite", false);
        //b.putString("BookStatus", "read|currently|planning");
        //b.putString("BookUserRating", "0.5");

        ShelfFragment shelfFragment = new ShelfFragment();
        shelfFragment.setShelf(this, shelves.get(shelfIndex));
        shelfFragment.setArguments(b);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, shelfFragment).commit();
    }


    // user/GetUserFollowers user/GetUserFollowings
    //   user/GetUserInfo

    // GET book/SetPageCurrent

    // book/SearchBooks

    // GET book/AddBookToShelf
    // user/AddUserCategory

    // post/GetUserPost

    // POST post/PostCreate
    // GET post/PostDelete

    // GET user/register

    /* Reading Challenge
        http://booklikes.com/apps/reading-challenge/69841
            $$('.challenge.container nav')
            finds two nav's, both of which contain:
                <a class="nav-active" href="http://booklikes.com/apps/reading-challenge/69841/2015">2015</a>


        http://booklikes.com/apps/reading-challenge/69841/2015
        http://booklikes.com/widget/readingchallenge?id=69841&year=2015
            progress: .info > span:nth-of-type(1)
            total: .info > span:nth-of-type(2)
     */

    // http://stackoverflow.com/questions/22096324/cache-data-locally-waiting-for-an-internet-connection-in-android
    // https://www.google.com/search?q=android+cache+data+from+server&oq=android+cache+data+from+server&gs_l=serp.3..0.8255.13634.0.13739.35.21.2.8.8.0.131.2145.2j18.20.0....0...1c.1.64.serp..6.29.2050.LqumgIv21mY
}
