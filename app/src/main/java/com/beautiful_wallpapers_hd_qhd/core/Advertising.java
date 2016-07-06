package com.beautiful_wallpapers_hd_qhd.core;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.beautiful_wallpapers_hd_qhd.*;
import com.google.android.gms.ads.*;
import com.google.android.gms.ads.internal.request.StringParcel;

/**
 * Created by Igor on 24.01.2015.
 */
public class Advertising {

    public static final String MAIN_ACTIVITY_BANNER_AD_ID = "ca-app-pub-8347960194719275/1381303942";
    public static final String PREVIEW_ACTIVITY_BANNER_AD_ID = "ca-app-pub-8347960194719275/4990163542";
    public static final String FULL_SCREEN_ACTIVITY_AD_ID = "ca-app-pub-8347960194719275/6466896748";
    public static final String SAVE_ACTIVITY_AD_ID = "ca-app-pub-8347960194719275/7943629942";
    public static final String CROP_IMAGE_ACTIVITY_AD_ID = "ca-app-pub-8347960194719275/1897096344";

    private static final String TEST_DEVICE_ID = "4E838BCAB8BDC27E015FAF10BFED0F90";
    private static final boolean TEST_MODE = true;

    private Context mContext;
    private String mAdId;
    private AdRequest adRequest;
    private boolean mIsPro = false;

    public Advertising(Context context, String adId, boolean isPro) {
        this.mContext = context;
        this.mAdId = adId;
        this.adRequest = TEST_MODE ? new AdRequest.Builder().addTestDevice(TEST_DEVICE_ID).build() : new AdRequest.Builder().build();
        this.mIsPro = isPro;
    }

    public Advertising(Context context, boolean isPro){
        this.mContext = context;
        this.adRequest = TEST_MODE ? new AdRequest.Builder().addTestDevice(TEST_DEVICE_ID).build() : new AdRequest.Builder().build();
        this.mIsPro = isPro;
    }

    public void loadSmartBanner(AdView adView){
        if(mIsPro){
            adView.setVisibility(View.GONE);
        } else {
            adView.loadAd(adRequest);
        }
    }

    public void loadFullScreenAd(String adId){
        final InterstitialAd interstitial = new InterstitialAd(mContext);
        interstitial.setAdUnitId(adId);
        interstitial.loadAd(adRequest);
        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                interstitial.show();
            }
        });
    }

    public void loadFullScreenAd(){
        final InterstitialAd interstitial = new InterstitialAd(mContext);
        interstitial.setAdUnitId(mAdId);
        interstitial.loadAd(adRequest);
        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                interstitial.show();
            }
        });
    }

    public void loadBanner(LinearLayout ad_layout, final Analytics analytics, boolean inCard){
        Resources resources = mContext.getResources();
        //int width = resources.getInteger(R.integer.ad_width);
        final AdView mAdView = new AdView(mContext);
        //int width = inCard ? resources.getInteger(R.integer.ad_width) : AdSize.SMART_BANNER.getWidth();
        //mAdView.setAdSize(test_new AdSize(width, 50));

        mAdView.setAdSize(AdSize.SMART_BANNER);

        mAdView.setAdUnitId(mAdId);
        mAdView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ad_layout.addView(mAdView);
        mAdView.loadAd(adRequest);
        mAdView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                analytics.registerEvent("Preview", Analytics.SMALL_ADVERTISING_PRESSED, null, 0);
            }
        });
    }

    /*
    private StartAppAd startAppAd;

    public Advertising(Context c){
        this.mContext = c;
        startAppAd = test_new StartAppAd(c);
    }

    public void loadStartAppBanner(LinearLayout ad_layout, final Analytics analytics){
        Banner banner = test_new Banner(mContext);
        ad_layout.addView(banner);
        ad_layout.setOnClickListener(test_new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                analytics.registerEvent("Preview", Analytics.SMALL_ADVERTISING_PRESSED, null, 0);
            }
        });
    }


    public void onPause(){
        startAppAd.onPause();
    }

    public void onResume() {
        startAppAd.onResume();
    }

    public void loadStartAppFullScreenAd(){
        startAppAd.showAd();
        startAppAd.loadAd();
    }*/

}
