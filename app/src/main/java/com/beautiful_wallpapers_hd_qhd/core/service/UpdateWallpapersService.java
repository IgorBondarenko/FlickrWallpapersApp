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
import com.beautiful_wallpapers_hd_qhd.core.di.DaggerAppComponent;
import com.beautiful_wallpapers_hd_qhd.core.di.MyModule;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.FlickrHelper;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.FlickrAPI;

import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Igor on 05.02.2015.
 */
public class UpdateWallpapersService extends Service {

    private WallpaperInstaller wallpaperInstaller;
    private Thread setWallpaperThread;

    @Inject SharedPreferencesController sPref;
    @Inject FlickrDatabase flickrDB;
    @Inject FlickrAPI flickrAPI;

    private static int count;

    @Override
    public void onCreate() {
        super.onCreate();
        DaggerAppComponent.builder().myModule(new MyModule(this)).build().inject(this);
        wallpaperInstaller = new WallpaperInstaller(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final List<String> wallpapers = flickrDB.getFavourites(FlickrDatabase.FAVOURITE_PHOTO);

        count = sPref.getInt(SharedPreferencesController.SP_WUS_COUNT, 0);

        flickrAPI.getPhotoSizes(FlickrHelper.METHOD_GET_PHOTO_SIZES, wallpapers.get(count))
                .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .map(photo -> photo.getSizes().getSizesArray().get(FlickrHelper.SIZE_LARGE).getSize())
                .subscribe(imageUrl -> {

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
