package com.beautiful_wallpapers_hd_qhd.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beautiful_wallpapers_hd_qhd.R;
import com.beautiful_wallpapers_hd_qhd.core.adapter.ImageAdapter;
import com.beautiful_wallpapers_hd_qhd.core.controller.AnimationController;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDatabase;
import com.beautiful_wallpapers_hd_qhd.core.di.DaggerAppComponent;
import com.beautiful_wallpapers_hd_qhd.core.di.MyModule;
import com.beautiful_wallpapers_hd_qhd.core.entity.Author;
import com.beautiful_wallpapers_hd_qhd.core.flickr.FlickrHelper;
import com.beautiful_wallpapers_hd_qhd.core.flickr.RequestLoadListener;
import com.etsy.android.grid.StaggeredGridView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Igor on 24.03.2016.
 */
public class AuthorPageActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageLoader mImageLoader = ImageLoader.getInstance();
    private List<String> flickrImagesId = new ArrayList<>();
    private FlickrHelper flickrHelper = new FlickrHelper();

    @Inject AnimationController mAnimationController;
    @Inject FlickrDatabase flickrDB;
    private String mAuthorFlickrId;
    private Author mAuthor;

    @BindView(R.id.author_images_grid_view) StaggeredGridView mGridView;
    @BindView(R.id.profile_image) CircleImageView mAuthorAvatar;
    private ImageAdapter mImageAdapter;

    @BindView(R.id.author_subscribe_btn) Button subscribeBtn;
    @BindView(R.id.author_unsubscribe_btn) Button unsubscribeBtn;

    private boolean isAuthorFavourite = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author);

        DaggerAppComponent.builder().myModule(new MyModule(this)).build().inject(this);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        final LinearLayout circularLayout = (LinearLayout)findViewById(R.id.circular_reveal_background);

        mAuthorFlickrId = getIntent().getStringExtra(getResources().getString(R.string.flickr_author_id));
        mAuthor = flickrDB.getAuthor(mAuthorFlickrId);
        isAuthorFavourite = flickrDB.isFavourite(mAuthorFlickrId, FlickrDatabase.FAVOURITE_AUTHOR);

        if(isAuthorFavourite){
            subscribeBtn.setVisibility(View.GONE);
        } else {
            unsubscribeBtn.setVisibility(View.GONE);
        }

        subscribeBtn.setOnClickListener(this);
        unsubscribeBtn.setOnClickListener(this);

        TextView mProfileName = (TextView)findViewById(R.id.profile_name);
        mProfileName.setText(!mAuthor.getRealName().equals("") ? mAuthor.getRealName() +"\n" + mAuthor.getUserName() : mAuthor.getUserName());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            circularLayout.post(new Runnable() {
                @Override
                public void run() {
                    CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout)findViewById(R.id.toolbar_layout);
                    mAnimationController.circularReveal(circularLayout, collapsingToolbar, R.color.colorPrimary);
                }
            });

        } else {
            circularLayout.setVisibility(View.GONE);
        }

        if(mAuthor.getUserAvatar() != null) {
            mImageLoader.displayImage(mAuthor.getUserAvatar(), mAuthorAvatar);
        }

        setAdapter(mAuthorFlickrId);
    }

    private void setAdapter(String userId){
        mImageAdapter = new ImageAdapter(this, flickrImagesId, "author_"+flickrImagesId);
        mGridView.setAdapter(mImageAdapter);
        flickrHelper.processRequestSearch(FlickrHelper.ARG_USER_ID, userId, new RequestLoadListener() {
            @Override
            public void onLoad(byte[] responseBody) {
                for (int i = 0; i < flickrHelper.getJSONArray(responseBody, "photos", "photo").length(); i++) {
                    flickrImagesId.add(flickrHelper.getValue(responseBody, "photos", "photo", i, "id"));
                }
                mImageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFail(int statusCode, Throwable error) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.author_subscribe_btn:
                //todo animation gone
                mAnimationController.replace(R.anim.slide_out_right, android.R.anim.slide_in_left, subscribeBtn, unsubscribeBtn);
                flickrDB.addFavourite(mAuthorFlickrId, FlickrDatabase.FAVOURITE_AUTHOR);
                isAuthorFavourite = !isAuthorFavourite;
                break;
            case R.id.author_unsubscribe_btn:
                //todo animation gone
                mAnimationController.replace(R.anim.slide_out_left, R.anim.slide_in_right, unsubscribeBtn, subscribeBtn);
                flickrDB.removeFavourite(mAuthorFlickrId, FlickrDatabase.FAVOURITE_AUTHOR);
                isAuthorFavourite = !isAuthorFavourite;
                break;
        }
    }
}
