package com.qweex.openbooklikes;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class LoadingViewManagerDialog implements LoadingViewInterface {
    ProgressDialog dialog;
    String successMsg;
    View fragment;

    public LoadingViewManagerDialog(View f, int i) {
        this(f, f.getContext().getResources().getString(i));
    }

    public LoadingViewManagerDialog(View f, String s) {
        this.fragment = f;
        this.successMsg = s;
        dialog = new ProgressDialog(fragment.getContext());
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
    }



    @Override
    public void show(String loadingText) {
        dialog.setTitle(loadingText);
        show();
    }

    @Override
    public void show() {
        dialog.show();
    }

    @Override
    public void content() {
        dialog.dismiss();
        Snackbar.make(fragment, successMsg, Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void empty() {
        dialog.dismiss();
    }

    @Override
    public void error(Throwable err) {
        dialog.dismiss();
        Snackbar snack = Snackbar.make(fragment, err.getMessage(), Snackbar.LENGTH_LONG);
        ((TextView)snack.getView().findViewById(android.support.design.R.id.snackbar_text))
                .setTextColor(Color.RED);
        snack.show();
    }

    @Override
    public void hide() {
        dialog.dismiss();
    }

    @Override
    public void changeState(State s) {
    }
}
