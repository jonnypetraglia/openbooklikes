package com.qweex.openbooklikes;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.qweex.openbooklikes.model.Book;
import com.qweex.openbooklikes.model.Search;

import java.util.ArrayList;

public class SearchFragment extends BookListFragment<Search> {

    EditText editText;

    @Override
    String getTitle() {
        if(primary==null)
            return "Search Books"; //TODO: getResources()
        else
            return primary.title();
    }

    @Override
    public void setArguments(Bundle a) {
        // Silence is golden
        primary = new Search(a);
        Log.d("setArguments", primary.id());
        super.setArguments(a);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View shelfView = super.onCreateView(inflater, null, savedInstanceState);
        shelfView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_search, null, false);
        layout.addView(shelfView);

        editText = ((EditText)layout.findViewById(R.id.edit_text));
        editText.setOnEditorActionListener(performSearch);

        changeWidget(listView);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        InputMethodManager mIMEMgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mIMEMgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
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

        ApiClient.get(params, searchHandler);
        return true;
    }

    TextView.OnEditorActionListener performSearch = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            String s = textView.getText().toString().trim();
            if (i == EditorInfo.IME_ACTION_SEARCH && s.length() > 0) {
                primary = Search.create(s);
                getMainActivity().setMainTitle();
                showLoading();
                textView.clearFocus();

                InputMethodManager mIMEMgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                mIMEMgr.hideSoftInputFromWindow(textView.getWindowToken(), 0);

                adapter.clear();
                fetchMore(0); // FIXME: Will EndlessScrollView call this once adapter is cleared?
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
