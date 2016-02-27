package com.qweex.openbooklikes.fragment;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.qweex.openbooklikes.ApiClient;
import com.qweex.openbooklikes.R;
import com.qweex.openbooklikes.activity.MainActivity;
import com.qweex.openbooklikes.handler.LoadingResponseHandler;
import com.qweex.openbooklikes.model.Post;
import com.qweex.openbooklikes.notmine.Misc;
import com.qweex.openbooklikes.notmine.RealPathUtil;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import cz.msebera.android.httpclient.Header;

public class PostCreateFragment extends FragmentBase {

    final static int MAX_IMAGE_COUNT = 10;

    public static final int[] POST_ICONS = {
            R.drawable.text_np45364,
            R.drawable.quote_np45804,
            R.drawable.photo_np45924,
            R.drawable.video_np45324,
            R.drawable.url_np45588
    };

    static final String[] POST_TYPES = {
        "text", "quote", "photo", "video", "url"
    };

    EditText title, special, desc, source, url;
    CheckBox review;
    RatingBar rating;
    Button date;
    LinearLayout imageContainer;
    int typeIndex;
    View addImage;

    final static int SOURCE = R.id.source, CAPTION = R.id.nav_title;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        typeIndex = args.getInt("type");
    }

    RequestParams getParams() throws FileNotFoundException {
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
            //params.put("PostRevRating", new DecimalFormat("#.#").format(rating.getRating()));
        } else {
            params.put("PostIsReview", "0");
        }
        for(int i=0; i<imageContainer.getChildCount(); i++) {
            params.put("PostPhotoFile["+i+"]", new File(imageContainer.getChildAt(i).getTag(SOURCE).toString()));
            params.put("PostPhotoFileCaption["+i+"]", imageContainer.getChildAt(i).getTag(CAPTION).toString());
            params.setForceMultipartEntityContentType(true);
        }
        //TODO: Date? Tags


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
        MenuItem mi;

        mi = menu.add(Menu.NONE, R.id.option_submit, Menu.NONE, "Submit") //TODO: String
                .setIcon(R.drawable.submit_np45903);
        optionIcon(mi);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu.add(Menu.NONE, R.id.option_submit, Menu.NONE, "Save") //TODO: String
                //TODO: Icon?
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.option_submit:
                try {
                    RequestParams p = getParams();
                    loadingManager.show();
                    ApiClient.post(p, responseHandler);
                } catch(FileNotFoundException e) {
                    e.printStackTrace();
                    loadingManager.error(e);
                }
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
        imageContainer = (LinearLayout) v.findViewById(R.id.images);
        addImage = v.findViewById(R.id.addImage);

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

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 0); //TODO: String
            }
        });

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
        if(typeIndex==2) {
            imageContainer.setVisibility(View.VISIBLE);
            addImage.setVisibility(View.VISIBLE);
        } else {
            imageContainer.setVisibility(View.GONE);
            addImage.setVisibility(View.GONE);
        }
        switch(typeIndex) {
            default:
            case 0: //text
                descL.setText("Text"); //TODO: String
                break;
            case 1: //quote
                descL.setText("Source"); //TODO: String
                specialL.setText("Quote"); //TODO: String
                break;
            case 2: //photo
                descL.setText("Description"); //TODO: String
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri _uri = data.getData();
        if(resultCode != Activity.RESULT_OK || _uri == null)
            return;

        final View cont;

//        final ImageView iv = new ImageView(getContext());
//        iv.setBackgroundColor(0xff99cc00);
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.WRAP_CONTENT,
//                Misc.convertDpToPixel(128));
//        int dp = Misc.convertDpToPixel(32);
//        lp.setMargins(dp, dp, dp, dp);
//        iv.setLayoutParams(lp);
//        iv.setAdjustViewBounds(true);
//      cont = iv;

        cont = getLayoutInflater(null).inflate(R.layout.post_image, null);
        ImageView iv = (ImageView) cont.findViewById(R.id.image_view);

        try {
            InputStream input = getContext().getContentResolver().openInputStream(data.getData());
            iv.setImageBitmap(BitmapFactory.decodeStream(input));
            cont.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCaptionDialog(cont);
                }
            });

            cont.setTag(SOURCE, RealPathUtil.get(getContext(), _uri));
            cont.setTag(CAPTION, "");
            imageContainer.addView(cont);
            addImage.setEnabled(imageContainer.getChildCount() < MAX_IMAGE_COUNT);
            showCaptionDialog(cont);
        } catch (Exception e) { //FileNotFoundException
            e.printStackTrace();
        }
    }

    void showCaptionDialog(final View view) {
        final EditText caption = new EditText(getContext());
        caption.setText(view.getTag(CAPTION).toString());
        final AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Set caption")
                .setView(caption)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((TextView) view.findViewById(R.id.title)).setText(caption.getText());
                        view.setTag(CAPTION, caption.getText().toString());
                    }
                })
                .setNeutralButton(android.R.string.cancel, null)
                .setNegativeButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        imageContainer.removeView(view);
                        addImage.setEnabled(imageContainer.getChildCount() < MAX_IMAGE_COUNT);
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        hideKeyboard();
                    }
                })
                .create();
        showKeyboard(alertDialog.getWindow(), caption);
        alertDialog.show();
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
                                    int dp = Misc.convertDpToPixel(32),
                                            lineHeight = Misc.convertDpToPixel(48);;
                                    Drawable d = Misc.resizeDrawable(v, dp, dp);

                                    TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
                                    textView.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
                                    textView.setCompoundDrawablePadding(
                                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics()));
                                    textView.getLayoutParams().height = lineHeight;
                                    return convertView;
                                }
                            },
                        clickType
                )
                .show();
    }
}
