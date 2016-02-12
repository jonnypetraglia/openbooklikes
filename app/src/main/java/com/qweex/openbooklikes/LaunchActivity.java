package com.qweex.openbooklikes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.qweex.openbooklikes.model.Me;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class LaunchActivity extends AppCompatActivity {

    public LoadingViewManager loadingManager = new LoadingViewManager();
    LoginFragment loginForm = new LoginFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApiClient.setApiKey(getResources());
        FrameLayout f = new FrameLayout(this);
        f.setId(R.id.fragment);

        ViewGroup loadingView = (ViewGroup) getLayoutInflater().inflate(R.layout.loading, null);
        View emptyView = getLayoutInflater().inflate(R.layout.empty, null),
             errorView = getLayoutInflater().inflate(R.layout.error, null);

        loadingManager.setInitial(loadingView, f, emptyView, errorView);
        loadingManager.changeState(LoadingViewManager.State.INITIAL);
        loadingManager.content();

        LinearLayout layout = new LinearLayout(this);
        layout.addView(loadingView);
        layout.addView(emptyView);
        layout.addView(errorView);
        layout.addView(f);
        f.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        f.setBackgroundColor(0xff99cc00);

        errorView.findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginForm.attemptLogin();
            }
        });

        setContentView(layout);

//        if(savedInstanceState==null)
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment, loginForm)
                .commit();


        try {
            MainActivity.me = Me.fromPrefs(this);
            if (MainActivity.me != null)
                startApp();
        } catch (JSONException e) {
            //TODO: Show Error
            e.printStackTrace();
            loadingManager.error(e);
        }
    }


    public void startApp() {
        Log.d("OBL", "startApp");
        loadingManager.show();
        ApiClient.get(new ShelvesHandler(loadingManager, MainActivity.shelves, MainActivity.me){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("OBL", "Shelves " + shelves.size());
                Intent i = new Intent(LaunchActivity.this, MainActivity.class);
                LaunchActivity.this.startActivity(i);
                LaunchActivity.this.finish();
            }

            //TODO: onError
        });
    }
}

