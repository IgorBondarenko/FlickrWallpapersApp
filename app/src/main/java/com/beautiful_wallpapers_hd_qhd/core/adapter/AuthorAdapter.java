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
import com.beautiful_wallpapers_hd_qhd.core.entity.Author;
import com.beautiful_wallpapers_hd_qhd.core.flickr.FlickrHelper;
import com.beautiful_wallpapers_hd_qhd.core.flickr.RequestLoadListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONException;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Igor on 10.04.2016.
 */
public class AuthorAdapter extends BaseAdapter {

    private final FlickrHelper flickrHelper = new FlickrHelper();
    private FlickrDatabase flickrDB;

    private AnimationController mAnimationController;
    private LayoutInflater mLayoutInflater;
    private List<String> mAuthorFlickrIds;

    public AuthorAdapter(Context context, List<String> authorIds){
        this.mAuthorFlickrIds = authorIds;
        this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.flickrDB = new FlickrDatabase(context);
        this.mAnimationController = new AnimationController(context);
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
            convertView = mLayoutInflater.inflate(R.layout.subscription_layout, parent, false);
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
            if(author.getUserAvatar() != null){
                loadImage(author.getUserAvatar(), vh.image);
            }
        } else {
            flickrHelper.processRequest(FlickrHelper.METHOD_PEOPLE_GET_INFO, FlickrHelper.ARG_USER_ID, mAuthorFlickrIds.get(position), new RequestLoadListener() {
                @Override
                public void onLoad(byte[] responseBody) {
                    try {
                        Author author = new Author();
                        author.setNsid(mAuthorFlickrIds.get(position));
                        author.setUserName(flickrHelper.getInform(responseBody, "person", "username", "_content"));
                        author.setRealName(flickrHelper.getInform(responseBody, "person", "realname", "_content"));
                        author.setUserAvatar(loadAvatar(responseBody, vh.image));

                        vh.name.setText(author.getRealName() != null ? author.getRealName() + "\n" + author.getUserName() : author.getUserName());
                        flickrDB.addAuthor(author);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFail(int statusCode, Throwable error) {

                }
            });
        }
        return convertView;
    }
    private String loadAvatar(byte[] responseBody, ImageView imageView) throws JSONException {

        String nsid = flickrHelper.getInform(responseBody, "person", FlickrHelper.PARAM_NSID);
        String iconfarm = flickrHelper.getInform(responseBody, "person", FlickrHelper.PARAM_ICONFARM);
        String iconserver = flickrHelper.getInform(responseBody, "person", FlickrHelper.PARAM_ICONSERVER);

        String url = flickrHelper.getUserAvatar(iconfarm, iconserver, nsid);
        if(Integer.valueOf(iconserver) != 0){
            loadImage(url, imageView);
            return url;
        } else {
            return null;
        }
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
