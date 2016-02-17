package com.qweex.openbooklikes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.qweex.openbooklikes.model.Search;
import com.qweex.openbooklikes.notmine.SimpleScannerActivity;

public class SearchFragment extends BookListFragment<Search> {

    EditText editText;
    protected Search searchTerm;

    @Override
    public String getTitle(Resources res) {
        if(primary==null)
            return res.getString(R.string.search_books);
        else
            return primary.title();
    }

    @Override
    public void setArguments(Bundle a) {
        // Silence is golden
        searchTerm = new Search(a);
        Log.d("setArguments", primary.id());

        super.setArguments(a);
    }

    public void setSearchTerm(String q) {
        searchTerm = Search.create(q);
        primary = searchTerm;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        responseHandler = new SearchHandler(this);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View shelfView = super.onCreateView(inflater, null, savedInstanceState);
        shelfView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_search, null, false);
        layout.addView(shelfView);

        editText = ((EditText)layout.findViewById(R.id.edit_text));
        editText.setOnEditorActionListener(imeListener);

        changeWidget(listView);
        return layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(searchTerm!=null)
            performSearch();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        InputMethodManager mIMEMgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mIMEMgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        loadingManager.hide();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.removeItem(R.id.filter_status);
        menu.removeItem(R.id.filter_special);

        menu.add(Menu.NONE, R.id.option_barcode, Menu.NONE, R.string.barcode)
                .setIcon(android.R.drawable.ic_menu_add)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.option_barcode) {
            Intent i = new Intent(SearchFragment.this.getActivity(), SimpleScannerActivity.class);
            SearchFragment.this.startActivityForResult(i, 1);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != Activity.RESULT_OK)
            return;
        String ISBN = data.getStringExtra("barcode");
        searchTerm = Search.create(ISBN);
        performSearch();
    }

    @Override
    public boolean fetchMore(int page) {
        // This will not execute all of BookList's fetchMore; just FetchFragmentBase's part
        if(!super.fetchMore(page))
            return false;
        RequestParams params = new ApiClient.PagedParams(page, adapter);
        params.put("q", primary.getS("q"));
        if(primary.getS("lng")!=null)
            params.put("lng", primary.getS("lng"));

        ApiClient.get(params, responseHandler);
        return true;
    }

    TextView.OnEditorActionListener imeListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            String s = textView.getText().toString().trim();
            if (i == EditorInfo.IME_ACTION_SEARCH && s.length() > 0) {
                primary = Search.create(s);
                InputMethodManager mIMEMgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                mIMEMgr.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                performSearch();

                return true;
            }
            return false;
        }
    };

    void performSearch() {
        getMainActivity().setMainTitle();
        loadingManager.show();
        editText.clearFocus();
        editText.setText(searchTerm.getS("q"));
        reload();
    }

    class SearchHandler extends BookHandler {
        public SearchHandler(FragmentBase f) {
            super(f);
        }

        @Override
        protected String urlPath() {
            return "book/SearchBooks";
        }

        @Override
        protected String countFieldName() {
            return "book_count";
        }
    }
}
