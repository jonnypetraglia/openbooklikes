package com.qweex.openbooklikes;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.qweex.openbooklikes.model.Me;
import com.qweex.openbooklikes.model.Shelf;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class LaunchActivity extends AppCompatActivity {
    public static final String USER_DATA_PREFS = "UserData";

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);


        SharedPreferences prefs = getSharedPreferences(USER_DATA_PREFS, MODE_PRIVATE);
        Log.d("OBL:LOGIN", prefs.getString("usr_token", "NULL"));
        if(prefs.getString("usr_token", null)!=null) {
            MainActivity.me = new Me(prefs);
            startApp();
        }
    }


    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            RequestParams urlParams = new RequestParams();
            urlParams.put("email", mEmailView.getText().toString());
            urlParams.put("password", mPasswordView.getText().toString());

            ApiClient.post("user/login", urlParams, loginHandler);
        }
    }

    private void startApp() {
        Log.d("OBL", "startApp");
        showProgress(true);
        ApiClient.get("user/GetUserCategories", shelvesHandler);
    }

    private JsonHttpResponseHandler loginHandler = new JsonHttpResponseHandler() {

        @Override
        public void onStart() {
            showProgress(true);
        }
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            // If the response is JSONObject instead of expected JSONArray
            try {
                if(response.getInt("status")!=0 || statusCode >= 400)
                    throw new JSONException(response.getString("message"));

                mEmailView.setError(null);
                mPasswordView.setError(null);
                MainActivity.me = new Me(response);
                if(MainActivity.me.token!=null)
                    saveMe();
                startApp();
            } catch (JSONException e) {
                e.printStackTrace();
                showProgress(false);
                mPasswordView.setError(statusCode + ": " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                showProgress(false);
                mPasswordView.setError("!!!: " + e.getMessage());
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody)
        {
            showProgress(false);
            mPasswordView.setError("Error " + statusCode + " " + error.getMessage());
        }
    };

    private JsonHttpResponseHandler shelvesHandler = new JsonHttpResponseHandler(){

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            Log.d("OBL:cat.", "Success " + response.length());

            try {
                if (response.getInt("status") != 0 || statusCode >= 400)
                    throw new JSONException(response.getString("message"));
                JSONArray categories = response.getJSONArray("categories");

                //MenuItem item = shelfNav.add(R.id.nav_group, R.id.nav_all_shelf, 0, "All books").setCheckable(true);
                JSONObject allBooks = new JSONObject();
                allBooks.put("id_category", "-1");
                allBooks.put("id_user", MainActivity.me.id);
                allBooks.put("category_name", "All books");
                allBooks.put("category_book_count", MainActivity.me.book_count);
                Shelf s = new Shelf(allBooks);
                MainActivity.shelves.add(s);

                for (int i = 0; i < categories.length(); i++) {
                    s = new Shelf(categories.getJSONObject(i));
                    MainActivity.shelves.add(s);
                    Log.d("OBL:Cat", s.name);
                }

                Intent i = new Intent(LaunchActivity.this, MainActivity.class);
                LaunchActivity.this.startActivity(i);
                LaunchActivity.this.finish();
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

    private void saveMe() {
        SharedPreferences.Editor prefs = getSharedPreferences(USER_DATA_PREFS, MODE_PRIVATE).edit();
        prefs.putString("id_user", MainActivity.me.id);
        prefs.putString("usr_username", MainActivity.me.username);
        prefs.putString("usr_domain", MainActivity.me.domain);
        prefs.putString("usr_photo", MainActivity.me.photo);

        prefs.putString("usr_email", MainActivity.me.email);
        prefs.putString("usr_blog_title", MainActivity.me.blog_title);
        prefs.putString("usr_blog_desc", MainActivity.me.blog_desc);
        prefs.putString("usr_following_count", MainActivity.me.following_count);
        prefs.putString("usr_followed_count", MainActivity.me.followed_count);
        prefs.putInt("usr_book_count", MainActivity.me.book_count);

        prefs.putString("usr_token", MainActivity.me.token);
        Log.d("OBL:saveAsMe", MainActivity.me.token);
        prefs.apply();
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}

