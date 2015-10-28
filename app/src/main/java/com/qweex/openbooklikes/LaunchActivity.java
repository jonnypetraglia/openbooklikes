package com.qweex.openbooklikes;

import android.content.Intent;
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
import android.widget.FrameLayout;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.qweex.openbooklikes.model.Me;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApiClient.setApiKey(getResources());
        FrameLayout f = new FrameLayout(this);
        f.setId(R.id.fragment);

        setContentView(f);


        try {
            MainActivity.me = Me.fromPrefs(this);
            if (MainActivity.me != null)
                startApp();
            return;
        } catch (JSONException e) {
            //TODO: Show Error
            e.printStackTrace();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(f.getId(), new LoginForm())
                .commit();
    }

    class LoginForm extends FragmentBase {
        // UI references.
        private AutoCompleteTextView mEmailView;
        private EditText mPasswordView;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_login);

            // Set up the login form.
            mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

            mPasswordView = (EditText) findViewById(R.id.password);
            mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                    if (id == R.id.ime_login || id == EditorInfo.IME_NULL) {
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

            // Check for a valid password, if the primary entered one.
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
                // perform the primary login attempt.
                showLoading();
                RequestParams urlParams = new RequestParams();
                urlParams.put("email", mEmailView.getText().toString());
                urlParams.put("password", mPasswordView.getText().toString());

                ApiClient.post(urlParams, loginHandler);
            }
        }

        private ApiClient.ApiResponseHandler loginHandler = new ApiClient.ApiResponseHandler() {

            @Override
            protected String urlPath() {
                return "user/login";
            }

            @Override
            protected String countFieldName() {
                return null; // No count
            }

            @Override
            public void onStart() {
                showLoading();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                try {
                    if (response.getInt("status") != 0 || statusCode >= 400)
                        throw new JSONException(response.getString("message"));

                    mEmailView.setError(null);
                    mPasswordView.setError(null);
                    MainActivity.me = new Me(response, LaunchActivity.this);
                    startApp();
                } catch (JSONException e) {
                    e.printStackTrace();
                    showContent();
                    mPasswordView.setError(statusCode + ": " + e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
                showContent();
                mPasswordView.setError("Error " + statusCode + " " + error.getMessage());
            }
        };

        private boolean isEmailValid(String email) {
            //TODO: Replace this with your own logic
            return email.contains("@");
        }

        @Override
        String getTitle() {
            return null; // Not needed
        }
    }



    private void startApp() {
        Log.d("OBL", "startApp");
        ApiClient.get(new ShelvesHandler(MainActivity.shelves, MainActivity.me){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("OBL", "Shelves " + shelves.size());
                Intent i = new Intent(LaunchActivity.this, MainActivity.class);
                LaunchActivity.this.startActivity(i);
                LaunchActivity.this.finish();
            }
        });
    }
}

