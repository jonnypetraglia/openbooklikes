package com.qweex.imagevieweractivity;


import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.qweex.openbooklikes.DownloadableImageView;
import com.qweex.openbooklikes.R;
import com.qweex.openbooklikes.activity.MainActivity;
import com.qweex.openbooklikes.fragment.FragmentBase;

public class ImageViewerActivity extends AppCompatActivity {
    int ID = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout f = new FrameLayout(this);
        f.setId(ID);
        setContentView(f);

        ImageViewerFragment i = new ImageViewerFragment();
        i.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .add(ID, i)
                .commit();
    }

    public static class ImageViewerFragment extends FragmentBase {

        private ViewPager mPager;

        String[] urls, captions;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            urls = getArguments().getStringArray("urls");
            captions = getArguments().getStringArray("captions");
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mPager = new ViewPager(getContext());
            mPager.setBackgroundColor(0xff000000);
            mPager.setAdapter(new ImageViewerAdapter());
            mPager.setCurrentItem(getArguments().getInt("selected", 0));
            return mPager;
        }

        @Override
        public String getTitle(Resources resources) {
            return null;
        }

        class ImageViewerAdapter extends PagerAdapter {

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }

            @Override
            public int getCount() {
                return urls.length;
            }

            @Override
            public Object instantiateItem(ViewGroup view, int position) {
                View imageLayout = getActivity().getLayoutInflater().inflate(R.layout.image_viewer_image, view, false);

                DownloadableImageView imageView = (DownloadableImageView) imageLayout.findViewById(R.id.image_view);
                imageView.setSource(captions[position], urls[position]);

                ((TextView) imageLayout.findViewById(R.id.title)).setText(captions[position]);
                MainActivity.imageLoader.displayImage(urls[position], imageView);

                view.addView(imageLayout);

                return imageLayout;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view.equals(object);
            }

            @Override
            public void restoreState(Parcelable state, ClassLoader loader) {
            }

            @Override
            public Parcelable saveState() {
                return null;
            }
        }
    }
}
