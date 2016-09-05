package com.beautiful_wallpapers_hd_qhd.activity.test;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.beautiful_wallpapers_hd_qhd.R;
import com.beautiful_wallpapers_hd_qhd.activity.MainActivity;
import com.beautiful_wallpapers_hd_qhd.core.Advertising;
import com.beautiful_wallpapers_hd_qhd.core.Device;
import com.beautiful_wallpapers_hd_qhd.core.controller.AnimationController;
import com.beautiful_wallpapers_hd_qhd.core.controller.SharedPreferencesController;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDataBaseHelper;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDatabase;
import com.beautiful_wallpapers_hd_qhd.core.di.DaggerAppComponent;
import com.beautiful_wallpapers_hd_qhd.core.di.MyModule;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.FlickrAPI;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.FlickrHelper;
import com.beautiful_wallpapers_hd_qhd.core.view.ResizablePortraitImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import me.kaede.tagview.Tag;
import me.kaede.tagview.TagView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Igor on 01.09.2016.
 */
public class PreviewImageFragment extends Fragment{

    private static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    private static final String ARGUMENT_FLICKR_IMAGE_ID = "arg_flickr_image_id";

    //private int pageNumber;
    private static String mFlickrImageId;

    public static PreviewImageFragment newInstance(int page, String flickrImageId) {

        PreviewImageFragment pageFragment = new PreviewImageFragment();

        Log.d("myLog", "page=" + page + " id="+flickrImageId);

        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        arguments.putString(ARGUMENT_FLICKR_IMAGE_ID, flickrImageId);

        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    private static final int READ_EXTERNAL_STORAGE_PERMISSION = 10;
    private ImageLoader mImageLoader = ImageLoader.getInstance();

    private boolean isFavouriteImage = false;
    private boolean isProVersion = false;
    private String previewImageUrl;

    @Inject AnimationController mAnimationController;
    @Inject SharedPreferencesController sPref;
    @Inject Advertising mAdvertising;
    @Inject FlickrAPI flickrAPI;
    @Inject FlickrDatabase flickrDB;
    @Inject static Device mDevice;

    @BindView(R.id.preview_iv) ResizablePortraitImageView mImageView;
    @BindView(R.id.buttons_panel) LinearLayout buttonsPanel;
    @BindView(R.id.load_preview_pb) ProgressBar mProgressBar;
    @BindView(R.id.container) ViewPager mViewPager;
    @BindView(R.id.additional_inform_layout) LinearLayout additionalInform;
    @BindView(R.id.scale_image_fab) FloatingActionButton mScaleFAB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFlickrImageId = getArguments().getString(ARGUMENT_FLICKR_IMAGE_ID);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        DaggerAppComponent.builder().myModule(new MyModule(context)).build().inject(this);
    }

    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_preview, null);

        unbinder = ButterKnife.bind(this, view);

        previewImageUrl = flickrDB.getPhoto(FlickrDataBaseHelper.TABLE_PREVIEW_SIZE, mFlickrImageId);

        if(previewImageUrl != null){
            Log.d("myLog", "url="+previewImageUrl);
            loadPreviewImage(previewImageUrl);
        } else {
            flickrAPI.getPhotoSizes(FlickrHelper.METHOD_GET_PHOTO_SIZES, mFlickrImageId)
                    .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                    .map(photo -> photo.getSizes().getSizesArray().get(FlickrHelper.SIZE_LARGE).getSize())
                    .subscribe(
                            previewUrl -> {setPreviewImageUrl(previewUrl); loadPreviewImage(previewUrl); Log.d("myLog", "url="+previewUrl);},
                            e -> {getActivity().finish();}
                    );
        }

        //loadButtonsPanel();
        loadTags(view);

        /*
        if(!(isProVersion = sPref.getBool(SharedPreferencesController.SP_PRO_VERSION, false))){
            mAdvertising.loadSmartBanner(R.id.preview_ad_stub, R.id.preview_ad_view);
        }*/

        mScaleFAB.setOnClickListener(v -> {
            Intent scalingImageIntent = new Intent(getString(R.string.scaling_image_activity));
            scalingImageIntent.putExtra(getString(R.string.extra_flickr_image_id), mFlickrImageId);
            mAnimationController.transition(mImageView, getString(R.string.transition_image), scalingImageIntent);
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //mImageLoader.getDiskCache().remove(previewImageUrl);
        unbinder.unbind();
    }

    private void loadTags(View view){

        TagView tagView = (TagView)view.findViewById(R.id.tagview);
        flickrAPI.getPhotoInformation(FlickrHelper.METHOD_PHOTOS_GET_INFO, mFlickrImageId)
                .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .map(photoInformation -> photoInformation.getPhoto().getTags())
                .filter(tags -> tags != null)
                .flatMap(tags -> Observable.from(tags))
                .subscribe(
                        tagText -> {
                            Tag tag = new Tag("#"+tagText.getTag());
                            tag.background = getResources().getDrawable(R.drawable.capsule_button);
                            tagView.addTag(tag);
                            tagView.setOnTagClickListener((clickedTag, i) -> startActivity(new Intent(getActivity(), MainActivity.class).putExtra("tag", clickedTag.text.substring(1, clickedTag.text.length()))));
                        },
                        e -> Log.d("myLog", "FAIL\n"+e.fillInStackTrace())
                );
    }

    private void setPreviewImageUrl(String url){
        this.previewImageUrl = url;
        flickrDB.addPhoto(mFlickrImageId, FlickrDataBaseHelper.TABLE_PREVIEW_SIZE, url);
    }

    private void loadPreviewImage(String url){

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true) //true
                .showImageOnLoading(new BitmapDrawable(getResources(), mImageLoader.loadImageSync(flickrDB.getPhoto(FlickrDataBaseHelper.TABLE_THUMB_SIZE, mFlickrImageId)))) //new
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();

        mImageLoader.displayImage(url, mImageView, options, new SimpleImageLoadingListener() {

            @Override
            public void onLoadingComplete(String imageUri, final View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);

                mImageView.setOnClickListener(v -> {
                    Intent scalingImageIntent = new Intent(getString(R.string.scaling_image_activity));
                    scalingImageIntent.putExtra(getString(R.string.extra_flickr_image_id), mFlickrImageId);
                    mAnimationController.transition(mImageView, getString(R.string.transition_image), scalingImageIntent);
                });

                mProgressBar.startAnimation(mAnimationController.getAnimation(android.R.anim.slide_out_right, new AnimationController.BaseAnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mProgressBar.setVisibility(View.GONE);

                        mProgressBar.startAnimation(mAnimationController.getAnimation(R.anim.image_on, new AnimationController.BaseAnimationListener(){
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                buttonsPanel.setVisibility(View.VISIBLE);
                                mProgressBar.startAnimation(mAnimationController.getAnimation(R.anim.show_buttons, new AnimationController.BaseAnimationListener(){
                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        mScaleFAB.setVisibility(View.VISIBLE);
                                        Animation showFAB = mAnimationController.getAnimation(R.anim.slide_in_right);
                                        showFAB.setDuration(800);
                                        mScaleFAB.startAnimation(showFAB);

                                        int width = mImageView.getDrawable().getIntrinsicWidth();
                                        additionalInform.setMinimumWidth(width);
                                        additionalInform.setVisibility(View.VISIBLE);
                                        Animation show_inform = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_bottom);
                                        additionalInform.startAnimation(show_inform);

                                        Log.d("myLog", "load tabs "+ additionalInform.getVisibility() + " " +View.VISIBLE);
                                        loadTabs();
                                    }
                                }));
                            }
                        }));
                    }
                }));
            }
        });
    }

    //// TODO: 01.09.2016
    @OnClick({R.id.set_as_btn, R.id.save_btn, R.id.favourite_btn})
    public void loadButtonsPanel(View view){

        isFavouriteImage = isFavourite((ImageButton) view.findViewById(R.id.favourite_btn));

        switch (view.getId()){
            case R.id.set_as_btn:
                Intent cropImageIntent = new Intent(getResources().getString(R.string.crop_image_activity));
                cropImageIntent.putExtra(getString(R.string.extra_flickr_image_id), mFlickrImageId);

                if(mScaleFAB.getVisibility() != View.INVISIBLE){
                    Pair<View, String> p1 = Pair.create((View) mImageView, getString(R.string.transition_image));
                    Pair<View, String> p2 = Pair.create((View) mScaleFAB, getString(R.string.transition_button));
                    mAnimationController.transition(cropImageIntent, p1, p2);
                } else {
                    startActivity(cropImageIntent);
                }

                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                //analytics.registerEvent("Preview", Analytics.BUTTON_PRESSED, "set as", 0);
                break;
            case R.id.save_btn:
                downloadImage();
                if(!isProVersion){
                    mAdvertising.loadFullScreenAd(Advertising.SAVE_ACTIVITY_AD_ID);
                }
                //analytics.registerEvent("Preview", Analytics.BUTTON_PRESSED, "save", 0);
                break;
            case R.id.favourite_btn:
                if(!isFavouriteImage){
                    Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.zoom_star);
                    ((ImageButton)view).setImageResource(R.drawable.ic_action_important);
                    ((ImageButton)view).startAnimation(anim);
                    flickrDB.addFavourite(mFlickrImageId, FlickrDatabase.FAVOURITE_PHOTO);
                    Toast.makeText(getActivity(), R.string.preview_add_to_favourite, Toast.LENGTH_LONG).show();
                } else {
                    ((ImageButton)view).setImageResource(R.drawable.ic_action_not_important);
                    flickrDB.removeFavourite(mFlickrImageId, FlickrDatabase.FAVOURITE_PHOTO);
                    Toast.makeText(getActivity(), R.string.preview_remove_from_favourite, Toast.LENGTH_LONG).show();
                }
                isFavouriteImage = !isFavouriteImage;
                break;
        }

    }

    private void downloadImage() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION);
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION);
                }
            } else {
                mDevice.downloadImage(getActivity(), previewImageUrl, mFlickrImageId);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mDevice.downloadImage(getActivity(), previewImageUrl, mFlickrImageId);
                } else {
                    Snackbar
                            .make(getActivity().findViewById(android.R.id.content) ,getString(R.string.no_permission), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.no_permission_try_again), v -> {downloadImage();}).show();
                }
            }
        }
    }

    private boolean isFavourite(ImageButton button){
        if(flickrDB.isFavourite(mFlickrImageId, FlickrDatabase.FAVOURITE_PHOTO)){
            button.setImageResource(R.drawable.ic_action_important);
            return true;
        } else return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                //onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    @Override
    protected void onBackPressed() {
        mImageLoader.getDiskCache().remove(previewImageUrl);
        super.onBackPressed();
    }*/

    @BindView(R.id.tabs) TabLayout tabLayout;
    private void loadTabs(){

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);


//        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    //todo fragment

    public static class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1, mFlickrImageId);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "AUTHOR";
                case 1:
                    return "IMAGE";
            }
            return null;
        }
    }

}
