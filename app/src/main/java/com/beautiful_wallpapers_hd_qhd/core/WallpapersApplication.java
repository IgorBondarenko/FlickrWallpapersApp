package com.beautiful_wallpapers_hd_qhd.core;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.beautiful_wallpapers_hd_qhd.core.billing.InAppConfig;
import com.beautiful_wallpapers_hd_qhd.core.di.AppComponent;
import com.beautiful_wallpapers_hd_qhd.core.di.DaggerAppComponent;
import com.beautiful_wallpapers_hd_qhd.core.di.MyModule;
import com.facebook.FacebookSdk;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by Igor on 10.03.2016.
 */
public class WallpapersApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .threadPoolSize(2)
                .build();
        ImageLoader.getInstance().init(config);
        InAppConfig.init();
        FacebookSdk.sdkInitialize(getApplicationContext());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
