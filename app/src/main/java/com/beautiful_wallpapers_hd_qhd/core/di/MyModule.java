package com.beautiful_wallpapers_hd_qhd.core.di;

/**
 * Created by Igor on 28.06.2016.
 */

import android.content.Context;
import android.util.DisplayMetrics;

import com.beautiful_wallpapers_hd_qhd.core.Advertising;
import com.beautiful_wallpapers_hd_qhd.core.Device;
import com.beautiful_wallpapers_hd_qhd.core.billing.InAppConfig;
import com.beautiful_wallpapers_hd_qhd.core.controller.AnimationController;
import com.beautiful_wallpapers_hd_qhd.core.controller.SharedPreferencesController;
import com.beautiful_wallpapers_hd_qhd.core.database.FlickrDatabase;
import com.beautiful_wallpapers_hd_qhd.core.flickr.FlickrHelper;
import com.beautiful_wallpapers_hd_qhd.core.retrofit.FlickrAPI;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.onepf.oms.OpenIabHelper;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class MyModule {

    private Context mContext;

    public MyModule(Context context) {
        this.mContext = context;
    }

    @Provides
    public AnimationController provideAnimationController(){
        return new AnimationController(mContext);
    }

    @Provides
    public FlickrHelper provideFlickrHelper(){
        return new FlickrHelper();
    }

    @Provides
    public FlickrDatabase provideFlickrDatabase(){
        return new FlickrDatabase(mContext);
    }

    @Provides
    public Device provideDevice(){
        return new Device(mContext);
    }

    @Provides
    public DisplayMetrics provideDisplayMetrics(){
        return new DisplayMetrics();
    }

    @Provides
    public SharedPreferencesController provideSharedPreferencesController(){
        return new SharedPreferencesController(mContext);
    }

    @Provides
    public FlickrAPI provideFlickrAPI(){
        return new Retrofit.Builder()
                .baseUrl(FlickrHelper.RETRO_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(FlickrAPI.class);
    }

    @Provides
    public FirebaseAnalytics provideFirebaseAnalytics(){
        return FirebaseAnalytics.getInstance(mContext);
    }

    @Provides
    public OpenIabHelper provideOpenIabHelper(){
        OpenIabHelper.Options.Builder builder = new OpenIabHelper.Options.Builder()
                .setStoreSearchStrategy(OpenIabHelper.Options.SEARCH_STRATEGY_INSTALLER_THEN_BEST_FIT)
                .setVerifyMode(OpenIabHelper.Options.VERIFY_EVERYTHING)
                .addStoreKeys(InAppConfig.STORE_KEYS_MAP);
        return new OpenIabHelper(mContext, builder.build());
    }

    @Provides
    public Advertising provideAdvertising(){
        return new Advertising(mContext);
    }
}
