package com.qweex.openbooklikes;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.qweex.openbooklikes.model.User;

public class MainActivity extends AppCompatActivity {
    public static User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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


        if(user==null) {
            finish();
            return;
        }
        Log.d("OBL:MASTER", user.token);
    }





    // user/GetUserFollowers
    //   user/GetUserInfo
    // user/GetUserFollowings
    // book/GetUserBooks

    // GET book/SetPageCurrent

    // book/SearchBooks

    // GET book/AddBookToShelf
    // user/AddUserCategory

    // post/GetUserPost

    // POST post/PostCreate
    // GET post/PostDelete

    // GET user/register

    /*
        Special shelves:
          - Favorite
          - Wishlist
          - Reviewed
          - Private
        Status:
          - Read
          - Planned
          - Currently
     */

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
}
