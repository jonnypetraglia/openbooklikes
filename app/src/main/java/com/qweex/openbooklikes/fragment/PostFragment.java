package com.qweex.openbooklikes.fragment;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qweex.openbooklikes.activity.MainActivity;
import com.qweex.openbooklikes.R;
import com.qweex.openbooklikes.model.Post;
import com.qweex.openbooklikes.model.User;

public class PostFragment extends FragmentBase<Post> {
    User owner;

    @Override
    public String getTitle(Resources r) {
        return owner.getS("blog_title");
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

        ((TextView)view.findViewById(R.id.likes)).setText(primary.getS("like_count"));
        ((TextView)view.findViewById(R.id.reblogs)).setText(primary.getS("reblog_count"));

        if(primary.getS("photo_url")!=null)
            MainActivity.imageLoader.displayImage(primary.getS("photo_url"), (ImageView) view.findViewById(R.id.image_view));
        else
            view.findViewById(R.id.image_view).setVisibility(View.GONE);

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

        LinearLayout ratings = (LinearLayout) view.findViewById(R.id.rating);
        ratings.removeAllViews();
        try {
            float r = Float.parseFloat(primary.getS("rating"));
            while(r-- > 0.5) {
                ImageView star = new ImageView(getActivity());
                star.setImageResource(android.R.drawable.btn_star_big_on);
                ratings.addView(star);
            }
            if(r>0) {
                ImageView star = new ImageView(getActivity());
                star.setImageResource(android.R.drawable.btn_star_big_off);
                ratings.addView(star);
            }
            ratings.setVisibility(View.VISIBLE);
        } catch(Exception e) {
            ratings.setVisibility(View.GONE);
        }

        return super.createProgressView(inflater, container, view);
    }
}
