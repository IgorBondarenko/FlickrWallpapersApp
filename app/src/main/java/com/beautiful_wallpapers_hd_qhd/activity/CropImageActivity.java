package com.beautiful_wallpapers_hd_qhd.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import com.beautiful_wallpapers_hd_qhd.R;
import com.beautiful_wallpapers_hd_qhd.core.Advertising;
import com.beautiful_wallpapers_hd_qhd.core.Device;
import com.beautiful_wallpapers_hd_qhd.core.WallpaperInstaller;
import com.beautiful_wallpapers_hd_qhd.core.WallpapersApplication;
import com.beautiful_wallpapers_hd_qhd.core.controller.SharedPreferencesController;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDatabase;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDataBaseHelper;
import com.beautiful_wallpapers_hd_qhd.core.di.DaggerAppComponent;
import com.beautiful_wallpapers_hd_qhd.core.di.MyModule;
import com.edmodo.cropper.CropImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Igor on 20.10.2014.
 */
public class CropImageActivity extends ActionBarActivity implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {

    //private Analytics analytics = test_new Analytics(this);
    private ImageLoader imageLoader = ImageLoader.getInstance();

    private WallpaperInstaller mWallpaperInstaller;
    @Inject DisplayMetrics mDisplayMetrics;
    private Thread setWallpaperThread;

    private int mWidth = 5;
    private int mHeight = 4;

    @BindView(R.id.crop_image_view) CropImageView mCropImageView;
    @BindView(R.id.ll_crop_iv) LinearLayout linearLayout;
    @BindView(R.id.crop_width_tv) TextView mWidthTextView;
    @BindView(R.id.crop_height_tv) TextView mHeightTextView;
    @BindView(R.id.aspect_ratio_cb) CheckBox mAspectRatioCheckBox;
    @BindView(R.id.no_crop_cb) CheckBox mNoCropCheckBox;
    @BindView(R.id.width_seekBar) SeekBar mWidthSeekBar;
    @BindView(R.id.height_seekBar) SeekBar mHeightSeekBar;
    @BindView(R.id.switch1) Switch mSwitcher;

    @Inject FlickrDatabase flickrDB;
    private String mFlickrImageId;
    private String mFlickrImageUrl;

