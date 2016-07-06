package com.beautiful_wallpapers_hd_qhd.core.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.beautiful_wallpapers_hd_qhd.R;
import com.beautiful_wallpapers_hd_qhd.core.WallpaperInstaller;
import com.beautiful_wallpapers_hd_qhd.core.controller.SharedPreferencesController;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDatabase;
import com.beautiful_wallpapers_hd_qhd.core.flickr.FlickrHelper;
import com.beautiful_wallpapers_hd_qhd.core.flickr.RequestLoadListener;

import java.util.List;

/**
 * Created by Igor on 05.02.2015.
 */
public class UpdateWallpapersService extends Service {

    private FlickrHelper flickrHelper;
    private WallpaperInstaller wallpaperInstaller;
    private Thread setWallpaperThread;
    private SharedPreferencesController sPref;
    private FlickrDatabase flickrDB;

    private static int count;

    @Override
    public void onCreate() {
        super.onCreate();
        flickrDB = new FlickrDatabase(getBaseContext());
        wallpaperInstaller = new WallpaperInstaller(getApplicationContext());
        sPref = new SharedPreferencesController(this);
        flickrHelper = new FlickrHelper();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final List<String> wallpapers = flickrDB.getFavourites(FlickrDatabase.FAVOURITE_PHOTO);

        count = sPref.getInt(SharedPreferencesController.SP_WUS_COUNT, 0);
        flickrHelper.processRequest(FlickrHelper.METHOD_GET_PHOTO_SIZES, FlickrHelper.ARG_GET_PHOTO_ID, wallpapers.get(count), new RequestLoadListener() {

            @Override
            public void onLoad(byte[] responseBody) {
                String imageUrl = flickrHelper.getValue(responseBody, "sizes", "size", FlickrHelper.SIZE_LARGE, "source");
                if(isOnline()){
                    if(wallpapers.size() == 0){
                        Toast.makeText(getBaseContext(), R.string.auto_update_empty_favorites, Toast.LENGTH_LONG).show();
                    } else if(count < wallpapers.size()){
                        setWallpaperThread = wallpaperInstaller.setWallpapersFromService(imageUrl);
                        if (!setWallpaperThread.isAlive()){
                            setWallpaperThread.start();
                            sPref.setInt(SharedPreferencesController.SP_WUS_COUNT, ++count);
                        }
                    } else {
                        sPref.setInt(SharedPreferencesController.SP_WUS_COUNT, 0);
                    }
                } else{
                    Log.d("UWS", "bad connection");
                }
            }

            @Override
            public void onFail(int statusCode, Throwable error) {

            }

        });
        return START_NOT_STICKY;
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setWallpaperThread.interrupt();
    }
}
