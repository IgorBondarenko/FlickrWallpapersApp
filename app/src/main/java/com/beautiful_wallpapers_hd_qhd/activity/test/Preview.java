package com.beautiful_wallpapers_hd_qhd.activity.test;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.beautiful_wallpapers_hd_qhd.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Igor on 01.09.2016.
 */
public class Preview extends AppCompatActivity {

    @BindView(R.id.preview_image_vp) ViewPager mViewPager;
    private MyFragmentPagerAdapter pagerAdapter;

    private static List<String> mImageFlickrIds = new ArrayList<>();

    public static int SelectedIndex = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_image);


        ActivityCompat.postponeEnterTransition(this);
        ActivityCompat.setEnterSharedElementCallback(this, EnterTransitionCallback);

        ButterKnife.bind(this);

        mImageFlickrIds.addAll(getIntent().getStringArrayListExtra("list"));

        /*mImageFlickrIds.add("21160839735");
        mImageFlickrIds.add("21083228645");
        mImageFlickrIds.add("21060409888");
        mImageFlickrIds.add("20975031068");
        mImageFlickrIds.add("19710094126");
        mImageFlickrIds.add("17672601688");
        mImageFlickrIds.add("17353108182");
        mImageFlickrIds.add("19786201820");*/

        pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.setCurrentItem(getIntent().getIntExtra("list_pos", 0));

        mViewPager.setOnPageChangeListener(PageChangeListener);
        mViewPager.getViewTreeObserver().addOnGlobalLayoutListener(PagerLayoutListener);
    }

    private ViewPager.OnPageChangeListener PageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            SelectedIndex = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    private ViewTreeObserver.OnGlobalLayoutListener PagerLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mViewPager.getViewTreeObserver().removeOnGlobalLayoutListener(PagerLayoutListener);
                ActivityCompat.startPostponedEnterTransition(Preview.this);
            }
        }
    };

    private final SharedElementCallback EnterTransitionCallback = new SharedElementCallback() {
        @SuppressLint("NewApi")
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            View view = null;

            if (mViewPager.getChildCount() > 0) {
                view = pagerAdapter.getCurrentView(mViewPager);
                //view = view.findViewById(R.id.preview_iv);
            }

            if (view != null) {
                sharedElements.put(names.get(0), view);
            }
        }
    };

    private static class MyFragmentPagerAdapter extends FragmentStatePagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return PreviewImageFragment.newInstance(position, mImageFlickrIds.get(position));
        }

        @Override
        public int getCount() {
            return mImageFlickrIds.size();
        }

        public View getCurrentView(ViewPager pager) {
            /*for (int i=0; i<pager.getChildCount(); i++) {
                /*if (pager.getChildAt(i).getTag(R.id.index) == pager.getCurrentItem()) {
                    return pager.getChildAt(i);
                }

            }*/

            return pager.getChildAt(pager.getCurrentItem());
        }

    }
}
