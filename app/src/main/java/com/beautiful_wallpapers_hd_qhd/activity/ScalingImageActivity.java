package com.beautiful_wallpapers_hd_qhd.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.beautiful_wallpapers_hd_qhd.R;
import com.beautiful_wallpapers_hd_qhd.core.Advertising;
import com.beautiful_wallpapers_hd_qhd.core.controller.SharedPreferencesController;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDatabase;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDataBaseHelper;
import com.beautiful_wallpapers_hd_qhd.core.di.DaggerAppComponent;
import com.beautiful_wallpapers_hd_qhd.core.di.MyModule;
import com.beautiful_wallpapers_hd_qhd.core.flickr.FlickrHelper;
import com.beautiful_wallpapers_hd_qhd.core.flickr.RequestLoadListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ooo.oxo.library.widget.TouchImageView;


/**
 * Created by Igor on 21.04.2016.
 */
public class ScalingImageActivity extends Activity implements CompoundButton.OnCheckedChangeListener{

    private ImageLoader mImageLoader = ImageLoader.getInstance();
    private FlickrHelper flickrHelper = new FlickrHelper();

    @BindView(R.id.progressBar2) ProgressBar mProgressBar;
    @BindView(R.id.scaling_image_view) TouchImageView mImageView;
    @BindView(R.id.switch1) Switch mSwitch;
    private View mDecorView;

    @Inject SharedPreferencesController sPref;
    @Inject FlickrDatabase flickrDB;
    private String mFlickrImageId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scaling);

        DaggerAppComponent.builder().myModule(new MyModule(this)).build().inject(this);
        ButterKnife.bind(this);

        mSwitch.setOnCheckedChangeListener(this);

        mFlickrImageId = getIntent().getStringExtra(getString(R.string.extra_flickr_image_id));
        mImageLoader.displayImage(flickrDB.getPhoto(FlickrDataBaseHelper.TABLE_PREVIEW_SIZE, mFlickrImageId), mImageView);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            mDecorView = getWindow().getDecorView();
            mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE);
            buttonsPanelTouchDetector(mImageView);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(!sPref.getBool(SharedPreferencesController.SP_PRO_VERSION, false)){
            new Advertising(this).loadFullScreenAd(Advertising.FULL_SCREEN_ACTIVITY_AD_ID);
        }
    }

    private void buttonsPanelTouchDetector(View view){
        view.setClickable(true);
        final GestureDetector clickDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        boolean visible = (mDecorView.getSystemUiVisibility()
                                & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0;
                        if (visible) {
                            hideSystemUI();
                        } else {
                            showSystemUI();
                        }
                        return true;
                    }
                });
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return clickDetector.onTouchEvent(motionEvent);
            }
        });
    }

    private void hideSystemUI() {
        mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void showSystemUI() {
        mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked){
            final String originalImageUrl = flickrDB.getPhoto(FlickrDataBaseHelper.TABLE_ORIGINAL_SIZE, mFlickrImageId);
            if(originalImageUrl != null){
                mImageLoader.displayImage(originalImageUrl, mImageView);
                showToast("High quality");
            } else {
                mProgressBar.setVisibility(View.VISIBLE);
                flickrHelper.processRequest(FlickrHelper.METHOD_GET_PHOTO_SIZES, FlickrHelper.ARG_GET_PHOTO_ID, mFlickrImageId, new RequestLoadListener() {
                    @Override
                    public void onLoad(byte[] responseBody) {
                        String originalUrl = flickrHelper.getValue(responseBody, "sizes", "size", FlickrHelper.SIZE_ORIGINAL, "source");
                        mImageLoader.displayImage(originalUrl, mImageView);
                        mProgressBar.setVisibility(View.GONE);
                        flickrDB.addPhoto(mFlickrImageId, FlickrDataBaseHelper.TABLE_ORIGINAL_SIZE, originalUrl);
                        showToast("High quality");
                    }

                    @Override
                    public void onFail(int statusCode, Throwable error) {

                    }
                });
            }
        } else {
            mImageLoader.displayImage(flickrDB.getPhoto(FlickrDataBaseHelper.TABLE_PREVIEW_SIZE, mFlickrImageId), mImageView);
            showToast("Low quality");
        }
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
