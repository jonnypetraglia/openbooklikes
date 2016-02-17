package com.qweex.openbooklikes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.support.design.widget.Snackbar;
import android.view.View;

public class LoadingViewManagerDialog extends LoadingViewManager {
    ProgressDialog dialog;
    String successMsg;
    View fragment;

    public LoadingViewManagerDialog(View f, int i) {
        this(f, f.getContext().getResources().getString(i));
    }

    public LoadingViewManagerDialog(View f, String s) {
        dialog = ProgressDialog.show(f.getContext(), "Reloading", null, true, false);
        this.fragment = f;
        this.successMsg = s;
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
        Snackbar.make(fragment, err.getMessage(), Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void hide() {
        dialog.dismiss();
    }
}
