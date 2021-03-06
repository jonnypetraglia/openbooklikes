package com.qweex.openbooklikes.fragment;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.qweex.openbooklikes.ApiClient;
import com.qweex.openbooklikes.LoadingViewManager;
import com.qweex.openbooklikes.R;
import com.qweex.openbooklikes.SettingsManager;
import com.qweex.openbooklikes.activity.MainActivity;
import com.qweex.openbooklikes.handler.ShelvesHandler;
import com.qweex.openbooklikes.handler.UserHandler;
import com.qweex.openbooklikes.model.Me;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class LoginFragment extends FragmentBase {
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private OnLoginListener onLoginListener;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, null);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) v.findViewById(R.id.email);

        mPasswordView = (EditText) v.findViewById(R.id.password);
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

        ViewGroup loadingView = (ViewGroup) inflater.inflate(R.layout.loading, null);
        View emptyView = inflater.inflate(R.layout.empty, null),
                errorView = inflater.inflate(R.layout.error, null);
        ((TextView)loadingView.findViewById(R.id.progress_text)).setText(R.string.signing_in);

        loadingManager.setInitial(loadingView, v, emptyView, errorView);
        loadingManager.changeState(LoadingViewManager.State.INITIAL);
        loadingManager.content();


        errorView.findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.me == null)
                    attemptLogin();
                else
                    startApp();
            }
        });

        Button mEmailSignInButton = (Button) v.findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        return loadingManager.wrapInitialInLayout(getActivity());
    }


    public void attemptLogin() {
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
            loadingManager.show();
            RequestParams urlParams = new RequestParams();
            urlParams.put("email", mEmailView.getText().toString());
            urlParams.put("password", mPasswordView.getText().toString());

            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mPasswordView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

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
            loadingManager.show();
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            // If the response is JSONObject instead of expected JSONArray
            try {
                if (response.getInt("status") != 0 || statusCode >= 400)
                    throw new JSONException(response.getString("message"));

                mEmailView.setError(null);
                mPasswordView.setError(null);
                MainActivity.me = new Me(response, getActivity());
                startApp();
            } catch (JSONException e) {
                e.printStackTrace();
                loadingManager.content();
                mPasswordView.setError(statusCode + ": " + e.getMessage());
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
            loadingManager.error(error);
            mPasswordView.setError("Error " + statusCode + " " + error.getMessage());
        }
    };

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    @Override
    public String getTitle(Resources r) {
        return r.getString(R.string.app_name);
    }





    public void startApp() {
        Log.d("OBL", "startApp");
        loadingManager.show("Fetching user data");
        boolean forceFetch = SettingsManager.userInfoExpired(getActivity());
        if(forceFetch) {
            RequestParams params = new RequestParams();
            ApiClient.get(params, new UserHandler(loadingManager, getActivity()) {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    // Parent class(es) show content because they assume it's what we want
                    //   in this case content is the login form, so it needs to stay hidden
                    //FIXME: Probably don't need both hide & show & two changeStates
                    Log.d("Success login", "WEEEEEE");
                    loadingManager.show();
                    loadingManager.changeState(LoadingViewManager.State.INITIAL);
                    loadingManager.show();
                    loadingManager.changeState(LoadingViewManager.State.INITIAL);

                    fetchShelves(true);
                }
            });
        } else
            fetchShelves(false);
    }

    private void fetchShelves(boolean force) {
        if(MainActivity.shelves.size()>1 && !force) {
            onLoginListener.onLogin();
            return;
        }
        ApiClient.get(new ShelvesHandler(loadingManager, MainActivity.shelves = new ArrayList<>(), MainActivity.me){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                loadingManager.show();
                loadingManager.changeState(LoadingViewManager.State.INITIAL);
                loadingManager.show();
                loadingManager.changeState(LoadingViewManager.State.INITIAL);

                try {
                    SettingsManager.saveShelves(
                            MainActivity.shelves = SettingsManager.mergeShelves(
                                    SettingsManager.loadShelves(getActivity()), shelves
                            ), getActivity()
                    );
                    Log.d("Saved shelves", "WEEEEEE");
                    onLoginListener.onLogin();
                } catch (JSONException e) {
                    e.printStackTrace();
                    loadingManager.error(e);
                }
            }
        });
    }

    public void setOnLoginListener(OnLoginListener onLoginListener) {
        this.onLoginListener = onLoginListener;
    }

    public interface OnLoginListener {
        void onLogin();
    }
}
