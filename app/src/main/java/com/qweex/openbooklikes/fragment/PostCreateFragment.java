package com.qweex.openbooklikes.fragment;


import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.qweex.openbooklikes.ApiClient;
import com.qweex.openbooklikes.R;
import com.qweex.openbooklikes.activity.MainActivity;
import com.qweex.openbooklikes.handler.LoadingResponseHandler;
import com.qweex.openbooklikes.model.Post;
import com.qweex.openbooklikes.notmine.Misc;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class PostCreateFragment extends FragmentBase {

    public static final int[] POST_ICONS = {
            R.drawable.settings_np113122,
            R.drawable.reorder_np203471,
            R.drawable.likes_np131693,
            R.drawable.shelf_np147205,
            R.drawable.profile_np76855
    };

    static final String[] POST_TYPES = {
        "text", "quote", "photo", "video", "url"
    };

    EditText title, special, desc, source, url;
    CheckBox review;
    RatingBar rating;
    Button date;
    int typeIndex;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        typeIndex = args.getInt("type");
    }

    RequestParams getParams() {
        RequestParams params = new RequestParams();
        params.put("PostType", POST_TYPES[typeIndex]);
        params.put("PostTitle", title.getText().toString());
        params.put("PostText", desc.getText().toString());
        if(source.getText().length()>0)
            params.put("PostSource", source.getText().toString());
        if(url.getText().length()>0)
            params.put("PostLinkUrl", url.getText().toString());
        if(review.isChecked()) {
            params.put("PostIsReview", "1");
            params.put("PostRevRating", rating.getRating());
        } else {
            params.put("PostIsReview", "0");
        }
        //params.put("PostRevRating", new DecimalFormat("#.#").format(rating.getRating()));
        //TODO: Date? Tags, photos


        switch(typeIndex) {
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        responseHandler = new LoadingResponseHandler(this) {
            @Override
            protected String urlPath() {
                return "post/PostCreate";
            }

            @Override
            protected String countFieldName() {
                return "count";
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("WEEEEE", response.toString());
                try {
                    if(response.getInt(this.countFieldName())<1)
                        throw new Exception("No post returned: " + response.toString());
                    Post post = new Post(response.getJSONArray("posts").getJSONObject(0), MainActivity.me);
                    Bundle b = MainActivity.me.wrapInBundle(post.wrapInBundle(new Bundle()));

                    PostFragment postFragment = new PostFragment();
                    postFragment.setArguments(b);
                    getMainActivity().loadSideFragment(postFragment);
                    ((Toolbar)getMainActivity().findViewById(R.id.side_toolbar)).setTitle(post.getS("title"));
                } catch (Exception e) {
                    e.printStackTrace();
                    loadingManager.error(e);
                }
            }
        };
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.add(Menu.NONE, R.id.option_submit, Menu.NONE, "Submit") //TODO: String
                //TODO: Icon
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(Menu.NONE, R.id.option_submit, Menu.NONE, "Save") //TODO: String
                //TODO: Icon
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        RequestParams p = getParams();
        switch(item.getItemId()) {
            case R.id.option_submit:
                Log.d("WEEEE", p.toString());
                loadingManager.show();
                ApiClient.post(p, responseHandler);
                break;
            case R.id.option_save:
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_post_new, null);

        title = (EditText) v.findViewById(R.id.title);
        special = (EditText) v.findViewById(R.id.special);
        desc = (EditText) v.findViewById(R.id.desc);
        source = (EditText) v.findViewById(R.id.source);
        url = (EditText) v.findViewById(R.id.url);
        review = (CheckBox) v.findViewById(R.id.review);
        rating = (RatingBar) v.findViewById(R.id.rating);

        date = (Button) v.findViewById(R.id.date);
        //TODO: Show DatePickerDialog on click

        review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rating.setVisibility(
                        ((CheckBox) v).isChecked() ? View.VISIBLE : View.INVISIBLE
                );
            }
        });

        TextView specialL = (TextView) v.findViewById(R.id.special_label),
                descL = (TextView) v.findViewById(R.id.desc_label);

        switch(typeIndex) {
            default:
            case 0: //text:
            case 2: //photo
                specialL.setVisibility(View.GONE);
                special.setVisibility(View.GONE);
                break;
            case 4: //url
                specialL.setVisibility(View.VISIBLE);
                special.setVisibility(View.VISIBLE);
                special.setSingleLine(true);
                special.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
                break;
            case 1: //quote
            case 3: //video
                specialL.setVisibility(View.VISIBLE);
                special.setVisibility(View.VISIBLE);
                special.setSingleLine(false);
                special.setMinLines(4);
                special.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                break;
        }
        switch(typeIndex) {
            default:
            case 0: //text
                descL.setText("Text"); //TODO: String
                // + photos
                break;
            case 1: //quote
                descL.setText("Source"); //TODO: String
                specialL.setText("Quote"); //TODO: String
                // - photos
                break;
            case 2: //photo
                descL.setText("Description"); //TODO: String
                // + photos
                break;
            case 3: //video
                descL.setText("Description"); //TODO: String
                specialL.setText("Video"); //TODO: String
                break;
            case 4: //url
                descL.setText("Description"); //TODO: String
                specialL.setText("URL"); //TODO: String
                break;
        }
        return super.createProgressView(inflater, container, v);
    }

    @Override
    public String getTitle(Resources resources) {
        return POST_TYPES[typeIndex].toUpperCase();
    }



    static public void showTypePicker(final FragmentBase fragment, final MainActivity activity) {
        DialogInterface.OnClickListener clickType = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PostCreateFragment fragment = new PostCreateFragment();
                Bundle b = new Bundle();
                b.putInt("type", which);
                fragment.setArguments(b);
                activity.loadSideFragment(fragment);
            }
        };
        new AlertDialog.Builder(fragment.getContext())
                .setTitle("Create Post")
                .setAdapter(new ArrayAdapter<String>(
                                    fragment.getContext(),
                                    android.R.layout.select_dialog_item,
                                    fragment.getResources().getStringArray(R.array.post_types)) {
                                @Override
                                public View getView(int position, View convertView, ViewGroup parent) {
                                    convertView = super.getView(position, convertView, parent);

                                    VectorDrawable v = (VectorDrawable) fragment.getResources().getDrawable(POST_ICONS[position]);
                                    int dp = Misc.convertDpToPixel(32);
                                    Drawable d = Misc.resizeDrawable(v, dp, dp);

                                    TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
                                    textView.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
                                    textView.setCompoundDrawablePadding(
                                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics()));
                                    textView.getLayoutParams().height = 100;
                                    return convertView;
                                }
                            },
                        clickType
                )
                .show();
    }
}
