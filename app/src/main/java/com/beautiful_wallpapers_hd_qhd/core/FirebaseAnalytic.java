package com.beautiful_wallpapers_hd_qhd.core;

import android.content.Context;
import android.os.Bundle;

import com.beautiful_wallpapers_hd_qhd.core.di.DaggerAppComponent;
import com.beautiful_wallpapers_hd_qhd.core.di.MyModule;
import com.google.firebase.analytics.FirebaseAnalytics;

import javax.inject.Inject;

/**
 * Created by Igor on 05.07.2016.
 */
public class FirebaseAnalytic {

    public static final String BUTTON_PRESSED = "button pressed";
    public static final String CHECK_BOX = "check box pressed";
    public static final String FREEDOM = "freedom";
    public static final String SMALL_ADVERTISING_PRESSED = "small advertising pressed";
    public static final String LARGE_ADVERTISING_PRESSED = "large advertising pressed";
    public static final String PURCHASED = "purchased";
    public static final String SWIPE = "swipe";

    @Inject FirebaseAnalytics mFirebaseAnalytics;

    public FirebaseAnalytic(Context context) {
        DaggerAppComponent.builder().myModule(new MyModule(context)).build().inject(this);
    }

    public void registerEvent(String category, String action, String label, String value){
        Bundle bundle = new Bundle();
        bundle.putString("category", category);
        bundle.putString("action", action);
        bundle.putString("label", label);
        bundle.putString("value", value);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

}
