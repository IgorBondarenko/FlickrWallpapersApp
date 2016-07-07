package com.beautiful_wallpapers_hd_qhd.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beautiful_wallpapers_hd_qhd.R;
import com.beautiful_wallpapers_hd_qhd.activity.dialog.InformationCardDialog;
import com.beautiful_wallpapers_hd_qhd.core.Device;
import com.beautiful_wallpapers_hd_qhd.core.controller.AnimationController;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDataBaseHelper;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDatabase;
import com.beautiful_wallpapers_hd_qhd.core.di.DaggerAppComponent;
import com.beautiful_wallpapers_hd_qhd.core.di.MyModule;
import com.beautiful_wallpapers_hd_qhd.core.entity.Author;
import com.beautiful_wallpapers_hd_qhd.core.entity.FlickrImageEXIF;
import com.beautiful_wallpapers_hd_qhd.core.flickr.FlickrHelper;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.FlickrAPI;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.enteties.ImageEXIF;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.enteties.PhotoInformation;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.enteties.PhotoSizes;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.enteties.UserIcon;
import com.beautiful_wallpapers_hd_qhd.core.view.ResizablePortraitImageView;
import com.beautiful_wallpapers_hd_qhd.core.view.helper.ListViewHelper;
import com.google.android.gms.ads.AdSize;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.kaede.tagview.OnTagClickListener;
import me.kaede.tagview.Tag;
import me.kaede.tagview.TagView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreviewActivity extends AppCompatActivity {

    private static final int READ_EXTERNAL_STORAGE_PERMISSION = 10;
    private ImageLoader mImageLoader = ImageLoader.getInstance();

    private boolean isFavouriteImage = false;
    private String previewImageUrl;

    @Inject AnimationController mAnimationController;
    @Inject static FlickrAPI flickrAPI;
    @Inject static FlickrDatabase flickrDB;
    @Inject static Device mDevice;

    private static String mFlickrIamgeId;
    private static Context mContext;

    @BindView(R.id.preview_iv) ResizablePortraitImageView mImageView;
    @BindView(R.id.buttons_panel) LinearLayout buttonsPanel;
    @BindView(R.id.load_preview_pb) ProgressBar mProgressBar;
    @BindView(R.id.container) ViewPager mViewPager;
    @BindView(R.id.additional_inform_layout) LinearLayout additionalInform;
    @BindView(R.id.set_as_btn) Button mSetAsButton;
    @BindView(R.id.save_btn) ImageButton mSaveButton;
    @BindView(R.id.favourite_btn) ImageButton mFavouriteButton;
    @BindView(R.id.scale_image_fab) FloatingActionButton mScaleFAB;

    public PreviewActivity(){
        mContext = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DaggerAppComponent.builder().myModule(new MyModule(this)).build().inject(this);
        ButterKnife.bind(this);

        mFlickrIamgeId = getIntent().getStringExtra("flickrImageId");

        mImageLoader.displayImage(flickrDB.getPhoto(FlickrDataBaseHelper.TABLE_THUMB_SIZE, mFlickrIamgeId), mImageView);
        previewImageUrl = flickrDB.getPhoto(FlickrDataBaseHelper.TABLE_PREVIEW_SIZE, mFlickrIamgeId);

        if(previewImageUrl != null){
            loadPreviewImage(previewImageUrl);
        } else {

            Call<PhotoSizes> call = flickrAPI.getPhotoSizes(FlickrHelper.METHOD_GET_PHOTO_SIZES, mFlickrIamgeId);
            call.enqueue(new Callback<PhotoSizes>() {
                @Override
                public void onResponse(Call<PhotoSizes> call, Response<PhotoSizes> response) {
                    String previewUrl = response.body().getSizes().getSizesArray().get(FlickrHelper.SIZE_LARGE).getSize();
                    setPreviewImageUrl(previewUrl);
                    loadPreviewImage(previewUrl);
                }

                @Override
                public void onFailure(Call<PhotoSizes> call, Throwable t) {

                }
            });
        }

        loadButtonsPanel();
        loadTags();

        //todo AD
        //boolean IS_PRO = false;
        //test_new Advertising(this, IS_PRO).loadSmartBanner((AdView) findViewById(R.id.preview_ad_view));

        LinearLayout adStub = (LinearLayout)findViewById(R.id.preview_ad_stub);
        AdSize smartBanner = AdSize.SMART_BANNER;
        adStub.setMinimumHeight(smartBanner.getHeightInPixels(this));

        mScaleFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent scalingImageIntent = new Intent("com.beautiful_wallpapers_hd_qhd.SCALING_IMAGE_ACTIVITY");
                scalingImageIntent.putExtra("flickrImageId", mFlickrIamgeId);
                mAnimationController.transition(mImageView, "transition_image", scalingImageIntent);
            }
        });
    }

    private void loadTags(){
        flickrAPI.getPhotoInformation(FlickrHelper.METHOD_PHOTOS_GET_INFO, mFlickrIamgeId).enqueue(new Callback<PhotoInformation>() {
            @Override
            public void onResponse(Call<PhotoInformation> call, Response<PhotoInformation> response) {
                TagView tagView = (TagView)findViewById(R.id.tagview);
                List<PhotoInformation.Photo.Tags.Tag> tags = response.body().getPhoto().getTags();
                if(tags != null){
                    for (PhotoInformation.Photo.Tags.Tag tagText : tags) {
                        Tag tag = new Tag("#"+tagText.getTag());
                        tag.background = getResources().getDrawable(R.drawable.capsule_button);
                        tagView.addTag(tag);
                    }
                }

                tagView.setOnTagClickListener(new OnTagClickListener() {
                    @Override
                    public void onTagClick(Tag tag, int i) {
                        Intent tagSearchIntent = new Intent(getBaseContext(), MainActivity.class);
                        tagSearchIntent.putExtra("tag", tag.text.substring(1, tag.text.length()));
                        startActivity(tagSearchIntent);
                    }
                });
            }

            @Override
            public void onFailure(Call<PhotoInformation> call, Throwable t) {
                Log.d("myLog", "FAIL\n"+t.fillInStackTrace());
            }
        });
    }

    private void setPreviewImageUrl(String url){
        this.previewImageUrl = url;
        flickrDB.addPhoto(mFlickrIamgeId, FlickrDataBaseHelper.TABLE_PREVIEW_SIZE, url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_preview, menu);
        return true;
    }

    private void loadPreviewImage(String url){

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();

        mImageLoader.displayImage(url, mImageView, options, new SimpleImageLoadingListener() {

            @Override
            public void onLoadingComplete(String imageUri, final View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);

                mImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent scalingImageIntent = new Intent("com.beautiful_wallpapers_hd_qhd.SCALING_IMAGE_ACTIVITY");
                        scalingImageIntent.putExtra("flickrImageId", mFlickrIamgeId);
                        mAnimationController.transition(mImageView, "transition_image", scalingImageIntent);
                    }
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
                                        Animation show_inform = AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_in_bottom);
                                        additionalInform.startAnimation(show_inform);

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

    private void loadButtonsPanel(){
        isFavouriteImage = isFavourite(mFavouriteButton);

        View.OnClickListener ocl = new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.set_as_btn:
                        Intent cropImageIntent = new Intent(getResources().getString(R.string.crop_image_activity));
                        cropImageIntent.putExtra("flickrImageId", mFlickrIamgeId);
                        Pair<View, String> p1 = Pair.create((View) mImageView, "transition_image");
                        Pair<View, String> p2 = Pair.create((View) mScaleFAB, "transition_button");
                        mAnimationController.transition(cropImageIntent, p1, p2);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                        //analytics.registerEvent("Preview", Analytics.BUTTON_PRESSED, "set as", 0);
                        break;
                    case R.id.save_btn:
                        downloadImage();
                        //analytics.registerEvent("Preview", Analytics.BUTTON_PRESSED, "save", 0);
                        break;
                    case R.id.favourite_btn:
                        if(!isFavouriteImage){
                            Animation anim = AnimationUtils.loadAnimation(getApplication(), R.anim.zoom_star);
                            mFavouriteButton.setImageResource(R.drawable.ic_action_important);
                            mFavouriteButton.startAnimation(anim);
                            flickrDB.addFavourite(mFlickrIamgeId, FlickrDatabase.FAVOURITE_PHOTO);
                            Toast.makeText(getBaseContext(), R.string.preview_add_to_favourite, Toast.LENGTH_LONG).show();
                        } else {
                            mFavouriteButton.setImageResource(R.drawable.ic_action_not_important);
                            flickrDB.removeFavourite(mFlickrIamgeId, FlickrDatabase.FAVOURITE_PHOTO);
                            Toast.makeText(getBaseContext(), R.string.preview_remove_from_favourite, Toast.LENGTH_LONG).show();
                        }
                        isFavouriteImage = !isFavouriteImage;
                        break;
                }
            }
        };
        mSetAsButton.setOnClickListener(ocl);
        mSaveButton.setOnClickListener(ocl);
        mFavouriteButton.setOnClickListener(ocl);
    }

    private void downloadImage() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((PreviewActivity)mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions((PreviewActivity)mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION);
                } else {
                    ActivityCompat.requestPermissions((PreviewActivity)mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION);
                }
            } else {
                mDevice.downloadImage(mContext, previewImageUrl, mFlickrIamgeId);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mDevice.downloadImage(mContext, previewImageUrl, mFlickrIamgeId);
                } else {
                    Snackbar.make(findViewById(android.R.id.content) ,getString(R.string.no_permission), Snackbar.LENGTH_LONG).setAction(getString(R.string.no_permission_try_again), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            downloadImage();
                        }
                    }).show();
                }
            }
        }
    }

    private boolean isFavourite(ImageButton button){
        if(flickrDB.isFavourite(mFlickrIamgeId, FlickrDatabase.FAVOURITE_PHOTO)){
            button.setImageResource(R.drawable.ic_action_important);
            return true;
        } else return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                return true;
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadTabs(){

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a test_new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        private final String NO_INFORMATION = "No information";

        @Override
        public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.content_card_preview, container, false);

            final TextView textView = (TextView) rootView.findViewById(R.id.inform_card_title);
            final ListView listView = (ListView) rootView.findViewById(R.id.information_lw);

            final RelativeLayout viewMore = (RelativeLayout) rootView.findViewById(R.id.view_more_info);

            final ProgressBar informationLoadingPB = (ProgressBar)rootView.findViewById(R.id.information_loading_pb);
            final ListViewHelper listViewHelper = new ListViewHelper(getActivity());

            switch (getArguments().getInt(ARG_SECTION_NUMBER)){
                case 1:
                    textView.setText(R.string.preview_author_label);
                    informationLoadingPB.setVisibility(View.GONE);

                    flickrAPI.getPhotoInformation(FlickrHelper.METHOD_PHOTOS_GET_INFO, mFlickrIamgeId).enqueue(new Callback<PhotoInformation>() {
                        @Override
                        public void onResponse(Call<PhotoInformation> call, Response<PhotoInformation> response) {
                            informationLoadingPB.setVisibility(View.GONE);

                            final Author author = new Author();
                            author.setNsid(response.body().getPhoto().getOwner().getNsid());
                            author.setRealName(response.body().getPhoto().getOwner().getRealName());
                            author.setUserName(response.body().getPhoto().getOwner().getUserName());
                            author.setLocation(response.body().getPhoto().getOwner().getLocation());
                            author.setLicenseNumber(Integer.parseInt(response.body().getPhoto().getLicense()));

                            viewMore.setOnClickListener(getOnClickListener(rootView, InformationCardDialog.AUTHOR_INFORMATION, author));

                            flickrAPI.getAuthorIcon(FlickrHelper.METHOD_PEOPLE_GET_INFO, author.getNsid()).enqueue(new Callback<UserIcon>() {
                                @Override
                                public void onResponse(Call<UserIcon> call, Response<UserIcon> response) {
                                    String nsid = response.body().getIcon().getNsid();
                                    String iconfarm = response.body().getIcon().getIconFarm();
                                    String iconserver = response.body().getIcon().getIconServer();

                                    if(flickrDB.getAuthor(nsid) == null){
                                        String userAvatar = Integer.valueOf(iconserver) == 0 ? null : FlickrHelper.getUserAvatar(iconfarm, iconserver, nsid);
                                        author.setUserAvatar(userAvatar);
                                        flickrDB.addAuthor(author);
                                    }
                                }

                                @Override
                                public void onFailure(Call<UserIcon> call, Throwable t) {

                                }
                            });

                            listView.setOnItemClickListener(listViewHelper.getAuthorOnClickListener(author));
                            listViewHelper.setupAdapter(author.getIcons(), new String[]{}, author.toArray(), listView);
                        }

                        @Override
                        public void onFailure(Call<PhotoInformation> call, Throwable t) {

                        }
                    });
                    break;
                case 2:
                    textView.setText(R.string.preview_image_label);

                    flickrAPI.getImageEXIF(FlickrHelper.METHOD_GET_EXIF, mFlickrIamgeId).enqueue(new Callback<ImageEXIF>() {
                        @Override
                        public void onResponse(Call<ImageEXIF> call, Response<ImageEXIF> response) {
                            informationLoadingPB.setVisibility(View.GONE);

                            final FlickrImageEXIF image = new FlickrImageEXIF();

                            if(response.body().getPhoto() != null){
                                String camera = response.body().getPhoto().getExif(FlickrHelper.EXIF_CAMERA);
                                String cameraModel = response.body().getPhoto().getExif(FlickrHelper.EXIF_CAMERA_MODEL);
                                image.setCamera(camera != null ? cameraModel.contains(camera) ? cameraModel : camera + " " + cameraModel : NO_INFORMATION);
                                image.setResolution("1920x1080");
                                image.setAperture(compare(response.body().getPhoto().getExif(FlickrHelper.EXIF_APERTURE)));
                                image.setFocalLength(compare(response.body().getPhoto().getExif(FlickrHelper.EXIF_FOCAL_LENGTH)));
                                image.setISO(compare(response.body().getPhoto().getExif(FlickrHelper.EXIF_ISO)));
                                image.setExposureTime(compare(response.body().getPhoto().getExif(FlickrHelper.EXIF_EXPOSURE_TIME)));
                                image.setWhiteBalance(compare(response.body().getPhoto().getExif(FlickrHelper.EXIF_WHITE_BALANCE)));
                                image.setMeteringMode(compare(response.body().getPhoto().getExif(FlickrHelper.EXIF_METERING_MODE)));
                                image.setExposureMode(compare(response.body().getPhoto().getExif(FlickrHelper.EXIF_EXPOSURE_MODE)));

                                listView.setOnItemClickListener(listViewHelper.getImageOnItemClickListener(image));
                                listViewHelper.setupAdapter(image.getIcons(), image.getEXIFTitles(mContext), image.toArray(), listView);

                                viewMore.setOnClickListener(getOnClickListener(rootView, InformationCardDialog.IMAGE_INFORMATION, image));
                            } else {
                                // TODO: 05.07.2016 NO INFORMATION STUB
                            }

                        }

                        @Override
                        public void onFailure(Call<ImageEXIF> call, Throwable t) {

                        }
                    });
                    break;
            }
            return rootView;
        }

        private String compare(String str){
            return str != null ? str : NO_INFORMATION;
        }

        private View.OnClickListener getOnClickListener(final View rootView, final int informNum, final Object object){
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CardView card = (CardView) rootView.findViewById(R.id.information_card);
                    Intent dialog = new Intent("com.beautiful_wallpapers_hd_qhd.DIALOG_CARD_INFORMATION");
                    dialog.putExtra("inform_num", informNum);
                    switch (informNum){
                        case InformationCardDialog.AUTHOR_INFORMATION:
                            dialog.putExtra("author_obj", (Author)object);
                            break;
                        case InformationCardDialog.IMAGE_INFORMATION:
                            dialog.putExtra("image_obj", (FlickrImageEXIF)object);
                            break;
                    }
                    new AnimationController(getContext()).transition(card, "transition_card", dialog);
                }
            };
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
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
