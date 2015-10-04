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

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.qweex.openbooklikes.model.Book;
import com.qweex.openbooklikes.model.Me;
import com.qweex.openbooklikes.model.Shelf;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static Me user;
    public Menu shelfNav;
    private Map<MenuItem, Shelf> shelves = new HashMap<MenuItem, Shelf>();
    private Map<Shelf, ArrayList<Book>> shelfBooks = new HashMap<Shelf, ArrayList<Book>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(user==null) {
            finish();
            return;
        }
        Log.d("OBL:MASTER", "token: " + user.token);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
        ((ImageView)findViewById(R.id.user_pic)).setImageBitmap(user.bitmap);

        // Set up nav menu
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        shelfNav = navView.getMenu().findItem(R.id.nav_shelves).getSubMenu();

        ApiClient.get("user/GetUserCategories", ShelvesHandler);
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
            case R.id.nav_shelf:
                Log.d("OBL:nav_shelf", shelves.get(item).name);
                fetchShelves(shelves.get(item));
                break;
            case R.id.nav_all_shelf:
            case R.id.nav_blog:
            //TODO: Special & Status shelves
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void fetchShelves(Shelf shelf) {
        RequestParams params = new RequestParams();
        params.put("Cat", shelf.id);
        params.put("PerPage", 25); //DEBUG
        //params.put("BookIsWish", "0");
        //params.put("Favorite", "0");
        //params.put("BookStatus", "read|currently|planning");
        ////params.put("BookUserRating", "0.5");
        ApiClient.get("book/GetUserBooks", params, new ShelfHandler(shelf));
    }

    class ShelfHandler extends JsonHttpResponseHandler {
        Shelf shelf;
        JSONArray books;
        int iter = 0;

        public ShelfHandler(Shelf s) {
            this.shelf = s;
        }

        AndThen createBook = new AndThen() {
            @Override
            public void call(Object o) {
                try {
                    if(iter>=books.length()) {
                        complete();
                        return;
                    }
                    Book b = new Book(books.getJSONObject(iter++), this);
                    Log.d("OBL:book", "Book: " + b.title);
                    shelfBooks.get(shelf).add(0, b);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        private void complete() {
            ShelfFragment shelfFragment = new ShelfFragment();
            shelfFragment.shelf = shelf;
            shelfFragment.books = shelfBooks.get(shelf);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, shelfFragment).commit();
        }


        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            Log.d("OBL:book.", "Success " + response.length());

            try {
                if (response.getInt("status") != 0 || statusCode >= 400)
                    throw new JSONException(response.getString("message"));
                books = response.getJSONArray("books");
                createBook.call(null);
            } catch (JSONException e) {
                Log.e("OBL:Book!", "Failed cause " + e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
            Log.e("OBL:Cat", "Failed cause " + error.getMessage());
        }
    };

    private JsonHttpResponseHandler ShelvesHandler = new JsonHttpResponseHandler() {

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            Log.d("OBL:cat.", "Success " + response.length());

            try {
                if(response.getInt("status")!=0 || statusCode >= 400)
                    throw new JSONException(response.getString("message"));
                JSONArray categories = response.getJSONArray("categories");
                shelfNav.add(R.id.nav_group, R.id.nav_all_shelf, 0, "All books").setCheckable(true);
                for(int i=0; i<categories.length(); i++) {
                    Shelf s = new Shelf(categories.getJSONObject(i));
                    Log.d("OBL:Cat", s.name);
                    MenuItem item = shelfNav.add(R.id.nav_group, R.id.nav_shelf, 0, s.name + " (" + s.book_count + ")")
                            .setCheckable(true)
                            .setIcon(android.R.drawable.ic_menu_compass); //TODO: Icon
                    shelves.put(item, s);
                    shelfBooks.put(s, new ArrayList<Book>());
                }
            } catch (JSONException e) {
                Log.e("OBL:Cat!", "Failed cause " + e.getMessage());
                e.printStackTrace();
            }
        }
        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
            Log.e("OBL:Cat", "Failed cause " + error.getMessage());
        }
    };


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
