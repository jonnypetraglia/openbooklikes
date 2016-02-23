package com.qweex.openbooklikes.fragment;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.qweex.imagevieweractivity.ImageViewerActivity;
import com.qweex.openbooklikes.R;
import com.qweex.openbooklikes.activity.MainActivity;
import com.qweex.openbooklikes.model.Post;
import com.qweex.openbooklikes.model.User;
import com.qweex.openbooklikes.notmine.Misc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PostFragment extends FragmentBase<Post> {
    User owner;

    @Override
    public String getTitle(Resources r) {
        return "[" + primary.getS("type").toUpperCase() + "] " +
                owner.getS("blog_title");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        owner.wrapInBundle(outState);
    }

    @Override
    public void setArguments(Bundle a) {
        primary = new Post(a, owner = new User(a));
        super.setArguments(a);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        Log.d("OBL:post", "createView " + primary.getS("title"));


        setOrHide(view, R.id.title, primary.getS("title"));
        setOrHide(view, R.id.date, primary.getS("date"));
        setOrHide(view, R.id.special, primary.getS("special"));
        setOrHide(view, R.id.desc, primary.getS("desc"));
        String source = primary.getS("source");
        if(source!=null)
            source = "<a href='" + source + "'>" + source + "</a>";
        setOrHide(view, R.id.source, source);

        ((TextView)view.findViewById(R.id.likes)).setText(primary.getS("like_count"));
        ((TextView)view.findViewById(R.id.reblogs)).setText(primary.getS("reblog_count"));


        JSONArray a = primary.getA("photos");
        LinearLayout images = (LinearLayout) ((ViewGroup)view.findViewById(R.id.images)).getChildAt(0);
        try {
            for(int i=0; i<a.length(); i++) {
                JSONObject photo = a.getJSONObject(i);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Misc.convertDpToPixel(60, getContext())
                );
                int m = Misc.convertDpToPixel(10, getContext());
                lp.setMargins(m, m * 2, m, m * 2);

                ImageView image = new ImageView(getContext());
                images.addView(image, lp);
                MainActivity.imageLoader.displayImage(photo.getString("photo_url"), image);
                image.setOnClickListener(clickImage);
            }
        } catch (Exception e) {
            e.printStackTrace();
            images.setVisibility(View.GONE);
        }

        LinearLayout tags = (LinearLayout) view.findViewById(R.id.tags);
        tags.removeAllViews();
        if(primary.getS("tag")!=null && primary.getS("tag").trim().length()>0) {
            for (String s : primary.getS("tag").split(",")) {
                TextView tag = new TextView(getActivity());
                tag.setBackgroundResource(R.drawable.tag);
                tag.setText(s);
                tag.setTextColor(getResources().getColor(android.R.color.white));
                int dp = (int) dpToPx(5);
                tag.setPadding(dp, dp, dp, dp);
                tags.addView(tag);
            }
            tags.setVisibility(View.VISIBLE);
        } else
            tags.setVisibility(View.GONE);

        RatingBar rating = ((RatingBar)view.findViewById(R.id.rating));
        float r = Float.parseFloat(primary.getS("rating"));
        rating.setRating(r);
        rating.setIsIndicator(true);
        rating.setVisibility(primary.getS("is_review").equals("1") ? View.VISIBLE : View.GONE);

        return super.createProgressView(inflater, container, view);
    }


    View.OnClickListener clickImage = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            JSONArray array = primary.getA("photos");
            String[] urls = new String[array.length()],
                    captions = new String[array.length()];
            int position = ((ViewGroup)v.getParent()).indexOfChild(v);

            try {
                for (int i = 0; i < array.length(); i++) {
                    urls[i] = array.getJSONObject(i).getString("photo_url")
                            .replaceFirst("photo\\/max\\/[0-9]+\\/[0-9]+\\/", "");
                    captions[i] = array.getJSONObject(i).getString("photo_caption");
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }

            final ImageViewerActivity.ImageViewerFragment fragment = new ImageViewerActivity.ImageViewerFragment();
            Bundle b = new Bundle();
            b.putStringArray("urls", urls);
            b.putStringArray("captions", captions);
            b.putInt("selected", position);
            fragment.setArguments(b);
            final FragmentManager sfm = getActivity().getSupportFragmentManager();
            sfm
                    .beginTransaction()
                    .add(R.id.side_fragment, fragment, "RADDA")
                    .addToBackStack("RADDA")
                    .commit();
            getMainActivity().findViewById(R.id.side_toolbar).setVisibility(View.GONE);
            sfm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    Log.d("WEEEEEEEEEE", sfm.getBackStackEntryCount() + "bob");
                    if(sfm.getBackStackEntryCount()>0) { // && sfm.getBackStackEntryAt(0) instanceof ImageViewerActivity.ImageViewerFragment) {
                        getActivity().findViewById(R.id.side_toolbar).setVisibility(View.GONE);
                    }
                    else {
                        getActivity().findViewById(R.id.side_toolbar).setVisibility(View.VISIBLE);
                        sfm.removeOnBackStackChangedListener(this);
                    }
                }
            });

//            Intent intent = new Intent(getContext(), ImageViewerActivity.class);
//            intent.putExtra("urls", urls);
//            intent.putExtra("captions", captions);
//            intent.putExtra("selected", position);
//            startActivity(intent);
        }
    };
}
