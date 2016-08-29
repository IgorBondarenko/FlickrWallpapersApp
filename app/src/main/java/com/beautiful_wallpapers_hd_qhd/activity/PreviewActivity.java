package com.beautiful_wallpapers_hd_qhd.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import com.beautiful_wallpapers_hd_qhd.core.Advertising;
import com.beautiful_wallpapers_hd_qhd.core.Device;
import com.beautiful_wallpapers_hd_qhd.core.controller.AnimationController;
import com.beautiful_wallpapers_hd_qhd.core.controller.SharedPreferencesController;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDataBaseHelper;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDatabase;
import com.beautiful_wallpapers_hd_qhd.core.di.DaggerAppComponent;
import com.beautiful_wallpapers_hd_qhd.core.di.MyModule;
import com.beautiful_wallpapers_hd_qhd.core.entity.Author;
import com.beautiful_wallpapers_hd_qhd.core.entity.FlickrImageEXIF;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.FlickrHelper;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.FlickrAPI;
import com.beautiful_wallpapers_hd_qhd.core.view.ResizablePortraitImageView;
import com.beautiful_wallpapers_hd_qhd.core.view.helper.ListViewHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.kaede.tagview.Tag;
import me.kaede.tagview.TagView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PreviewActivity extends AppCompatActivity {

    private static final int READ_EXTERNAL_STORAGE_PERMISSION = 10;
    private ImageLoader mImageLoader = ImageLoader.getInstance();

    private boolean isFavouriteImage = false;
    private boolean isProVersion = false;
    private String previewImageUrl;

    @Inject AnimationController mAnimationController;
    @Inject SharedPreferencesController sPref;
    @Inject Advertising mAdvertising;
    @Inject static FlickrAPI flickrAPI;
    @Inject static FlickrDatabase flickrDB;
    @Inject static Device mDevice;

    private static String mFlickrImageId;

    @BindView(R.id.preview_iv) ResizablePortraitImageView mImageView;
    @BindView(R.id.buttons_panel) LinearLayout buttonsPanel;
    @BindView(R.id.load_preview_pb) ProgressBar mProgressBar;
    @BindView(R.id.container) ViewPager mViewPager;
    @BindView(R.id.additional_inform_layout) LinearLayout additionalInform;
    @BindView(R.id.set_as_btn) Button mSetAsButton;
    @BindView(R.id.save_btn) ImageButton mSaveButton;
    @BindView(R.id.favourite_btn) ImageButton mFavouriteButton;
    @BindView(R.id.scale_image_fab) FloatingActionButton mScaleFAB;

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

        mFlickrImageId = getIntent().getStringExtra(getString(R.string.extra_flickr_image_id));
        previewImageUrl = flickrDB.getPhoto(FlickrDataBaseHelper.TABLE_PREVIEW_SIZE, mFlickrImageId);

        if(previewImageUrl != null){
            loadPreviewImage(previewImageUrl);
        } else {
            flickrAPI.getPhotoSizes(FlickrHelper.METHOD_GET_PHOTO_SIZES, mFlickrImageId)
                    .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                    .map(photo -> photo.getSizes().getSizesArray().get(FlickrHelper.SIZE_LARGE).getSize())
                    .subscribe(previewUrl -> {setPreviewImageUrl(previewUrl); loadPreviewImage(previewUrl);}, e -> {finish();});
        }

        loadButtonsPanel();
        loadTags();

        if(!(isProVersion = sPref.getBool(SharedPreferencesController.SP_PRO_VERSION, false))){
            mAdvertising.loadSmartBanner(R.id.preview_ad_stub, R.id.preview_ad_view);
        }

        mScaleFAB.setOnClickListener(view -> {
            Intent scalingImageIntent = new Intent(getString(R.string.scaling_image_activity));
            scalingImageIntent.putExtra(getString(R.string.extra_flickr_image_id), mFlickrImageId);
            mAnimationController.transition(mImageView, getString(R.string.transition_image), scalingImageIntent);
        });
    }

    private void loadTags(){

        TagView tagView = (TagView)findViewById(R.id.tagview);
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
                            tagView.setOnTagClickListener((clickedTag, i) -> startActivity(new Intent(getBaseContext(), MainActivity.class).putExtra("tag", clickedTag.text.substring(1, clickedTag.text.length()))));
                        },
                        e -> Log.d("myLog", "FAIL\n"+e.fillInStackTrace())
                );
    }

    private void setPreviewImageUrl(String url){
        this.previewImageUrl = url;
        flickrDB.addPhoto(mFlickrImageId, FlickrDataBaseHelper.TABLE_PREVIEW_SIZE, url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_preview, menu);
        return true;
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

        View.OnClickListener ocl = v -> {
            switch (v.getId()){
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

                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
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
                        Animation anim = AnimationUtils.loadAnimation(getApplication(), R.anim.zoom_star);
                        mFavouriteButton.setImageResource(R.drawable.ic_action_important);
                        mFavouriteButton.startAnimation(anim);
                        flickrDB.addFavourite(mFlickrImageId, FlickrDatabase.FAVOURITE_PHOTO);
                        Toast.makeText(getBaseContext(), R.string.preview_add_to_favourite, Toast.LENGTH_LONG).show();
                    } else {
                        mFavouriteButton.setImageResource(R.drawable.ic_action_not_important);
                        flickrDB.removeFavourite(mFlickrImageId, FlickrDatabase.FAVOURITE_PHOTO);
                        Toast.makeText(getBaseContext(), R.string.preview_remove_from_favourite, Toast.LENGTH_LONG).show();
                    }
                    isFavouriteImage = !isFavouriteImage;
                    break;
            }
        };
        mSetAsButton.setOnClickListener(ocl);
        mSaveButton.setOnClickListener(ocl);
        mFavouriteButton.setOnClickListener(ocl);
    }

    private void downloadImage() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(PreviewActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(PreviewActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(PreviewActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION);
                } else {
                    ActivityCompat.requestPermissions(PreviewActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION);
                }
            } else {
                mDevice.downloadImage(PreviewActivity.this, previewImageUrl, mFlickrImageId);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mDevice.downloadImage(PreviewActivity.this, previewImageUrl, mFlickrImageId);
                } else {
                    Snackbar
                            .make(findViewById(android.R.id.content) ,getString(R.string.no_permission), Snackbar.LENGTH_LONG)
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
            case R.id.action_settings:
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        mImageLoader.getDiskCache().remove(previewImageUrl);
        super.onBackPressed();
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
            final ListView listView = (ListView) rootView.findViewById(R.id.information_lv);
            listView.setScrollContainer(false);

            final RelativeLayout viewMore = (RelativeLayout) rootView.findViewById(R.id.view_more_info);

            final ProgressBar informationLoadingPB = (ProgressBar)rootView.findViewById(R.id.information_loading_pb);
            final ListViewHelper listViewHelper = new ListViewHelper(getActivity());

            switch (getArguments().getInt(ARG_SECTION_NUMBER)){
                case 1:
                    textView.setText(R.string.preview_author_label);
                    flickrAPI.getPhotoInformation(FlickrHelper.METHOD_PHOTOS_GET_INFO, mFlickrImageId)
                            .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                            .map(photoInformation -> photoInformation.getPhoto())
                            .map(photo -> {
                                final Author author = new Author();
                                author.setNsid(photo.getOwner().getNsid());
                                author.setRealName(photo.getOwner().getRealName());
                                author.setUserName(photo.getOwner().getUserName());
                                author.setLocation(photo.getOwner().getLocation());
                                author.setLicenseNumber(Integer.parseInt(photo.getLicense()));

                                flickrAPI.getAuthorIcon(FlickrHelper.METHOD_PEOPLE_GET_INFO, photo.getOwner().getNsid())
                                        .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                                        .map(userIcon -> userIcon.getIcon())
                                        .subscribe(icon -> {
                                            String nsid = icon.getNsid(); String iconfarm = icon.getIconFarm(); String iconserver = icon.getIconServer();
                                            author.setUserAvatar(Integer.valueOf(iconserver) == 0 ? null : FlickrHelper.getUserAvatar(iconfarm, iconserver, nsid));

                                            if(flickrDB.getAuthor(nsid) == null){
                                                flickrDB.addAuthor(author);
                                            } else {
                                                flickrDB.updateAuthor(author);
                                            }
                                        });
                                return author;
                            })
                            .doOnCompleted(() -> informationLoadingPB.setVisibility(View.GONE))
                            .subscribe(
                                    author -> {
                                        viewMore.setOnClickListener(getOnClickListener(rootView, InformationCardDialog.AUTHOR_INFORMATION, author));
                                        listView.setOnItemClickListener(listViewHelper.getAuthorOnClickListener(author));
                                        listViewHelper.setupAdapter(author.getIcons(), new String[]{}, author.toArray(), listView);
                                    },
                                    e -> Log.d("RX-JaVa", "ERROR: "+e.fillInStackTrace())
                            );

                    break;
                case 2:
                    textView.setText(R.string.preview_image_label);
                    Observable.zip(
                            flickrAPI.getImageEXIF(FlickrHelper.METHOD_GET_EXIF, mFlickrImageId)
                                    .map(exif -> exif.getPhoto()),
                            flickrAPI.getPhotoSizes(FlickrHelper.METHOD_GET_PHOTO_SIZES, mFlickrImageId)
                                    .map(sizes -> sizes.getSizes().getSizesArray().get(sizes.getSizes().getSizesArray().size() - 1).getResolution()),
                            (image, resolution) -> {
                                final FlickrImageEXIF imageData = new FlickrImageEXIF();
                                if(image != null){
                                    String camera = image.getExif(FlickrHelper.EXIF_CAMERA);
                                    String cameraModel = image.getExif(FlickrHelper.EXIF_CAMERA_MODEL);
                                    imageData.setCamera(camera != null ? cameraModel.contains(camera) ? cameraModel : camera + " " + cameraModel : NO_INFORMATION);
                                    imageData.setAperture(compare(image.getExif(FlickrHelper.EXIF_APERTURE)));
                                    imageData.setFocalLength(compare(image.getExif(FlickrHelper.EXIF_FOCAL_LENGTH)));
                                    imageData.setISO(compare(image.getExif(FlickrHelper.EXIF_ISO)));
                                    imageData.setExposureTime(compare(image.getExif(FlickrHelper.EXIF_EXPOSURE_TIME)));
                                    imageData.setWhiteBalance(compare(image.getExif(FlickrHelper.EXIF_WHITE_BALANCE)));
                                    imageData.setMeteringMode(compare(image.getExif(FlickrHelper.EXIF_METERING_MODE)));
                                    imageData.setExposureMode(compare(image.getExif(FlickrHelper.EXIF_EXPOSURE_MODE)));
                                } else {
                                    imageData.setCamera(NO_INFORMATION);
                                    imageData.setAperture(NO_INFORMATION);
                                    imageData.setLocked(true);
                                }
                                imageData.setResolution(resolution);

                                return imageData;
                            })
                                .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                                .doOnCompleted(() -> informationLoadingPB.setVisibility(View.GONE))
                                .subscribe(
                                        image -> {
                                            if(!image.isLocked()){
                                                listView.setOnItemClickListener(listViewHelper.getImageOnItemClickListener(image));
                                                viewMore.setOnClickListener(getOnClickListener(rootView, InformationCardDialog.IMAGE_INFORMATION, image));
                                            } else
                                                viewMore.setVisibility(View.GONE);
                                            //todo check getActivity()
                                            listViewHelper.setupAdapter(image.getIcons(), image.getEXIFTitles(getActivity()), image.toArray(), listView);
                                        },
                                        e -> Log.d("RX-JaVa", "ERROR: "+e.fillInStackTrace())
                                );
                    break;
            }
            return rootView;
        }

        private String compare(String str){
            return str != null ? str : NO_INFORMATION;
        }

        private View.OnClickListener getOnClickListener(final View rootView, final int informNum, final Object object){
            return v -> {
                CardView card = (CardView) rootView.findViewById(R.id.information_card);
                Intent dialog = new Intent(getString(R.string.card_information_dialog));
                dialog.putExtra("inform_num", informNum);
                switch (informNum){
                    case InformationCardDialog.AUTHOR_INFORMATION:
                        dialog.putExtra("author_obj", (Author)object);
                        break;
                    case InformationCardDialog.IMAGE_INFORMATION:
                        dialog.putExtra("image_obj", (FlickrImageEXIF)object);
                        break;
                }
                new AnimationController(getContext()).transition(card, getString(R.string.transition_card), dialog);
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
