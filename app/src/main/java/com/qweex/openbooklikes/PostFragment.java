package com.qweex.openbooklikes;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qweex.openbooklikes.model.Post;
import com.qweex.openbooklikes.model.User;

public class PostFragment extends FragmentBase<Post> {

    static final int IMG_SIZE_PX = 500;
    User user;

    @Override
    String getTitle() {
        return user.blog_title;
    }

    @Override
    public void setArguments(Bundle a) {
        primary = new Post(a);
        user = new User(a);
        super.setArguments(a);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        Log.d("OBL:primary", "createView " + primary.title);


        setOrHide(view, R.id.title, primary.title);
        setOrHide(view, R.id.date, primary.date);
        setOrHide(view, R.id.special, primary.special);
        setOrHide(view, R.id.description, primary.desc);

        ((TextView)view.findViewById(R.id.likes)).setText(primary.like_count);
        ((TextView)view.findViewById(R.id.reblogs)).setText(primary.reblog_count);

        if(primary.photo_url!=null)
            MainActivity.imageLoader.displayImage(primary.photo_url, (ImageView) view.findViewById(R.id.image));
        else
            view.findViewById(R.id.image).setVisibility(View.GONE);

        LinearLayout tags = (LinearLayout) view.findViewById(R.id.tags);
        if(primary.tag!=null && primary.tag.trim().length()>0) {
            for (String s : primary.tag.split(",")) {
                Button tag = new Button(getActivity());
                tag.setText(s);
                tag.setPadding(5, 5, 5, 5);
                tags.addView(tag);
            }
            tags.setVisibility(View.VISIBLE);
        } else
            tags.setVisibility(View.GONE);

        LinearLayout ratings = (LinearLayout) view.findViewById(R.id.rating);
        ratings.removeAllViews();
        try {
            float r = Float.parseFloat(primary.rating);
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
