package com.qweex.openbooklikes;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.qweex.openbooklikes.model.Shelf;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * A placeholder fragment containing a simple view.
 */
public class ShelfFragment extends Fragment {

    ArrayAdapter spinnerAdapter;

    public ShelfFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_main, container, false);
        return contentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((TextView) view.findViewById(R.id.username)).setText(MainActivity.user.username);

        ApiClient.get("user/GetUserCategories", ShelfHandler);



        ArrayList spinnerlist = new ArrayList<String>();
        spinnerlist.add("All");
        spinnerlist.add("Shelf 1");


        spinnerAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_dropdown_item_1line, spinnerlist);
        Spinner shelfSpinner = new Spinner(getActivity());
        shelfSpinner.setAdapter(spinnerAdapter);
        shelfSpinner.setOnItemSelectedListener(changeShelf);

        spinnerAdapter.add("All Again");

        ((AppCompatActivity) getActivity()).getSupportActionBar().setCustomView(shelfSpinner);

        ApiClient.post("user/GetUserCategories", ShelfHandler);
    }

    private AdapterView.OnItemSelectedListener changeShelf = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Log.d("OBL", "Change to " + spinnerAdapter.getItem(position));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            Log.d("OBL", "Nothing selected");
        }
    };

    private JsonHttpResponseHandler ShelfHandler = new JsonHttpResponseHandler() {

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
            try {
                for(int i=0; i<response.length(); i++) {
                    Shelf s = new Shelf(response.getJSONObject(i));
                    Log.d("OBL:Cat", s.name + " (" + s.book_count + ")");
                    spinnerAdapter.add(s.name + " (" + s.book_count + ")");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody)
        {
            Log.e("OBL:Cat", "Failed cause " + error.getMessage());
        }
    };
}
