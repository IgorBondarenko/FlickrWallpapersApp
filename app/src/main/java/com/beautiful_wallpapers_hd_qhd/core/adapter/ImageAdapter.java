package com.beautiful_wallpapers_hd_qhd.core.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.beautiful_wallpapers_hd_qhd.core.controller.AnimationController;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDataBaseHelper;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDatabase;
import com.beautiful_wallpapers_hd_qhd.core.flickr.RequestLoadListener;
import com.etsy.android.grid.util.DynamicHeightImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.beautiful_wallpapers_hd_qhd.R;
import com.beautiful_wallpapers_hd_qhd.core.flickr.FlickrHelper;

import java.util.List;

/**
 * Created by Igor on 06.10.2014.
 */
public class ImageAdapter extends BaseAdapter {

    private final FlickrHelper flickrHelper = new FlickrHelper();

    private static final SparseArray<Double> sPositionHeightRatios = new SparseArray<Double>();
    private static String mCategory;

    private AnimationController mAnimationController;
    private LayoutInflater mLayoutInflater;
    private List<String> mImageFlickrIds;
    private Context mContext;
    public FlickrDatabase flickrDB;

    public ImageAdapter(Context context, List<String> flickrIds, String category) {
        this.mContext = context;
        this.mImageFlickrIds = flickrIds;
        sPositionHeightRatios.clear();
        mCategory = category;

        /*if(ImageAdapter.category != null && !ImageAdapter.category.equals(category)){
            sPositionHeightRatios.clear();
        }*/

        this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.flickrDB = new FlickrDatabase(context);
        this.mAnimationController = new AnimationController(context);
    }

    public int getCount() {
        return mImageFlickrIds.size();
    }

    @Override
    public Object getItem(int position) {
        return mImageFlickrIds.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        DynamicHeightImageView thumbnailImage;
        ImageView favouriteImage;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder vh;
        if(convertView == null){
            convertView = mLayoutInflater.inflate(R.layout.layout_image, parent, false);
            convertView.setVisibility(View.VISIBLE);
            vh = new ViewHolder();
            vh.thumbnailImage = (DynamicHeightImageView)convertView.findViewById(R.id.small_img);
            vh.favouriteImage = (ImageView)convertView.findViewById(R.id.favourite_img);
            convertView.setTag(R.string.image_view_holder, vh);
        } else {
            vh = (ViewHolder) convertView.getTag(R.string.image_view_holder);
        }
        vh.thumbnailImage.setHeightRatio(getPositionRatio(position));

        String url = flickrDB.getPhoto(FlickrDataBaseHelper.TABLE_THUMB_SIZE, mImageFlickrIds.get(position));

        final View finalConvertView = convertView;
        if(url != null){
            loadImage(url, vh.thumbnailImage, finalConvertView, vh.favouriteImage, position);
        } else {
            flickrHelper.processRequest(FlickrHelper.METHOD_GET_PHOTO_SIZES, FlickrHelper.ARG_GET_PHOTO_ID, mImageFlickrIds.get(position), new RequestLoadListener() {
                @Override
                public void onLoad(byte[] responseBody) {
                    String thumbUrl = flickrHelper.getValue(responseBody, "sizes", "size", FlickrHelper.SIZE_THUMBNAIL, "source");
                    flickrDB.addPhoto(mImageFlickrIds.get(position), FlickrDataBaseHelper.TABLE_THUMB_SIZE, thumbUrl);
                    loadImage(thumbUrl, vh.thumbnailImage, finalConvertView, vh.favouriteImage, position);
                }

                @Override
                public void onFail(int statusCode, Throwable error) {

                }
            });
        }

        convertView.startAnimation(mAnimationController.getAnimation(R.anim.zoom_grid_elem));
        touchEventsProcessing(convertView, vh.favouriteImage, position);
        return convertView;
    }

    private double getPositionRatio(final int position) {
        return sPositionHeightRatios.get(position, 0.0);
    }

