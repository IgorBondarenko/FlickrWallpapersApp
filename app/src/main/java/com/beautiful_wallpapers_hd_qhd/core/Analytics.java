package com.beautiful_wallpapers_hd_qhd.core;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.beautiful_wallpapers_hd_qhd.R;

/**
 * Created by Igor on 13.12.2014.
 */
public class Analytics {

    public static final String BUTTON_PRESSED = "button pressed";
    public static final String CHECK_BOX = "check box pressed";
    public static final String FREEDOM = "freedom";
    public static final String SMALL_ADVERTISING_PRESSED = "small advertising pressed";
    public static final String LARGE_ADVERTISING_PRESSED = "large advertising pressed";
    public static final String PURCHASED = "purchased";
    public static final String SWIPE = "swipe";
    private Context mContext;


    public Analytics(Context context) {
        this.mContext = context;
    }

    public void registerScreen(String name){/*
        EasyTracker tracker = EasyTracker.getInstance(mContext);
        tracker.set(Fields.SCREEN_NAME, name);
        tracker.send(MapBuilder.createAppView().build());*/
    }

    public void registerEvent(String category, String action, String label, long value){
        /*EasyTracker tracker = EasyTracker.getInstance(mContext);
        tracker.send(MapBuilder
                        .createEvent(category, action, label, value)
                        .build()
        );*/
        Tracker mTracker = GoogleAnalytics.getInstance(mContext).newTracker(R.xml.app_tracker);
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build());

    }
}
