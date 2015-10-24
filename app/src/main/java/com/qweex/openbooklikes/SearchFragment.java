package com.qweex.openbooklikes;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.qweex.openbooklikes.model.Search;

public class SearchFragment extends ShelfFragment<Search> {

    @Override
    String getTitle() {
        if(primary==null)
            return "Search Books";
        else
            return primary.title();
    }

    @Override
    public void setArguments(Bundle a) {
        // Silence is golden
        primary = new Search(a);
        Log.d("setArguments", primary.id());
        setArguments(a);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View shelfView = super.onCreateView(inflater, null, savedInstanceState);
        shelfView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        shelfView.findViewById(R.id.shelf_views).setVisibility(View.GONE);

        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_search, null, false);
        layout.addView(shelfView);

        ((EditText)layout.findViewById(R.id.editText)).setOnEditorActionListener(performSearch);

        return layout;
    }

    @Override
    public boolean fetchMore(int page) {
        // Don't call Super
        if(adapter.noMore())
            return false;
        RequestParams params = new ApiClient.PagedParams(page, adapter);
        params.put("q", primary.getS("q"));
        if(primary.getS("lng")!=null)
            params.put("lng", primary.getS("lng"));

        ApiClient.get(params, searchHandler);
        return true;
    }

    TextView.OnEditorActionListener performSearch = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            String s = textView.getText().toString().trim();
            if (i == EditorInfo.IME_ACTION_SEARCH && s.length() > 0) {
                adapter.clear();
                primary = Search.create(s);
                getMainActivity().setMainTitle();
                showLoading();
                textView.clearFocus();
                fetchMore(0);
                return true;
            }
            return false;
        }
    };


    SearchHandler searchHandler = new SearchHandler();
    class SearchHandler extends BookHandler {
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
