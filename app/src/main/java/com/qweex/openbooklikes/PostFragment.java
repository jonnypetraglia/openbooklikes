package com.qweex.openbooklikes;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qweex.openbooklikes.model.Post;

public class PostFragment extends Fragment {

    static final int IMG_SIZE_PX = 500;
    String blogTitle;
    Post post;


    public void setPost(Post p, String b) {
        this.post = p;
        this.blogTitle = b;
        Log.d("OBL:post", "setPost " + post.date);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        Log.d("OBL:post", "createView " + post.title);

        ((Toolbar) getActivity().findViewById(R.id.side_toolbar)).setTitle(blogTitle);


        setOrHide(view, R.id.title, post.title);
        setOrHide(view, R.id.date, post.date);
        setOrHide(view, R.id.special, post.special);
        setOrHide(view, R.id.description, post.desc);

        ((TextView)view.findViewById(R.id.likes)).setText(post.like_count);
        ((TextView)view.findViewById(R.id.reblogs)).setText(post.reblog_count);

        if(post.photo_url!=null)
            MainActivity.imageLoader.displayImage(post.photo_url, (ImageView) view.findViewById(R.id.image));
        else
            view.findViewById(R.id.image).setVisibility(View.GONE);

        LinearLayout tags = (LinearLayout) view.findViewById(R.id.tags);
        if(post.tag!=null && post.tag.trim().length()>0) {
            for (String s : post.tag.split(",")) {
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
            float r = Float.parseFloat(post.rating);
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

        return view;
    }

    private void setOrHide(View container, int tvId, String text) {
        ((TextView)container.findViewById(tvId)).setText(text);
        ((View)container.findViewById(tvId).getParent()).setVisibility(text==null ? View.GONE : View.VISIBLE);
    }

}
