package com.qweex.openbooklikes.fragment;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.klinker.android.link_builder.LinkConsumableTextView;
import com.qweex.linkspan.LinkSpan;
import com.qweex.openbooklikes.ApiClient;
import com.qweex.openbooklikes.LoadingViewManager;
import com.qweex.openbooklikes.R;
import com.qweex.openbooklikes.Titleable;
import com.qweex.openbooklikes.activity.MainActivity;
import com.qweex.openbooklikes.model.ModelBase;
import com.qweex.openbooklikes.model.Shareable;
import com.qweex.openbooklikes.notmine.Misc;


abstract public class FragmentBase<Primary extends ModelBase> extends Fragment implements Toolbar.OnMenuItemClickListener, Titleable,
        LinkSpan.OnLinkClickListener, LinkSpan.OnLinkLongClickListener{
    Primary primary;
    ApiClient.ApiResponseHandler responseHandler;
    LoadingViewManager loadingManager = new LoadingViewManager();

    public static void optionIcon(MenuItem mi) {
        if(mi.getIcon()!=null) {
            if(mi.getIcon() instanceof VectorDrawable) {
                int dp = Misc.convertDpToPixel(32);
                mi.setIcon(Misc.resizeDrawable((VectorDrawable) mi.getIcon(), dp, dp));
            }
            mi.getIcon().setColorFilter(0xffffffff, PorterDuff.Mode.SRC_ATOP);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(primary!=null)
            primary.wrapInBundle(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState!=null) {
            setArguments(savedInstanceState);
            Log.d("oac:" + getClass().getSimpleName(), "Saved data: " + primary.apiName());
         } else {
            Log.d("oac:" + getClass().getSimpleName(), "No saved, fetching data");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false); // Children should set this if they have one
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        MenuItem mi;
        //getActivity().onCreateOptionsMenu(menu);
        if(primary instanceof  Shareable || this instanceof Shareable) {
            mi = menu.add(Menu.NONE, R.id.option_share, Menu.NONE, R.string.option_share)
                    .setIcon(R.drawable.share_np48841);
            optionIcon(mi);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.option_share) {
            startActivity(Intent.createChooser(
                    ((Shareable) (primary instanceof Shareable ? primary : this))
                            .share(), getResources().getString(R.string.option_share)));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected TextView setOrHide(View container, int tvId, String text) {
        TextView tv = ((TextView)container.findViewById(tvId));
        ModelBase.unHTML(tv, text);
        if(tv instanceof LinkConsumableTextView)
            LinkSpan.replaceURLSpans(tv, this, this);
        tv.setVisibility(tv.getText() == null || tv.getText().length()==0 ? View.GONE : View.VISIBLE);
        return tv;
    }

    protected MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }


    protected View createProgressView(LayoutInflater inflater, ViewGroup container, View childView) {

        View loadingView = inflater.inflate(R.layout.loading, null),
             emptyView = inflater.inflate(R.layout.empty, null),
             errorView = inflater.inflate(R.layout.error, null);

        loadingManager.setInitial(loadingView, childView, emptyView, errorView);
        loadingManager.changeState(LoadingViewManager.State.INITIAL);
        loadingManager.content();

        return loadingManager.wrapInitialInLayout(getContext());
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return onOptionsItemSelected(item);
    }

    final protected float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    public LoadingViewManager getLoadingManager() { return loadingManager; }


    @Override
    public void onClick(LinkSpan linkSpan, String label, String link, LinkConsumableTextView textView) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(browserIntent);
    }

    @Override
    public void onLongClick(final LinkSpan linkSpan, final String label, final String link, final LinkConsumableTextView textView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(link);
        builder.setItems(R.array.click_link, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Object choice = ((AlertDialog)dialog).getListView().getAdapter().getItem(which);

                switch(which) { //heheheheheheh
                    default:
                    case 0: // Open in Browser
                        FragmentBase.this.onClick(linkSpan, label, link, textView);
                        break;
                    case 1: // Copy to clipboard
                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Activity.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(label, link);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(getActivity(), R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
                        break;
                    case 2: // Share
                        Intent intent = new Intent(android.content.Intent.ACTION_SEND)
                                .setType("text/plain")
                                .putExtra(Intent.EXTRA_TEXT, link);
                        startActivity(Intent.createChooser(intent, getResources().getString(R.string.option_share)));
                        break;
                }
            }
        });
        builder.show();
    }


    void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    void showKeyboard(Window w,View v) {
        w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        if(v!=null)
            v.requestFocus();
    }
}
