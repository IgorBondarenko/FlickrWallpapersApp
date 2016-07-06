package com.beautiful_wallpapers_hd_qhd.core.controller;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Igor on 28.11.2015.
 */
public class SharedPreferencesController {

    public static final String SP_WUS_INTERVAL = "WALLPAPERS_UPDATE_SERVICE_INTERVAL";
    public static final String SP_WUS_STATE = "WALLPAPERS_UPDATE_SERVICE_STATE";
    public static final String SP_WUS_COUNT = "WUS_COUNT";
    public static final String SP_IS_FIRST_TIME = "is_first_time";
    public static final String SP_PRO_VERSION = "pro";
    public static final String SP_START_CATEGORY = "start_category";
    public static final String SP_NEW_MESSAGE = "new_message";
    public static final String SP_IMAGE_QUALITY = "quality";
    public static final String SP_RATE_COUNT = "count";
    public static final String SP_OLD_APP_COUNT = "old_app_count";

    private SharedPreferences sharedPreferences;

    public SharedPreferencesController(Context c){
        sharedPreferences = c.getSharedPreferences(null, Context.MODE_PRIVATE);
    }

    public void setInt(String key, int value){
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public void setString(String key, String value){
        sharedPreferences.edit().putString(key, value).apply();
    }

    public void setBool(String key, boolean value){
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public int getInt(String key, int defaultValue){
        return sharedPreferences.getInt(key, defaultValue);
    }

    public String getString(String key, String defaultValue){
        return sharedPreferences.getString(key, defaultValue);
    }

    public boolean getBool(String key, boolean defaultValue){
        return sharedPreferences.getBoolean(key, defaultValue);
    }

}
