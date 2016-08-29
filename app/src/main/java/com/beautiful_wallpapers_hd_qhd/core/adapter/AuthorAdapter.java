package com.beautiful_wallpapers_hd_qhd.core.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.beautiful_wallpapers_hd_qhd.R;
import com.beautiful_wallpapers_hd_qhd.core.controller.AnimationController;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDatabase;
import com.beautiful_wallpapers_hd_qhd.core.di.DaggerAppComponent;
import com.beautiful_wallpapers_hd_qhd.core.di.MyModule;
import com.beautiful_wallpapers_hd_qhd.core.entity.Author;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.FlickrHelper;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.FlickrAPI;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.enteties.UserIcon;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.List;

import javax.inject.Inject;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Igor on 10.04.2016.
 */
public class AuthorAdapter extends BaseAdapter {

    @Inject FlickrDatabase flickrDB;
    @Inject FlickrAPI flickrAPI;

    private AnimationController mAnimationController;
    private LayoutInflater mLayoutInflater;
    private List<String> mAuthorFlickrIds;

    public AuthorAdapter(Context context, List<String> authorIds){
        this.mAuthorFlickrIds = authorIds;
        this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mAnimationController = new AnimationController(context);
        DaggerAppComponent.builder().myModule(new MyModule(context)).build().inject(this);
    }
    static class ViewHolder {
        CircleImageView image;
        TextView name;
    }

    @Override
    public int getCount() {
        return mAuthorFlickrIds.size();
    }

    @Override
    public Object getItem(int position) {
        return mAuthorFlickrIds.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder vh;

        if(convertView == null){
            convertView = mLayoutInflater.inflate(R.layout.layout_subscription, parent, false);
            vh = new ViewHolder();
            vh.image = (CircleImageView)convertView.findViewById(R.id.sub_profile_image);
            vh.name = (TextView)convertView.findViewById(R.id.sub_profile_name);
            convertView.setTag(R.string.author_view_holder, vh);
        } else {
            vh = (ViewHolder) convertView.getTag(R.string.author_view_holder);
        }

        Author author = flickrDB.getAuthor(mAuthorFlickrIds.get(position));
        if(author != null){
            vh.name.setText(!author.getRealName().equals("") ? author.getRealName() + "\n" + author.getUserName() : author.getUserName());

            flickrAPI.getAuthorIcon(FlickrHelper.METHOD_PEOPLE_GET_INFO, author.getNsid())
                    .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                    .map(UserIcon::getIcon)
                    .subscribe(icon -> {
                        author.setUserAvatar(Integer.valueOf(icon.getIconServer()) == 0 ? null : FlickrHelper.getUserAvatar(icon.getIconFarm(), icon.getIconServer(), icon.getNsid()));
                        loadImage(author.getUserAvatar(), vh.image);
                        flickrDB.updateAuthor(author);
                    });

        }
        return convertView;
    }

    private void loadImage(String url, final ImageView imageView){
        ImageLoader.getInstance().displayImage(url, imageView,
                new DisplayImageOptions.Builder()
                        .cacheOnDisk(true)
                        .imageScaleType(ImageScaleType.EXACTLY)
                        .build(), new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        imageView.startAnimation(mAnimationController.getAnimation(R.anim.slide_in_top));
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {

                    }
                });
    }
}
