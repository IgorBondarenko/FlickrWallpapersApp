package com.beautiful_wallpapers_hd_qhd.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beautiful_wallpapers_hd_qhd.R;
import com.beautiful_wallpapers_hd_qhd.core.adapter.ImageRecyclerAdapter;
import com.beautiful_wallpapers_hd_qhd.core.controller.AnimationController;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDatabase;
import com.beautiful_wallpapers_hd_qhd.core.di.DaggerAppComponent;
import com.beautiful_wallpapers_hd_qhd.core.di.MyModule;
import com.beautiful_wallpapers_hd_qhd.core.entity.Author;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.FlickrHelper;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.FlickrAPI;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Igor on 24.03.2016.
 */
public class AuthorPageActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageLoader mImageLoader = ImageLoader.getInstance();
    private List<String> flickrImagesId = new ArrayList<>();

    @Inject AnimationController mAnimationController;
    @Inject FlickrDatabase flickrDB;
    @Inject FlickrAPI flickrAPI;

    private ImageRecyclerAdapter mImageAdapter;
    private String mAuthorFlickrId;
    private Author mAuthor;

    @BindView(R.id.author_images_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.profile_image) CircleImageView mAuthorAvatar;
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

        mAuthorFlickrId = getIntent().getStringExtra(getResources().getString(R.string.extra_flickr_author_id));
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
            circularLayout.post(() -> {
                CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout)findViewById(R.id.toolbar_layout);
                mAnimationController.circularReveal(circularLayout, collapsingToolbar, R.color.colorPrimary);
            });

        } else {
            circularLayout.setVisibility(View.VISIBLE);
        }

        if(mAuthor.getUserAvatar() != null) {
            mImageLoader.displayImage(mAuthor.getUserAvatar(), mAuthorAvatar);
        }

        setAdapter(mAuthorFlickrId);
    }

    private void setAdapter(String userId){
        mImageAdapter = new ImageRecyclerAdapter(this, flickrImagesId, "author_"+flickrImagesId);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(getResources().getInteger(R.integer.columns), StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mImageAdapter);

        flickrAPI.getPhotosByAuthors(FlickrHelper.METHOD_SEARCH, userId)
                .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .map(photosObject -> photosObject.getPhotos().getPhoto())
                .flatMap(Observable::from)
                .doOnCompleted(() -> mImageAdapter.notifyDataSetChanged())
                .subscribe(
                        photo -> flickrImagesId.add(photo.getId()),
                        e -> Log.d("myLog", e.fillInStackTrace().toString())
                );
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
                mAnimationController.replace(R.anim.slide_out_right, android.R.anim.slide_in_left, subscribeBtn, unsubscribeBtn);
                flickrDB.addFavourite(mAuthorFlickrId, FlickrDatabase.FAVOURITE_AUTHOR);
                isAuthorFavourite = !isAuthorFavourite;
                break;
            case R.id.author_unsubscribe_btn:
                mAnimationController.replace(R.anim.slide_out_left, R.anim.slide_in_right, unsubscribeBtn, subscribeBtn);
                flickrDB.removeFavourite(mAuthorFlickrId, FlickrDatabase.FAVOURITE_AUTHOR);
                isAuthorFavourite = !isAuthorFavourite;
                break;
        }
    }
}
