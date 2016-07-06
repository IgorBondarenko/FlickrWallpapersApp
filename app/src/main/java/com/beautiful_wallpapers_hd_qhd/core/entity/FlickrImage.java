package com.beautiful_wallpapers_hd_qhd.core.entity;

import android.content.Context;
import android.media.MediaCodecInfo;
import android.os.Parcel;
import android.os.Parcelable;

import com.beautiful_wallpapers_hd_qhd.R;

/**
 * Created by Igor on 12.04.2016.
 */
public class FlickrImage {

    private String flickrId;
    private String someSizeUrl;

    public String getFlickrId() {
        return flickrId;
    }

    public void setFlickrId(String flickrId) {
        this.flickrId = flickrId;
    }

    public String getSomeSizeUrl() {
        return someSizeUrl;
    }

    public void setSomeSizeUrl(String someSizeUrl) {
        this.someSizeUrl = someSizeUrl;
    }

}