    Bitmap mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);

        //WallpapersApplication.getComponent().inject(this);
        DaggerAppComponent.builder().myModule(new MyModule(this)).build().inject(this);
        ButterKnife.bind(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(mDisplayMetrics);

        mFlickrImageId = getIntent().getStringExtra("flickrImageId");
        mFlickrImageUrl = flickrDB.getPhoto(FlickrDataBaseHelper.TABLE_PREVIEW_SIZE, mFlickrImageId);

        mSwitcher.setOnCheckedChangeListener(this);

        try{
            mImage = imageLoader.loadImageSync(flickrDB.getPhoto(FlickrDataBaseHelper.TABLE_PREVIEW_SIZE, mFlickrImageId));
            setupLayout();
            if(!new Device(this).isTablet()){
                if(mImage.getWidth() < mImage.getHeight()){
                    mSwitcher.setChecked(true);
                    setPortraitAspectRatio();
                } else{
                    setLandscapeAspectRatio();
                }
            } else{
                mSwitcher.setEnabled(false);
                setTabletAspectRatio();
            }
        } catch (NullPointerException e){
            Intent previewIntent = new Intent(getResources().getString(R.string.preview_activity));
            previewIntent.putExtra("flickrImageId", mFlickrImageId);
            startActivity(previewIntent);
        }

        mCropImageView.setImageBitmap(mImage);
        mCropImageView.setFixedAspectRatio(true);
        mCropImageView.setGuidelines(1);

        mAspectRatioCheckBox.setOnCheckedChangeListener(this);
        mNoCropCheckBox.setOnCheckedChangeListener(this);
        mWidthSeekBar.setOnSeekBarChangeListener(this);
        mHeightSeekBar.setOnSeekBarChangeListener(this);

        initWallpaperInstaller();
//        todo HideOnScroll
        //scrollView.setOnTouchListener(test_new ShowHideOnScroll(setAsWallpaperFAB, R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom));
    }

    private void setPortraitAspectRatio(){
        double param = (double)mDisplayMetrics.widthPixels / (double)mDisplayMetrics.heightPixels;

        for (ImageSizes size : ImageSizes.values()) {
            if(param > (size.getRatio() - 0.01) && param < (size.getRatio() + 0.01)) {
                setAspectRatio(size.getWidth(), size.getHeight());
            }
        }
    }

    private void setTabletAspectRatio(){
        double param = (double)mDisplayMetrics.widthPixels / (double)mDisplayMetrics.heightPixels;
        for (ImageSizes size : ImageSizes.values()) {
            if(param > (size.getRatio() - 0.01) && param < (size.getRatio() + 0.01)) {
                setAspectRatio(size.getHeight(), size.getWidth());
            }
        }
    }

    private void setLandscapeAspectRatio(){
        setAspectRatio(mWidth, mHeight);
    }

    private void setAspectRatio(int v1, int v2){
        mWidthTextView.setText(String.valueOf(v1));
        mHeightTextView.setText(String.valueOf(v2));
        mCropImageView.setAspectRatio(v1, v2);
    }

    private void setupLayout(){
        if(mImage.getHeight() > mImage.getWidth()){
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,  mDisplayMetrics.heightPixels/2);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            linearLayout.setLayoutParams(params);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()){
            case R.id.width_seekBar:
                mWidth = (progress == 0) ? 1 : progress;
                mWidthTextView.setText(String.valueOf(mWidth));
                break;
            case R.id.height_seekBar:
                mHeight = (progress == 0) ? 1 : progress;
                mHeightTextView.setText(String.valueOf(mHeight));
                break;
        }
        mCropImageView.setAspectRatio(mWidth, mHeight);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void initWallpaperInstaller(){
        mWallpaperInstaller = isCrop ? new WallpaperInstaller(this, mCropImageView) : new WallpaperInstaller(this);
        setWallpaperThread = mWallpaperInstaller.setWallpaper(mFlickrImageUrl);
    }

    @OnClick(R.id.accept_crop_fab)
    public void OnClickFAB(View v){
        if(!setWallpaperThread.isAlive()){

            if(!(new SharedPreferencesController(getBaseContext()).getBool(SharedPreferencesController.SP_PRO_VERSION, false))){
                new Advertising(getBaseContext()).loadFullScreenAd(Advertising.CROP_IMAGE_ACTIVITY_AD_ID);
            }
            //analytics.registerEvent("CropImage", Analytics.BUTTON_PRESSED, "install wallpaper "+dao.getStringValueByURL(u, "category") + "_category_id-" +  dao.getIntegerValueByURL(u, "cat_index"), 0);

            initWallpaperInstaller();
            Animation anim = AnimationUtils.loadAnimation(getApplication(), R.anim.zoom_btn);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    setWallpaperThread.start();
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    Toast.makeText(getBaseContext(), R.string.successfully_installed, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            v.startAnimation(anim);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isCrop = true;

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId()){
            case R.id.aspect_ratio_cb:
                //analytics.registerEvent("CropImage", Analytics.CHECK_BOX, "aspect ratio", 0);
                mWidthSeekBar.setEnabled(!isChecked);
                mHeightSeekBar.setEnabled(!isChecked);
                mCropImageView.setFixedAspectRatio(!isChecked);
                break;
            case R.id.no_crop_cb:
                isCrop = !isChecked;
                mSwitcher.setChecked(false);
                mSwitcher.setEnabled(!isChecked);

                mWidthSeekBar.setEnabled(!isChecked);
                mHeightSeekBar.setEnabled(!isChecked);
                mAspectRatioCheckBox.setEnabled(!isChecked);
                break;
            case R.id.switch1:
                if(isChecked){
                    setPortraitAspectRatio();
                } else {
                    setLandscapeAspectRatio();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    private enum ImageSizes{
        SIZE_9X16(9, 16), SIZE_5X16(5, 16), SIZE_3X4(3, 4), SIZE_10X16(10, 16), SIZE_16X25(16, 25);
        int width;
        int height;

        ImageSizes(int width, int height){
            this.width = width;
            this.height = height;
        }

        public double getRatio(){
            return width / height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}