    private DisplayImageOptions getDisplayImageOptions(double ratio){
        return new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .showImageOnLoading(ratio == 0 ? R.drawable.background : ratio > 1 ? R.drawable.background_portrait : R.drawable.background_land)
                .imageScaleType(ImageScaleType.NONE)
                .build();
    }

    private void loadImage(final String url, final ImageView thumbImage, final View layout, final ImageView favouriteIcon, final int position) {
        ImageLoader.getInstance().displayImage(url, thumbImage, getDisplayImageOptions(getPositionRatio(position)), new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);

                sPositionHeightRatios.append(position, (double)loadedImage.getHeight()/(double)loadedImage.getWidth());
                favouriteIcon.setVisibility(View.INVISIBLE);
                if(!mCategory.equals("favourite")){
                    Animation.AnimationListener animationListener = new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if(flickrDB.isFavourite(mImageFlickrIds.get(position), FlickrDatabase.FAVOURITE_PHOTO)){
                                favouriteIcon.setVisibility(View.VISIBLE);
                                favouriteIcon.startAnimation(mAnimationController.getAnimation(R.anim.zoom_grid_elem));
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    };
                    layout.startAnimation(mAnimationController.getAnimation(R.anim.zoom_grid_elem, animationListener));
                }
            }
        });
    }

    private void touchEventsProcessing(final View layout, final ImageView favouriteImage, final int position){

        final GestureDetector detector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {

            private static final float ALLOWED_PATH = 200;

            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {}

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                final Intent previewIntent = new Intent(mContext.getResources().getString(R.string.preview_activity));
                previewIntent.putExtra(mContext.getString(R.string.extra_flickr_image_id), mImageFlickrIds.get(position));

                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
                    mAnimationController.transition(layout, mContext.getString(R.string.transition_image), previewIntent);
                } else{
                    mAnimationController.zoomCenter(layout, previewIntent);
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                System.out.println(distanceX + "| " + e1.getX() + " = " + e2.getX() + " | " + (e2.getX() - e1.getX()));
                float path = e2.getX() - e1.getX();
                if(path > ALLOWED_PATH){
                    onSwipeRight();
                    return true;
                } else if(path < -ALLOWED_PATH){
                    onSwipeLeft();
                    return true;
                }
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {}

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }

            private void onSwipeRight() {
//                test_new Analytics(mContext).registerEvent("Gallery", Analytics.SWIPE, "right"+dao.getStringValueByURL(url, "category") + "_category_id-" +  dao.getIntegerValueByURL(url, "cat_index"), 0);

                if(!mCategory.equals("favourite")){
                    if(!flickrDB.isFavourite(mImageFlickrIds.get(position), FlickrDatabase.FAVOURITE_PHOTO)){
                        Animation.AnimationListener animationListener = new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                Toast.makeText(mContext, R.string.preview_add_to_favourite, Toast.LENGTH_LONG).show();
                                flickrDB.addFavourite(mImageFlickrIds.get(position), FlickrDatabase.FAVOURITE_PHOTO);
                                favouriteImage.setVisibility(View.VISIBLE);
                                favouriteImage.startAnimation(mAnimationController.getAnimation(R.anim.zoom_grid_elem));
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        };
                        layout.startAnimation(mAnimationController.getAnimation(R.anim.slide_out_right, animationListener));
                    } else {
                        layout.startAnimation(mAnimationController.getAnimation(R.anim.already_added));
                    }
                }
            }

            private void onSwipeLeft(){
//                test_new Analytics(mContext).registerEvent("Gallery", Analytics.SWIPE, "left", 0);

                if(mCategory.equals("favourite")){
                    Animation.AnimationListener animationListener = new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            Toast.makeText(mContext, R.string.preview_remove_from_favourite, Toast.LENGTH_LONG).show();
                            flickrDB.removeFavourite(mImageFlickrIds.get(position), FlickrDatabase.FAVOURITE_PHOTO);
                            mImageFlickrIds.remove(position);
                            sPositionHeightRatios.clear();
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    };
                    layout.startAnimation(mAnimationController.getAnimation(R.anim.slide_out_left, animationListener));
                }
            }

        });

        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                detector.onTouchEvent(event);
                return true;
            }
        });

    }
}
