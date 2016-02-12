package com.qweex.openbooklikes;


import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class LoadingViewManager {

    private class LoadingView {
        ViewGroup loadingView;
        View contentView, emptyView, errorView;

        public LoadingView(ViewGroup l, View c, View e, View x) {
            loadingView = l;
            contentView = c;
            emptyView = e;
            errorView = x;
        }

        public void loading() { loading(null); }

        public void loading(String text) {
            loadingView.setVisibility(View.VISIBLE);
            if(text!=null)
                ((TextView)loadingView.findViewById(R.id.progress_text)).setText(text);
            if(currentState==State.INITIAL) {
                contentView.setVisibility(View.GONE);
                emptyView.setVisibility(View.GONE);
            }
        }

        public void content() {
            Log.d("showing", "content");
            contentView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            if(currentState==State.INITIAL)
                loadingView.setVisibility(View.GONE);
            errorView.setVisibility(View.GONE);
        }

        public void empty() {
            emptyView.setVisibility(View.VISIBLE);
            loadingView.setVisibility(View.GONE);
            contentView.setVisibility(View.GONE);
            errorView.setVisibility(View.GONE);
        }

        public void error(String error) {
            errorView.setVisibility(View.VISIBLE);
            ((TextView)errorView.findViewById(R.id.title)).setText(error);
            emptyView.setVisibility(View.GONE);
            loadingView.setVisibility(View.GONE);
            contentView.setVisibility(View.GONE);
        }
    }


    public enum State {
        INITIAL,
        MORE
    }
    State currentState;
    LoadingView initial;
    ArrayList<LoadingView> mores = new ArrayList<>();

    public LoadingViewManager() {
        currentState = State.INITIAL;
    }

    public void setInitial(ViewGroup loadingView, View contentView, View emptyView, View errorView) {
        initial = new LoadingView(loadingView, contentView, emptyView, errorView);
    }

    public void changeState(State s) {
        //initial.content(); //FIXME: why is this here??? ANS: to attempt to show the content when going from INITIAL to MORE
        //content();
        currentState = s;
        //content();
    }

    public void addMore(ViewGroup loadingView, View contentView, View emptyView, View errorView) {
        mores.add(new LoadingView(loadingView, contentView, emptyView, errorView));
    }


    public void show(String loadingText) {
        Log.d("LoadingManager", "Showing loading " + (currentState==State.MORE ? "more" : "Init") );
        if(currentState==State.MORE)
            for(LoadingView v : mores)
                v.loading(loadingText);
        else
            initial.loading(loadingText);
    }

    public void show() { show(null); }

    public void content() {
        Log.d("LoadingManager", "Showing content "  + (currentState==State.MORE ? "more" : "Init") );
        if(currentState==State.MORE)
            for(LoadingView v : mores)
                v.content();
        else
            initial.content();
    }

    public void empty() {
        Log.d("LoadingManager", "Showing empty " + (currentState==State.MORE ? "more" : "Init") );
        if(currentState==State.MORE)
            for(LoadingView v : mores)
                v.empty();
        else
            initial.empty();
    }

    public void error(Throwable err) {
        if(currentState==State.MORE)
            for(LoadingView v : mores)
                v.error(err.getMessage());
        else
            initial.error(err.getMessage());
        Log.e("OBL", "LoadingViewManager error");
        err.printStackTrace();
    }
}
