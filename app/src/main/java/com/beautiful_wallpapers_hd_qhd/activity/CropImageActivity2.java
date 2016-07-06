package com.beautiful_wallpapers_hd_qhd.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.beautiful_wallpapers_hd_qhd.R;
import com.beautiful_wallpapers_hd_qhd.core.Device;
import com.beautiful_wallpapers_hd_qhd.core.WallpaperInstaller;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDatabase;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDataBaseHelper;
import com.edmodo.cropper.CropImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by Igor on 20.10.2014.
 */
public class CropImageActivity2 extends ActionBarActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    //private Analytics analytics = test_new Analytics(this);
    private ImageLoader imageLoader = ImageLoader.getInstance();

    private WallpaperInstaller mWallpaperInstaller;
    private DisplayMetrics mDisplayMetrics;
    private Thread setWallpaperThread;

    private double[] mainWidths = {9, 9, 3, 10, 16};
    private double[] mainHeights = {16, 15, 4, 16, 25};

    private double[] mAspectRatios = {9/16, 9/15, 3/4, 10/16, 16/25};

    private int mWidth = 5;
    private int mHeight = 4;

    private CropImageView mCropImageView;
    private LinearLayout linearLayout;
    private TextView mWidthTextView;
    private TextView mHeightTextView;
    private CheckBox mAspectRatioCheckBox;
    private CheckBox mNoCropCheckBox;
    private SeekBar mWidthSeekBar;
    private SeekBar mHeightSeekBar;
    private FloatingActionButton setAsWallpaperFAB;
    private Switch mSwitcher;

    private String mFlickrImageId;
    private FlickrDatabase flickrDB;
    private String mFlickrImageUrl;

    Bitmap mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        linearLayout = (LinearLayout)findViewById(R.id.ll_crop_iv);

        flickrDB = new FlickrDatabase(this);
        mDisplayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(mDisplayMetrics);

        mFlickrImageId = getIntent().getStringExtra("flickrImageId");
        mFlickrImageUrl = flickrDB.getPhoto(FlickrDataBaseHelper.TABLE_PREVIEW_SIZE, mFlickrImageId);

        mCropImageView = (CropImageView) findViewById(R.id.crop_image_view);

        mWidthTextView = (TextView)findViewById(R.id.crop_width_tv);
        mHeightTextView = (TextView)findViewById(R.id.crop_height_tv);

        mSwitcher = (Switch)findViewById(R.id.switch1);
        mSwitcher.setOnCheckedChangeListener(this);

        try{
            mImage = imageLoader.loadImageSync(flickrDB.getPhoto(FlickrDataBaseHelper.TABLE_PREVIEW_SIZE, mFlickrImageId));
            setupLayout();
            if(!new Device(this).isTablet()){
                if(mImage.getWidth() < mImage.getHeight()){
                    mSwitcher.setChecked(true);
                    /*for (int i = 0; i < mainHeights.length; i++) {
                        setPortraitAspectRatio(mainWidths[i], mainHeights[i]);
                    }*/
                    setPortraitAspectRatio();
                } else{
                    setLandscapeAspectRatio();
                }
            } else{
                mSwitcher.setEnabled(false);
                /*for (int i = 0; i < mainHeights.length; i++) {
                    setTabletAspectRatio(mainWidths[i], mainHeights[i]);
                }*/
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

        mAspectRatioCheckBox = (CheckBox)findViewById(R.id.aspect_ratio_cb);
        mAspectRatioCheckBox.setOnCheckedChangeListener(this);
        mNoCropCheckBox = (CheckBox)findViewById(R.id.no_crop_cb);
        mNoCropCheckBox.setOnCheckedChangeListener(this);

        mWidthSeekBar = (SeekBar)findViewById(R.id.width_seekBar);
        mWidthSeekBar.setOnSeekBarChangeListener(this);
        mHeightSeekBar = (SeekBar)findViewById(R.id.height_seekBar);
        mHeightSeekBar.setOnSeekBarChangeListener(this);

        setAsWallpaperFAB = (FloatingActionButton) findViewById(R.id.accept_crop_fab);
        setAsWallpaperFAB.setOnClickListener(this);

        initWallpaperInstaller();
//        todo HideOnScroll
        //scrollView.setOnTouchListener(test_new ShowHideOnScroll(setAsWallpaperFAB, R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom));
    }

    //private void setPortraitAspectRatio(double mWidth, double mHeight){
    private void setPortraitAspectRatio(){
        double param = (double)mDisplayMetrics.widthPixels / (double)mDisplayMetrics.heightPixels;

        for (ImageSizes size : ImageSizes.values()) {
            if(param > (size.getRatio() - 0.01) && param < (size.getRatio() + 0.01)) {
                /*
                mCropImageView.setAspectRatio(size.getWidth(), size.getHeight());
                mWidthTextView.setText(String.valueOf(size.getWidth()));
                mHeightTextView.setText(String.valueOf(size.getHeight()));*/
                setAspectRatio(size.getWidth(), size.getHeight());
            }
        }

        /*if(param > (mWidth/mHeight - 0.01) && param < (mWidth/mHeight + 0.01)) {
            mCropImageView.setAspectRatio((int)mWidth, (int)mHeight);
            mWidthTextView.setText(String.valueOf((int)mWidth));
            mHeightTextView.setText(String.valueOf((int)mHeight));
        }*/
    }

    private void setTabletAspectRatio(){
        double param = (double)mDisplayMetrics.widthPixels / (double)mDisplayMetrics.heightPixels;
        for (ImageSizes size : ImageSizes.values()) {
            if(param > (size.getRatio() - 0.01) && param < (size.getRatio() + 0.01)) {
                /*
                mCropImageView.setAspectRatio(size.getHeight(), size.getWidth());
                mWidthTextView.setText(String.valueOf(size.getHeight()));
                mHeightTextView.setText(String.valueOf(size.getWidth()));*/
                setAspectRatio(size.getHeight(), size.getWidth());
            }
        }

        /*if(param > (mWidth/mHeight - 0.01) && param < (mWidth/mHeight + 0.01)) {
            mCropImageView.setAspectRatio((int)mHeight, (int)mWidth);
            mWidthTextView.setText(String.valueOf((int)mHeight));
            mHeightTextView.setText(String.valueOf((int)mWidth));
        }*/
    }

    private void setLandscapeAspectRatio(){
        /*
        mWidthTextView.setText(String.valueOf(mWidth));
        mHeightTextView.setText(String.valueOf(mHeight));
        mCropImageView.setAspectRatio(mWidth, mHeight);*/

        setAspectRatio(mWidth, mHeight);

        /*mWidthTextView.setText(String.valueOf(mWidth));
        mHeightTextView.setText(String.valueOf(mHeight));
        mCropImageView.setAspectRatio(mWidth, mHeight);*/
    }

    private void setAspectRatio(int v1, int v2){
        mWidthTextView.setText(String.valueOf(v1));
        mHeightTextView.setText(String.valueOf(v2));
        mCropImageView.setAspectRatio(v1, v2);
    }

    private void setupLayout(){
        //Bitmap bitmap = imageLoader.loadImageSync(flickrDB.getPhoto(FlickrDataBaseHelper.TABLE_PREVIEW_SIZE, mFlickrImageId));

        //int w = bitmap.getWidth();
        //int h = bitmap.getHeight();

        //if(h > w){
        if(mImage.getHeight() > mImage.getWidth()){
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,  mDisplayMetrics.heightPixels/2);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            linearLayout.setLayoutParams(params);
        }
        //return bitmap;
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

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.accept_crop_fab:
                if(!setWallpaperThread.isAlive()){

//                    // TODO: 21.04.2016 Ad
                    //if(!getSharedPreferences(null, MODE_PRIVATE).getBoolean("pro", false)){test_new Advertising(this, Advertising.CROP_IMAGE_ACTIVITY_AD_ID).loadFullScreenAd();}

                    //String u = URL.substring(0, URL.length() - 5);
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
                break;
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
                    /*for (int i = 0; i < mainWidths.length; i++) {
                        setPortraitAspectRatio(mainWidths[i], mainHeights[i]);
                    }*/
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
