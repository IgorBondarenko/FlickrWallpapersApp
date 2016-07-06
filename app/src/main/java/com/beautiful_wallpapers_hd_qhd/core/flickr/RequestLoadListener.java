package com.beautiful_wallpapers_hd_qhd.core.flickr;

/**
 * Created by Igor on 05.02.2016.
 */
public interface RequestLoadListener {
    public void onLoad(byte[] responseBody);
    public void onFail(int statusCode, Throwable error);
}
