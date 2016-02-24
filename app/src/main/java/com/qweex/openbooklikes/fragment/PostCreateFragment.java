package com.qweex.openbooklikes.fragment;


import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.qweex.openbooklikes.ApiClient;
import com.qweex.openbooklikes.R;
import com.qweex.openbooklikes.activity.MainActivity;
import com.qweex.openbooklikes.handler.LoadingResponseHandler;
import com.qweex.openbooklikes.model.Post;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import cz.msebera.android.httpclient.Header;

public class PostCreateFragment extends FragmentBase {

    Spinner type;
    EditText title, special, desc, source, url;
    CheckBox review;
    RatingBar rating;
    EditText date;


    RequestParams getParams() {
        RequestParams params = new RequestParams();
        params.put("PostType", type.getSelectedItem().toString());
        params.put("PostTitle", title.getText().toString());
//        params.put("spcial", special.getText().toString());
        params.put("PostText", special.getText().toString());
        if(source.getText().length()>0)
            params.put("PostSource", source.getText().toString());
        if(url.getText().length()>0)
            params.put("PostLinkUrl", url.getText().toString());
        params.put("PostIsReview", review.isChecked() ? "1" : "0");
        params.put("PostRevRating", new DecimalFormat("#.#").format(rating.getRating()));
//        params.put("date", date.getText().toString());
        //TODO: Tags, photos


        switch(type.getSelectedItemPosition()) {
            default:
            case 0: //text
                break;
            case 1: //quote
                params.put("PostQuoteText", special.getText().toString());
                break;
            case 2: //photo
                break;
            case 3: //video
                params.put("PostVideoUrl", special.getText().toString());
                break;
            case 4: //url
                //params.put("PostQuoteText", special.getText().toString());
                //FIXME: What is Special here???
                break;
        }

        return params;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.add(Menu.NONE, R.id.option_submit, Menu.NONE, "Submit")
                //TODO: Icon
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(Menu.NONE, R.id.option_submit, Menu.NONE, "Save")
                //TODO: Icon
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        RequestParams p = getParams();
        switch(item.getItemId()) {
            case R.id.option_submit:
                Log.d("WEEEE", p.toString());
                ApiClient.post(p, new LoadingResponseHandler(this) {
                    @Override
                    protected String urlPath() {
                        return "post/PostCreate";
                    }

                    @Override
                    protected String countFieldName() {
                        return null;
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        try {
                            Post post = new Post(response, MainActivity.me);
                            Bundle b = MainActivity.me.wrapInBundle(post.wrapInBundle(new Bundle()));

                            PostFragment postFragment = new PostFragment();
                            postFragment.setArguments(b);
                            getMainActivity().loadSideFragment(postFragment);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            loadingManager.error(e);
                        }
                    }
                });
                break;
            case R.id.option_save:
        }
        return super.onOptionsItemSelected(item);
    }

    class MuhAdapter extends ArrayAdapter<String> {

        public MuhAdapter(Context context, int resource, String[] objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = super.getView(position, convertView, parent);
            ((TextView)convertView).setTextColor(0xffffffff);
            Log.d("WEEE", (String) ((TextView)convertView).getText());
            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            convertView = super.getDropDownView(position, convertView, parent);
//            convertView.setBackgroundResource(android.R.drawable.list_selector_background);
            return convertView;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Toolbar tb = ((Toolbar)getMainActivity().findViewById(R.id.side_toolbar));
        tb.setTitle(null);

        Spinner navSpinner = (Spinner) tb.findViewById(R.id.spinner);
        String[] vals = getResources().getStringArray(R.array.post_types);
        for(int i=0; i<vals.length; i++)
            vals[i] = vals[i].toUpperCase();
        MuhAdapter adap = new MuhAdapter(
                getContext(), android.R.layout.simple_spinner_item,
                vals
        );
        adap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        navSpinner.setAdapter(adap);
        navSpinner.setVisibility(View.VISIBLE);
        navSpinner.setOnItemSelectedListener(changeType);


        View v = inflater.inflate(R.layout.fragment_post_new, null);

        type = (Spinner) v.findViewById(R.id.type);
        title = (EditText) v.findViewById(R.id.title);
        special = (EditText) v.findViewById(R.id.special);
        desc = (EditText) v.findViewById(R.id.desc);
        source = (EditText) v.findViewById(R.id.source);
        url = (EditText) v.findViewById(R.id.url);
        review = (CheckBox) v.findViewById(R.id.review);
        rating = (RatingBar) v.findViewById(R.id.rating);

        date = (EditText) v.findViewById(R.id.date);

//        type.setOnItemSelectedListener(changeType);
        review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rating.setVisibility(
                        ((CheckBox) v).isChecked() ? View.VISIBLE : View.GONE
                );
            }
        });
        return super.createProgressView(inflater, container, v);
    }

    AdapterView.OnItemSelectedListener changeType = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            EditText special = (EditText) getView().findViewById(R.id.special);
            TextView specialL = (TextView) getView().findViewById(R.id.special_label),
                     descL = (TextView) getView().findViewById(R.id.desc_label);
            switch(position) {
                default:
                case 0: //text
                    descL.setText("Text");
                    specialL.setVisibility(View.GONE);
                    special.setVisibility(View.GONE);
                    // + photos
                    break;
                case 1: //quote
                    descL.setText("Source");
                    specialL.setText("Quote");
                    specialL.setVisibility(View.VISIBLE);
                    special.setVisibility(View.VISIBLE);
                    special.setSingleLine(true);
                    // - photos
                    break;
                case 2: //photo
                    descL.setText("Description");
                    special.setVisibility(View.GONE);
                    specialL.setVisibility(View.GONE);
                    // + photos
                    break;
                case 3: //video
                    descL.setText("Description");
                    specialL.setText("Video");
                    specialL.setVisibility(View.VISIBLE);
                    special.setVisibility(View.VISIBLE);
                    special.setSingleLine(false);
                    special.setMinLines(4);
                    break;
                case 4: //url
                    descL.setText("Description");
                    specialL.setText("URL");
                    specialL.setVisibility(View.VISIBLE);
                    special.setVisibility(View.VISIBLE);
                    special.setSingleLine(true);
                    break;
            }
        }
    };

    @Override
    public String getTitle(Resources resources) {
        return "Create Post"; //TODO: String
    }


}
