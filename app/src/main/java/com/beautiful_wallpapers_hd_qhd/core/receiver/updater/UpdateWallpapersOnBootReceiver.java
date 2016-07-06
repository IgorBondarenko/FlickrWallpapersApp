package com.beautiful_wallpapers_hd_qhd.core.receiver.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.beautiful_wallpapers_hd_qhd.core.controller.SharedPreferencesController;

/**
 * Created by Igor on 15.10.2015.
 */
public class UpdateWallpapersOnBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(new SharedPreferencesController(context).getBool(SharedPreferencesController.SP_WUS_STATE, false)){
            new UpdateWallpapersReceiver().startUpdateWallpapersService(context);
        }
    }
}
