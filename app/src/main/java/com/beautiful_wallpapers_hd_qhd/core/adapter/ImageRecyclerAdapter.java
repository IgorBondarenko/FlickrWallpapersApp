package com.beautiful_wallpapers_hd_qhd.core.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;

import com.beautiful_wallpapers_hd_qhd.R;
import com.beautiful_wallpapers_hd_qhd.core.controller.AnimationController;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDataBaseHelper;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDatabase;
import com.beautiful_wallpapers_hd_qhd.core.di.DaggerAppComponent;
import com.beautiful_wallpapers_hd_qhd.core.di.MyModule;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.FlickrHelper;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.FlickrAPI;
import com.etsy.android.grid.util.DynamicHeightImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Igor on 08.06.2016.
 */
public class ImageRecyclerAdapter extends RecyclerView.Adapter<ImageRecyclerAdapter.ImageViewHolder> {

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        DynamicHeightImageView thumbnailImage;
        ImageView favouriteImage;

        public ImageViewHolder(View itemView) {
            super(itemView);
            this.thumbnailImage = (DynamicHeightImageView)itemView.findViewById(R.id.small_img);
            this.favouriteImage = (ImageView)itemView.findViewById(R.id.favourite_img);
        }

    }

    private static final SparseArray<Double> sPositionHeightRatios = new SparseArray<Double>();
    private static String mCategory;

    @Inject AnimationController mAnimationController;
    @Inject FlickrAPI flickrAPI;
    @Inject FlickrDatabase flickrDB;

    private List<String> mImageFlickrIds;
    private Context mContext;

    public ImageRecyclerAdapter(Context context, List<String> mImageFlickrIds, String category) {
        this.mContext = context;
        this.mImageFlickrIds = mImageFlickrIds;
        mCategory = category;
        sPositionHeightRatios.clear();
        DaggerAppComponent.builder().myModule(new MyModule(mContext)).build().inject(this);
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ImageViewHolder holder, final int position) {
        holder.thumbnailImage.setHeightRatio(getPositionRatio(holder.getLayoutPosition()));

        Observable.just(flickrDB.getPhoto(FlickrDataBaseHelper.TABLE_THUMB_SIZE, mImageFlickrIds.get(holder.getLayoutPosition())))
                .subscribe(url -> {
                    if(url != null){
                        loadImage(url, holder.thumbnailImage, holder.itemView, holder.favouriteImage, holder.getLayoutPosition());
                    } else {
                        flickrAPI.getPhotoSizes(FlickrHelper.METHOD_GET_PHOTO_SIZES, mImageFlickrIds.get(holder.getLayoutPosition()))
                                .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                                .map(photoSizes -> photoSizes.getSizes().getSizesArray().get(FlickrHelper.SIZE_THUMBNAIL).getSize())
                                .subscribe(thumbUrl -> {
                                    flickrDB.addPhoto(mImageFlickrIds.get(holder.getLayoutPosition()), FlickrDataBaseHelper.TABLE_THUMB_SIZE, thumbUrl);
                                    loadImage(thumbUrl, holder.thumbnailImage, holder.itemView, holder.favouriteImage, holder.getLayoutPosition());
                                });
                    }
                });
        holder.itemView.setOnClickListener(v -> {
            final Intent previewIntent = new Intent(mContext.getResources().getString(R.string.preview_activity));
            previewIntent.putExtra(mContext.getString(R.string.extra_flickr_image_id), mImageFlickrIds.get(holder.getLayoutPosition()));

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
                mAnimationController.transition(holder.thumbnailImage, mContext.getString(R.string.transition_image), previewIntent);
            } else{
                mAnimationController.zoomCenter(holder.thumbnailImage, previewIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mImageFlickrIds.size();
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

                Animation.AnimationListener animationListener = new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if(!mCategory.equals("favourite")){
                            if(flickrDB.isFavourite(mImageFlickrIds.get(position), FlickrDatabase.FAVOURITE_PHOTO)){
                                favouriteIcon.setVisibility(View.VISIBLE);
                                favouriteIcon.startAnimation(mAnimationController.getAnimation(R.anim.zoom_grid_elem));
                            }
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                };
                layout.startAnimation(mAnimationController.getAnimation(R.anim.zoom_grid_elem, animationListener));

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
